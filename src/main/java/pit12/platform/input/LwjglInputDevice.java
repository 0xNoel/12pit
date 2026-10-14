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
package pit12.platform.input;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.InputImplementation;
import pit12.runtime.input.InputDevice;

public final class LwjglInputDevice implements InputDevice {
    private final Object lock;
    private final Field keyboardBackend;
    private final Field mouseBackend;
    private InputImplementation keyboardOriginal;
    private InputImplementation keyboardBlocked;
    private InputImplementation mouseOriginal;
    private InputImplementation mouseBlocked;

    public LwjglInputDevice() {
        try {
            // LaunchWrapper loads LWJGL in its parent, outside Mixin's transformer.
            Class<?> access = Class.forName("org.lwjgl.input.OpenGLPackageAccess", true,
                    Keyboard.class.getClassLoader());
            Field globalLock = access.getDeclaredField("global_lock");
            globalLock.setAccessible(true);
            lock = globalLock.get(null);
            keyboardBackend = Keyboard.class.getDeclaredField("implementation");
            keyboardBackend.setAccessible(true);
            mouseBackend = Mouse.class.getDeclaredField("implementation");
            mouseBackend.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not access the LWJGL input backend", exception);
        }
    }

    @Override
    public void block() {
        synchronized (lock) {
            try {
                if (Keyboard.isCreated()) {
                    InputImplementation original = (InputImplementation) keyboardBackend.get(null);
                    if (original != keyboardOriginal) {
                        keyboardOriginal = original;
                        keyboardBlocked = mask(original);
                    }
                    keyboardBackend.set(null, keyboardBlocked);
                }
                if (Mouse.isCreated()) {
                    InputImplementation original = (InputImplementation) mouseBackend.get(null);
                    if (original != mouseOriginal) {
                        mouseOriginal = original;
                        mouseBlocked = mask(original);
                    }
                    mouseBackend.set(null, mouseBlocked);
                }
                pollAndDrain();
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not block LWJGL input", exception);
            }
        }
    }

    @Override
    public void unblock() {
        synchronized (lock) {
            try {
                // Drain native events before restoring the backend so they cannot be replayed.
                try {
                    pollAndDrain();
                } finally {
                    if (keyboardBlocked != null && keyboardBackend.get(null) == keyboardBlocked)
                        keyboardBackend.set(null, keyboardOriginal);
                    if (mouseBlocked != null && mouseBackend.get(null) == mouseBlocked)
                        mouseBackend.set(null, mouseOriginal);
                }
                pollAndDrain();
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not restore LWJGL input", exception);
            }
        }
    }

    private static InputImplementation mask(InputImplementation original) {
        return (InputImplementation) Proxy.newProxyInstance(
                InputImplementation.class.getClassLoader(),
                new Class<?>[] {InputImplementation.class}, (proxy, method, arguments) -> {
                    switch (method.getName()) {
                        case "pollKeyboard":
                            ByteBuffer keys = (ByteBuffer) arguments[0];
                            original.pollKeyboard(keys);
                            clear(keys);
                            return null;
                        case "pollMouse":
                            IntBuffer coordinates = (IntBuffer) arguments[0];
                            ByteBuffer buttons = (ByteBuffer) arguments[1];
                            original.pollMouse(coordinates, buttons);
                            clear(buttons);
                            coordinates.put(0, Mouse.isGrabbed() ? 0 : Mouse.getX());
                            coordinates.put(1, Mouse.isGrabbed() ? 0 : Mouse.getY());
                            coordinates.put(2, 0);
                            return null;
                        case "readKeyboard":
                        case "readMouse":
                            ByteBuffer events = (ByteBuffer) arguments[0];
                            do {
                                events.clear();
                                if (method.getName().equals("readKeyboard"))
                                    original.readKeyboard(events);
                                else
                                    original.readMouse(events);
                            } while (events.position() != 0);
                            return null;
                        default:
                            return MethodHandles.publicLookup().unreflect(method).bindTo(original)
                                    .invokeWithArguments(arguments);
                    }
                });
    }

    private static void clear(ByteBuffer buffer) {
        for (int i = 0; i < buffer.limit(); i++)
            buffer.put(i, (byte) 0);
    }

    private static void pollAndDrain() {
        if (Keyboard.isCreated()) {
            Keyboard.poll();
            while (Keyboard.next()) {
            }
        }
        if (Mouse.isCreated()) {
            Mouse.poll();
            while (Mouse.next()) {
            }
            Mouse.getDX();
            Mouse.getDY();
            Mouse.getDWheel();
        }
    }
}
