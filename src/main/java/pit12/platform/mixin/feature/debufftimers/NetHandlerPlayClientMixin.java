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
package pit12.platform.mixin.feature.debufftimers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pit12.feature.debufftimers.VenomPacketBinding;
import pit12.feature.debufftimers.VenomTracker;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin implements VenomPacketBinding {
    @Shadow
    private Minecraft gameController;
    @Unique
    private VenomTracker pit12$venomTracker;

    @Override
    public void bindVenomTracker(VenomTracker tracker) {
        pit12$venomTracker = tracker;
    }

    @Inject(method = "handleAnimation", at = @At("TAIL"))
    private void pit12$afterVenomAnimation(S0BPacketAnimation packet, CallbackInfo ci) {
        if (pit12$venomTracker == null || packet.getAnimationType() != 0
                || (Object) this != gameController.getNetHandler()
                || gameController.theWorld == null)
            return;
        Entity entity = gameController.theWorld.getEntityByID(packet.getEntityID());
        if (entity instanceof EntityPlayer)
            pit12$venomTracker.swing((EntityPlayer) entity);
    }

    @Inject(method = "handleEntityStatus", at = @At("TAIL"))
    private void pit12$afterVenomHurt(S19PacketEntityStatus packet, CallbackInfo ci) {
        if (pit12$venomTracker == null || packet.getOpCode() != 2
                || (Object) this != gameController.getNetHandler()
                || gameController.theWorld == null)
            return;
        Entity entity = packet.getEntity(gameController.theWorld);
        if (entity instanceof EntityPlayer)
            pit12$venomTracker.hurt((EntityPlayer) entity);
    }
}
