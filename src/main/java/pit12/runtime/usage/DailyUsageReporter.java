/*
 * This file is part of 12pit.
 *
 * Copyright (C) 2026 The 12pit Authors and contributors <https://github.com/12src/12pit>
 *
 * 12pit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 12pit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 12pit. If not, see <https://www.gnu.org/licenses/>.
 */
package pit12.runtime.usage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import pit12.runtime.pit.PitContext;
import pit12.runtime.pit.PitState;
import pit12.shared.concurrent.ClientThread;
import pit12.shared.lifecycle.ClientLifecycle;
import pit12.shared.storage.AtomicFile;

public final class DailyUsageReporter implements ClientLifecycle {
    private static final Logger LOGGER = Logger.getLogger(DailyUsageReporter.class.getName());
    private static final String[] ENDPOINTS =
            {"https://dau.12pit.workers.dev/", "https://12pit.askll.dev/"};
    private final ClientThread client;
    private final PitContext pit;
    private Reporting reporting;
    private long observedSecond = Long.MIN_VALUE;

    public DailyUsageReporter(ClientThread client, PitContext pit) {
        this.client = client;
        this.pit = pit;
    }

    @Override
    public void start() {
        client.check();
        String doNotTrack = System.getenv("DO_NOT_TRACK");
        if (reporting != null || "1".equals(doNotTrack) || "true".equalsIgnoreCase(doNotTrack)) {
            return;
        }
        reporting = new Reporting();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void stop() {
        client.check();
        if (reporting != null) {
            MinecraftForge.EVENT_BUS.unregister(this);
            reporting.stop();
            reporting = null;
        }
        observedSecond = Long.MIN_VALUE;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.END || reporting == null) {
            return;
        }
        client.check();
        long second = System.currentTimeMillis() / 1000;
        if (second == observedSecond || pit.current().pitState() != PitState.IN_PIT) {
            return;
        }
        observedSecond = second;
        reporting.observe(LocalDate.now());
    }

    private static final class Reporting {
        private final ScheduledThreadPoolExecutor worker =
                new ScheduledThreadPoolExecutor(1, task -> {
                    Thread thread = new Thread(task, "12pit-dau");
                    thread.setDaemon(true);
                    return thread;
                });
        private ScheduledFuture<?> scheduled;
        private LocalDate observedDay;
        private LocalDate reportedDay;
        private LocalDate savedDay;
        private long sequence;
        private boolean busy;
        private boolean stopped;

        Reporting() {
            worker.setRemoveOnCancelPolicy(true);
            worker.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        }

        synchronized void observe(LocalDate day) {
            if (stopped || day.equals(observedDay)) {
                return;
            }
            observedDay = day;
            if (scheduled != null) {
                scheduled.cancel(false);
                scheduled = null;
                sequence++;
            }
            if (!busy) {
                schedule(0);
            }
        }

        synchronized void stop() {
            stopped = true;
            sequence++;
            // Let an accepted report finish saving its date.
            worker.shutdown();
        }

        private void schedule(long delay) {
            long requestSequence = ++sequence;
            scheduled =
                    worker.schedule(() -> report(requestSequence), delay, TimeUnit.MILLISECONDS);
        }

        private void report(long requestSequence) {
            LocalDate day;
            synchronized (this) {
                if (stopped || requestSequence != sequence) {
                    return;
                }
                scheduled = null;
                day = LocalDate.now();
                if (!day.equals(observedDay)) {
                    return;
                }
                busy = true;
            }
            try {
                // Do not count an earlier date after a clock or time zone change.
                if (savedDay != null && !day.isAfter(savedDay)) {
                    return;
                }
                String localAppData = System.getenv("LOCALAPPDATA");
                Path directory = localAppData == null || localAppData.isEmpty()
                        ? Paths.get(System.getProperty("user.home"), ".12pit")
                        : Paths.get(localAppData, "12pit");
                Files.createDirectories(directory);
                Path path = directory.resolve("dau-date.txt");
                // Keep the lock separate because AtomicFile replaces the date file.
                try (FileChannel channel = FileChannel.open(directory.resolve("dau.lock"),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        FileLock lock = channel.tryLock()) {
                    if (lock == null) {
                        return;
                    }
                    if (!Files.notExists(path)) {
                        String stored =
                                new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
                        if (!stored.isEmpty()) {
                            LocalDate storedDay = LocalDate.parse(stored);
                            if (!day.isAfter(storedDay)) {
                                savedDay = storedDay;
                                return;
                            }
                        }
                    }
                    if (reportedDay == null || day.isAfter(reportedDay)) {
                        if (!send(day)) {
                            return;
                        }
                        // A failed file write must not send another report from this process.
                        reportedDay = day;
                    }
                    AtomicFile.write(path, reportedDay + "\n");
                    savedDay = reportedDay;
                }
            } catch (OverlappingFileLockException failure) {
                LOGGER.fine("Another DAU request is using the date file");
            } catch (DateTimeParseException failure) {
                LOGGER.warning("Invalid DAU date: " + failure.getParsedString());
            } catch (IOException | IllegalArgumentException failure) {
                LOGGER.warning("Could not save the DAU date: " + failure);
            } finally {
                synchronized (this) {
                    busy = false;
                    ZonedDateTime current = ZonedDateTime.now();
                    LocalDate currentDay = current.toLocalDate();
                    if (!stopped && currentDay.equals(observedDay)) {
                        long delay =
                                !currentDay.equals(day) ? 0
                                        : savedDay != null && !currentDay.isAfter(savedDay)
                                                ? Duration
                                                        .between(current,
                                                                currentDay.plusDays(1).atStartOfDay(
                                                                        current.getZone()))
                                                        .toMillis()
                                                : TimeUnit.MINUTES.toMillis(5);
                        schedule(delay);
                    }
                }
            }
        }

        private boolean send(LocalDate day) {
            boolean accepted = false;
            try (CloseableHttpClient http = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000)
                            .setConnectionRequestTimeout(5000).setSocketTimeout(5000).build())
                    .setUserAgent("12pit").useSystemProperties().disableAutomaticRetries()
                    .disableRedirectHandling().disableCookieManagement().build()) {
                for (String endpoint : ENDPOINTS) {
                    ZonedDateTime now;
                    synchronized (this) {
                        now = ZonedDateTime.now();
                        // Leaving Pit does not erase activity already observed on this date.
                        if (stopped || !day.equals(observedDay) || !day.equals(now.toLocalDate())) {
                            break;
                        }
                    }
                    double offset = now.getOffset().getTotalSeconds() / 3600.0;
                    try (CloseableHttpResponse response =
                            http.execute(new HttpGet(endpoint + "?tz=" + offset))) {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            accepted = true;
                        } else {
                            LOGGER.fine("DAU request to " + endpoint + " returned HTTP " + status);
                        }
                    } catch (IOException failure) {
                        LOGGER.fine("DAU request to " + endpoint + " failed: " + failure);
                    }
                    if (accepted) {
                        break;
                    }
                }
            } catch (IOException failure) {
                LOGGER.fine("Could not close the DAU connection: " + failure);
            }
            return accepted;
        }
    }
}
