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
package pit12.runtime.input;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import pit12.shared.concurrent.ClientThread;
import pit12.shared.lifecycle.ClientLifecycle;

public final class InputTicks implements ClientLifecycle {
    private final ClientThread client;
    private final InputTickBinding binding;
    // Callbacks can unsubscribe while a phase is running.
    private Listener[] listeners = new Listener[0];
    private boolean started;

    public InputTicks(ClientThread client, InputTickBinding binding) {
        this.client = client;
        this.binding = binding;
    }

    @Override
    public void start() {
        client.check();
        started = true;
        if (listeners.length > 0)
            binding.pit12$bindInputTicks(this);
    }

    @Override
    public void stop() {
        client.check();
        started = false;
        binding.pit12$bindInputTicks(null);
        listeners = new Listener[0];
    }

    public void addListener(Listener listener) {
        client.check();
        for (Listener existing : listeners) {
            if (existing == listener)
                return;
        }
        listeners = Arrays.copyOf(listeners, listeners.length + 1);
        listeners[listeners.length - 1] = listener;
        if (started)
            binding.pit12$bindInputTicks(this);
    }

    public void removeListener(Listener listener) {
        client.check();
        for (int index = 0; index < listeners.length; index++) {
            if (listeners[index] != listener)
                continue;
            Listener[] remaining = new Listener[listeners.length - 1];
            System.arraycopy(listeners, 0, remaining, 0, index);
            System.arraycopy(listeners, index + 1, remaining, index, listeners.length - index - 1);
            listeners = remaining;
            if (listeners.length == 0)
                binding.pit12$bindInputTicks(null);
            return;
        }
    }

    public void tick(Phase phase) {
        client.check();
        for (Listener listener : listeners)
            listener.onInputTick(phase);
    }

    // All phases precede the player's movement packets.
    public enum Phase {
        START, GUI_INPUT, GUI_UPDATE, BEFORE_ACTIONS, AFTER_INPUT
    }
    public interface Listener {
        void onInputTick(Phase phase);
    }
    public static final class Timing {
        private final Phase[] phases;
        private Phase selected;

        public Timing(Phase... phases) {
            this.phases = phases;
        }

        public boolean ready(Phase phase, boolean random) {
            if (!random)
                return phase == phases[phases.length - 1];
            if (selected == null) {
                int first = 0;
                while (phases[first].ordinal() < phase.ordinal())
                    first++;
                selected = phases[ThreadLocalRandom.current().nextInt(first, phases.length)];
            }
            // GUI hooks can be skipped when a screen closes during input.
            return phase.ordinal() >= selected.ordinal();
        }

        public void reset() {
            selected = null;
        }
    }
}
