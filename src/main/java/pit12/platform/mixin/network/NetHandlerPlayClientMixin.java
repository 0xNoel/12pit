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
package pit12.platform.mixin.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pit12.runtime.player.PlayerEquipmentPacketBinding;
import pit12.runtime.player.PlayerEquipmentPacketObserver;
import pit12.runtime.player.TabPacketBinding;
import pit12.runtime.player.TabPacketObserver;
import pit12.runtime.scoreboard.SidebarPacketBinding;
import pit12.runtime.scoreboard.SidebarPacketObserver;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin
        implements PlayerEquipmentPacketBinding, TabPacketBinding, SidebarPacketBinding {
    @Unique
    private PlayerEquipmentPacketObserver pit12$playerEquipmentObserver;
    @Unique
    private TabPacketObserver pit12$tabObserver;
    @Unique
    private SidebarPacketObserver pit12$sidebarObserver;

    @Override
    public void bindPlayerEquipmentObserver(PlayerEquipmentPacketObserver observer) {
        pit12$playerEquipmentObserver = observer;
    }

    @Override
    public void bindTabObserver(TabPacketObserver observer) {
        pit12$tabObserver = observer;
    }

    @Override
    public void bindSidebarObserver(SidebarPacketObserver observer) {
        pit12$sidebarObserver = observer;
    }

    @Inject(method = "handleEntityEquipment", at = @At("TAIL"))
    private void pit12$afterEntityEquipment(S04PacketEntityEquipment packet, CallbackInfo ci) {
        PlayerEquipmentPacketObserver observer = pit12$playerEquipmentObserver;
        if (observer != null) {
            observer.onEquipmentPacket(packet.getEntityID(), packet.getEquipmentSlot());
        }
    }

    @Inject(method = "handlePlayerListItem", at = @At("TAIL"))
    private void pit12$afterPlayerListItem(S38PacketPlayerListItem packet, CallbackInfo ci) {
        TabPacketObserver observer = pit12$tabObserver;
        if (observer != null) {
            observer.onTabPacket(packet);
        }
    }

    @Inject(method = "handleScoreboardObjective", at = @At("TAIL"))
    private void pit12$afterObjective(S3BPacketScoreboardObjective packet, CallbackInfo ci) {
        // Action 2 only changes the title and score render type.
        if (pit12$sidebarObserver != null && packet.func_149338_e() != 2) {
            pit12$sidebarObserver.onSidebarPacket(packet);
        }
    }

    @Inject(method = "handleUpdateScore", at = @At("TAIL"))
    private void pit12$afterScore(S3CPacketUpdateScore packet, CallbackInfo ci) {
        if (pit12$sidebarObserver != null) {
            pit12$sidebarObserver.onSidebarPacket(packet);
        }
    }

    @Inject(method = "handleDisplayScoreboard", at = @At("TAIL"))
    private void pit12$afterDisplay(S3DPacketDisplayScoreboard packet, CallbackInfo ci) {
        if (pit12$sidebarObserver != null && packet.func_149371_c() != 0
                && packet.func_149371_c() != 2) {
            pit12$sidebarObserver.onSidebarPacket(packet);
        }
    }

    @Inject(method = "handleTeams", at = @At("TAIL"))
    private void pit12$afterTeam(S3EPacketTeams packet, CallbackInfo ci) {
        if (pit12$sidebarObserver != null) {
            pit12$sidebarObserver.onSidebarPacket(packet);
        }
    }
}
