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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pit12.runtime.input.InputBlockBinding;
import pit12.runtime.input.InputBlocker;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"),
            cancellable = true)
    private void pit12$blockActionPacket(Packet<?> packet, CallbackInfo callback) {
        if (pit12$blocksPacket(packet))
            callback.cancel();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"), cancellable = true)
    private void pit12$blockActionPacketWithListeners(Packet<?> packet,
            GenericFutureListener<? extends Future<? super Void>> listener,
            GenericFutureListener<? extends Future<? super Void>>[] listeners,
            CallbackInfo callback) {
        if (pit12$blocksPacket(packet))
            callback.cancel();
    }

    @Unique
    private boolean pit12$blocksPacket(Packet<?> packet) {
        if (packet instanceof C07PacketPlayerDigging) {
            Action action = ((C07PacketPlayerDigging) packet).getStatus();
            // Stopping mining and item use must still reach the server.
            if (action == Action.ABORT_DESTROY_BLOCK || action == Action.RELEASE_USE_ITEM)
                return false;
        } else if (!(packet instanceof C02PacketUseEntity
                || packet instanceof C08PacketPlayerBlockPlacement
                || packet instanceof C0APacketAnimation || packet instanceof C0EPacketClickWindow
                || packet instanceof C10PacketCreativeInventoryAction
                || packet instanceof C11PacketEnchantItem)) {
            return false;
        }
        InputBlocker blocker = ((InputBlockBinding) Minecraft.getMinecraft()).pit12$inputBlocker();
        return blocker != null && blocker.blocksActions();
    }
}
