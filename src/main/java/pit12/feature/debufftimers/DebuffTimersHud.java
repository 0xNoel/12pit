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
package pit12.feature.debufftimers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import pit12.runtime.config.HudConfig;
import pit12.runtime.hud.HudElement;
import pit12.runtime.hud.HudRenderer;

final class DebuffTimersHud implements HudElement {
    private final Minecraft minecraft;
    private final DebuffTimersConfig config;
    private final HudRenderer renderer;
    private List<Row> rows = Collections.emptyList();
    private List<Row> content = Collections.emptyList();
    private float pixelScale = Float.NaN;
    private boolean dirty = true;
    private boolean editing;
    private boolean unicode;
    private int width;
    private int height;
    private int lineHeight;
    private boolean metricsDirty = true;
    private final int[] titleWidths = new int[TimerType.values().length];
    float visibleHeight = Float.POSITIVE_INFINITY;

    DebuffTimersHud(Minecraft minecraft, DebuffTimersConfig config, HudRenderer renderer) {
        this.minecraft = minecraft;
        this.config = config;
        this.renderer = renderer;
    }

    void rows(List<Row> rows) {
        this.rows = rows;
        dirty = true;
    }

    void invalidate() {
        dirty = true;
    }

    boolean empty() {
        return rows.isEmpty();
    }

    @Override
    public String id() {
        return "debuff-timers";
    }

    @Override
    public String displayName() {
        return config.displayName();
    }

    @Override
    public boolean enabled() {
        return config.enabled();
    }

    @Override
    public HudConfig config() {
        return config.hud;
    }

    @Override
    public void resize(float pixelScale) {
        if (Float.compare(this.pixelScale, pixelScale) != 0) {
            this.pixelScale = pixelScale;
            renderer.resize(pixelScale);
            dirty = true;
            metricsDirty = true;
        }
    }

    @Override
    public void prepare(float pixelScale, boolean editing) {
        resize(pixelScale);
        boolean nextUnicode = minecraft.fontRendererObj.getUnicodeFlag();
        if (!dirty && this.editing == editing && unicode == nextUnicode)
            return;
        dirty = false;
        this.editing = editing;
        metricsDirty |= unicode != nextUnicode;
        unicode = nextUnicode;
        if (editing) {
            String name = minecraft.thePlayer == null ? "You"
                    : minecraft.thePlayer.getDisplayName().getFormattedText();
            content = Arrays.asList(new Row(TimerType.PIN, Group.OWN, name, 10_000_000_000L),
                    new Row(TimerType.VENOM, Group.OWN, name, 24_000_000_000L),
                    new Row(TimerType.TELEBOW, Group.OWN, name, 20_000_000_000L));
        } else {
            content = rows;
        }
        lineHeight = renderer.fontHeight() + 2;
        if (metricsDirty) {
            Arrays.fill(titleWidths, -1);
            if (editing) {
                for (Row row : rows) {
                    row.nameWidth = -1;
                    row.timeWidth = -1;
                }
            }
        }
        int nameWidth = 0;
        int timeWidth = 0;
        int titleWidth = 0;
        int lines = content.size();
        TimerType previousType = null;
        Group previous = null;
        for (Row row : content) {
            if (metricsDirty) {
                row.nameWidth = -1;
                row.timeWidth = -1;
            }
            if (previous != null && (previousType != row.type || previous != row.group))
                lines++;
            if (previousType != row.type && config.showGroupNames.get()) {
                lines++;
                int index = row.type.ordinal();
                if (titleWidths[index] < 0)
                    titleWidths[index] = renderer.textWidth(row.type.title);
                titleWidth = Math.max(titleWidth, titleWidths[index]);
            }
            previousType = row.type;
            previous = row.group;
            if (row.nameWidth < 0)
                row.nameWidth = renderer.textWidth(row.name);
            nameWidth = Math.max(nameWidth, row.nameWidth);
            if (row.timeWidth < 0)
                row.timeWidth = renderer.textWidth(row.time);
            timeWidth = Math.max(timeWidth, row.timeWidth);
        }
        width = content.isEmpty() ? 0 : 8 + Math.max(titleWidth, nameWidth + 10 + timeWidth);
        height = content.isEmpty() ? 0 : 8 + lineHeight * lines;
        metricsDirty = false;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void render(float partialTicks, boolean editing) {
        int y = 4;
        TimerType previousType = null;
        Group previous = null;
        for (Row row : content) {
            if (previous != null && (previousType != row.type || previous != row.group))
                y += lineHeight;
            if (!editing && y >= visibleHeight + lineHeight)
                break;
            if (previousType != row.type && config.showGroupNames.get()) {
                renderer.text(row.type.title, 4, y, 0xFFFFFFFF, config.hud);
                y += lineHeight;
            }
            previousType = row.type;
            previous = row.group;
            renderer.text(row.name, 4, y, 0xFFFFFFFF, config.hud);
            renderer.text(row.time, width - 4 - row.timeWidth, y, row.group.color(config),
                    config.hud);
            y += lineHeight;
        }
    }

    enum TimerType {
        PIN("§e§lPin"), VENOM("§d§lVenom"), TELEBOW("§b§lTelebow");

        final String title;

        TimerType(String title) {
            this.title = title;
        }
    }
    enum Group {
        OWN, FRIEND, ENEMY, OTHER;

        int color(DebuffTimersConfig config) {
            switch (this) {
                case OWN:
                    return config.ownColor.get();
                case FRIEND:
                    return config.friendColor.get();
                case ENEMY:
                    return config.enemyColor.get();
                default:
                    return config.otherColor.get();
            }
        }
    }
    static final class Row {
        final TimerType type;
        Group group;
        String name;
        String time;
        long until;
        private long tenths = Long.MIN_VALUE;
        int nameWidth = -1;
        int timeWidth = -1;

        Row(TimerType type, Group group, String name, long remainingNanos) {
            this.type = type;
            update(group, name, remainingNanos);
        }

        void update(Group group, String name, long remainingNanos) {
            this.group = group;
            if (!name.equals(this.name)) {
                this.name = name;
                nameWidth = -1;
            }
            long next = (remainingNanos + 99_999_999L) / 100_000_000L;
            if (tenths != next) {
                tenths = next;
                time = tenths / 10 + "." + tenths % 10 + "s";
                timeWidth = -1;
            }
        }
    }
}
