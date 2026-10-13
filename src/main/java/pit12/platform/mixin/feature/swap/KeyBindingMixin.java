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
package pit12.platform.mixin.feature.swap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pit12.feature.swap.SwapHooks;
import pit12.feature.swap.SwapHooksBinding;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Shadow
    private boolean pressed;
    @Shadow
    private int pressTime;

    @Inject(method = {"setKeyBindState", "onTick"}, at = @At("HEAD"), cancellable = true)
    private static void pit12$blockSwapKeyState(CallbackInfo callback) {
        SwapHooks hooks = ((SwapHooksBinding) Minecraft.getMinecraft()).pit12$swapHooks();
        if (hooks != null && hooks.inputLocked())
            callback.cancel();
    }

    @Inject(method = {"isKeyDown", "isPressed"}, at = @At("HEAD"), cancellable = true)
    private void pit12$blockSwapKeyRead(CallbackInfoReturnable<Boolean> callback) {
        SwapHooks hooks = ((SwapHooksBinding) Minecraft.getMinecraft()).pit12$swapHooks();
        if (hooks != null && hooks.inputLocked()) {
            pressed = false;
            pressTime = 0;
            callback.setReturnValue(false);
        }
    }
}
