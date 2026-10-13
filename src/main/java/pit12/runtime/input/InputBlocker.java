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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import pit12.shared.concurrent.ClientThread;
import pit12.shared.lifecycle.ClientLifecycle;

public final class InputBlocker implements ClientLifecycle {
    private final Minecraft minecraft;
    private final ClientThread client;
    private final InputBlockBinding binding;
    private final Set<Object> owners = new HashSet<>();
    private final List<InputHandler> handlers = new ArrayList<>();
    private boolean started;

    public InputBlocker(Minecraft minecraft, ClientThread client, InputBlockBinding binding) {
        this.minecraft = minecraft;
        this.client = client;
        this.binding = binding;
    }

    @Override
    public void start() {
        client.check();
        if (started)
            return;
        started = true;
        binding.pit12$bindInputBlocker(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void stop() {
        client.check();
        if (locked()) {
            owners.clear();
            release(false);
        }
        handlers.clear();
        binding.pit12$bindInputBlocker(null);
        MinecraftForge.EVENT_BUS.unregister(this);
        started = false;
    }

    public void addHandler(InputHandler handler) {
        client.check();
        if (!handlers.contains(handler))
            handlers.add(handler);
    }

    public void removeHandler(InputHandler handler) {
        client.check();
        handlers.remove(handler);
    }

    public void lock(Object owner) {
        client.check();
        boolean alreadyLocked = locked();
        owners.add(owner);
        if (alreadyLocked)
            return;
        KeyBinding.unPressAllKeys();
        if (minecraft.playerController != null) {
            minecraft.playerController.resetBlockRemoving();
            if (minecraft.thePlayer != null && minecraft.thePlayer.isUsingItem())
                minecraft.playerController.onStoppedUsingItem(minecraft.thePlayer);
        }
        suppressMovement();
    }

    public void unlock(Object owner, boolean resume) {
        client.check();
        if (owners.remove(owner) && !locked())
            release(resume);
    }

    public boolean locked() {
        return !owners.isEmpty();
    }

    public boolean blockedFor(Object owner) {
        return locked() && (owners.size() != 1 || !owners.contains(owner));
    }

    public boolean key(int key, boolean pressed, boolean repeat) {
        if (locked())
            return true;
        for (InputHandler handler : handlers) {
            if (handler.key(key, pressed, repeat))
                return true;
        }
        return false;
    }

    public void suppressMovement() {
        EntityPlayerSP player = minecraft.thePlayer;
        if (!locked() || player == null)
            return;
        player.movementInput.moveForward = 0;
        player.movementInput.moveStrafe = 0;
        player.movementInput.jump = false;
        player.movementInput.sneak = false;
        player.moveForward = 0;
        player.moveStrafing = 0;
        player.setJumping(false);
        player.setSprinting(false);
    }

    private void release(boolean resume) {
        if (Keyboard.isCreated())
            while (Keyboard.next()) {
            }
        if (Mouse.isCreated()) {
            while (Mouse.next()) {
            }
            Mouse.getDX();
            Mouse.getDY();
        }
        minecraft.mouseHelper.deltaX = 0;
        minecraft.mouseHelper.deltaY = 0;
        KeyBinding.unPressAllKeys();
        if (!resume || !minecraft.inGameHasFocus || !Display.isActive())
            return;
        // Restore held states without replaying presses buffered while input was blocked.
        for (KeyBinding keyBinding : minecraft.gameSettings.keyBindings) {
            int key = keyBinding.getKeyCode();
            boolean consumed = false;
            for (InputHandler handler : handlers) {
                if (handler.consumesKey(key)) {
                    consumed = true;
                    break;
                }
            }
            if (!consumed)
                KeyBinding.setKeyBindState(key, physicallyHeld(key));
        }
    }

    public static boolean physicallyHeld(int key) {
        if (key > 0 && key < Keyboard.KEYBOARD_SIZE)
            return Keyboard.isCreated() && Keyboard.isKeyDown(key);
        int button = key + 100;
        return key < 0 && Mouse.isCreated() && button >= 0 && button < Mouse.getButtonCount()
                && Mouse.isButtonDown(button);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (locked())
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (locked())
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGameMouse(MouseEvent event) {
        if (locked())
            event.setCanceled(true);
    }
}
