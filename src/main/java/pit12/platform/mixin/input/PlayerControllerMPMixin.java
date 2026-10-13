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
package pit12.platform.mixin.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pit12.runtime.input.InputBlockBinding;
import pit12.runtime.input.InputBlocker;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = {"clickBlock", "onPlayerDamageBlock", "onPlayerDestroyBlock",
            "onPlayerRightClick", "sendUseItem", "interactWithEntitySendPacket",
            "isPlayerRightClickingOnEntity"}, at = @At("HEAD"), cancellable = true)
    private void pit12$blockInteraction(CallbackInfoReturnable<Boolean> callback) {
        InputBlocker blocker = ((InputBlockBinding) mc).pit12$inputBlocker();
        if (blocker != null && blocker.locked())
            callback.setReturnValue(false);
    }

    @Inject(method = {"attackEntity", "sendEnchantPacket", "sendSlotPacket", "sendPacketDropItem"},
            at = @At("HEAD"), cancellable = true)
    private void pit12$blockAction(CallbackInfo callback) {
        InputBlocker blocker = ((InputBlockBinding) mc).pit12$inputBlocker();
        if (blocker != null && blocker.locked())
            callback.cancel();
    }
}
