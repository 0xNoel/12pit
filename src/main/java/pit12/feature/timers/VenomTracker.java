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
package pit12.feature.timers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import pit12.runtime.item.PitEnchantment;
import pit12.runtime.item.PitEnchantmentReader;
import pit12.runtime.player.PlayerEntityTracker;
import pit12.runtime.player.PlayerEquipmentAccess;
import pit12.runtime.player.PlayerEquipmentCache;
import pit12.runtime.player.PlayerEquipmentListener;
import pit12.runtime.player.PlayerEquipmentSnapshot;
import pit12.runtime.session.ClientSession;

public final class VenomTracker implements PlayerEquipmentListener {
    private final Minecraft minecraft;
    private final ClientSession session;
    private final PlayerEntityTracker players;
    private final PlayerEquipmentAccess equipment;
    private final BiConsumer<EntityPlayer, EntityPlayer> triggered;
    private final Runnable resetTimers;
    private final Runnable sessionChanged = this::onSessionChanged;
    private final Map<UUID, Combo> wearers = new HashMap<>();
    // Swing and hurt packets can arrive in either order.
    private final Map<Integer, Hit> pendingHits = new HashMap<>();
    private NetHandlerPlayClient boundConnection;

    VenomTracker(Minecraft minecraft, ClientSession session, PlayerEntityTracker players,
            PlayerEquipmentAccess equipment, BiConsumer<EntityPlayer, EntityPlayer> triggered,
            Runnable resetTimers) {
        this.minecraft = minecraft;
        this.session = session;
        this.players = players;
        this.equipment = equipment;
        this.triggered = triggered;
        this.resetTimers = resetTimers;
    }

    void active(boolean active) {
        if (active) {
            equipment.addListener(this);
            session.addListener(sessionChanged);
            onSessionChanged();
        } else {
            equipment.removeListener(this);
            session.removeListener(sessionChanged);
            unbind();
            wearers.clear();
            pendingHits.clear();
            resetTimers.run();
        }
    }

    private void onSessionChanged() {
        unbind();
        wearers.clear();
        pendingHits.clear();
        resetTimers.run();
        NetHandlerPlayClient connection = session.connection();
        if (connection instanceof VenomPacketBinding) {
            ((VenomPacketBinding) connection).bindVenomTracker(this);
            boundConnection = connection;
        }
        if (session.world() != null) {
            for (EntityPlayer player : session.world().playerEntities) {
                observe(player, PitEnchantmentReader.contains(player.getCurrentArmor(1),
                        PitEnchantment.Combo_Venom));
            }
        }
    }

    private void unbind() {
        if (boundConnection instanceof VenomPacketBinding)
            ((VenomPacketBinding) boundConnection).bindVenomTracker(null);
        boundConnection = null;
    }

    @Override
    public void onPlayerEquipmentChanged(UUID playerId, int changedSlots, long revision) {
        if ((changedSlots & PlayerEquipmentCache.LEGGINGS) == 0)
            return;
        EntityPlayer player = players.loadedPlayer(playerId);
        PlayerEquipmentSnapshot snapshot = equipment.loadedEquipment(playerId);
        if (player != null && snapshot != null)
            observe(player, snapshot.leggingsEnchantments().contains(PitEnchantment.Combo_Venom));
    }

    @Override
    public void onPlayerEquipmentRemoved(UUID playerId) {
        wearers.remove(playerId);
    }

    @Override
    public void onPlayerEquipmentReset() {
        wearers.clear();
        pendingHits.clear();
    }

    private void observe(EntityPlayer player, boolean venom) {
        UUID playerId = player.getUniqueID();
        if (!venom || !player.isEntityAlive()) {
            wearers.remove(playerId);
            return;
        }
        Combo previous = wearers.get(playerId);
        if (previous == null || previous.player != player)
            wearers.put(playerId, new Combo(player));
    }

    private void observeLocal() {
        EntityPlayer player = minecraft.thePlayer;
        if (player != null)
            observe(player, PitEnchantmentReader.contains(player.getCurrentArmor(1),
                    PitEnchantment.Combo_Venom));
    }

    public void swing(EntityPlayer player) {
        if (wearers.isEmpty() || player == minecraft.thePlayer)
            return;
        Combo combo = wearers.get(player.getUniqueID());
        if (combo == null)
            return;
        combo.swungAt = System.nanoTime();
        combo.from = player.getPositionEyes(1);
        Vec3 look = player.getLook(1);
        combo.to = combo.from.addVector(look.xCoord * 4.5, look.yCoord * 4.5, look.zCoord * 4.5);
    }

    void attack(EntityPlayer target) {
        observeLocal();
        if (wearers.isEmpty() || minecraft.thePlayer == null)
            return;
        Combo combo = wearers.get(minecraft.thePlayer.getUniqueID());
        if (combo != null) {
            combo.swungAt = System.nanoTime();
            combo.localTarget = target;
        }
    }

    public void hurt(EntityPlayer player) {
        if (wearers.isEmpty())
            return;
        for (Combo combo : wearers.values()) {
            if (combo.player != player && combo.player.getDistanceSqToEntity(player) <= 36) {
                pendingHits.put(player.getEntityId(), new Hit(player, System.nanoTime()));
                return;
            }
        }
    }

    void tick() {
        observeLocal();
        if (wearers.isEmpty()) {
            pendingHits.clear();
            return;
        }
        wearers.values().removeIf(combo -> !combo.player.isEntityAlive()
                || players.loadedPlayer(combo.player.getUniqueID()) != combo.player);
        if (pendingHits.isEmpty())
            return;
        long now = System.nanoTime();
        Iterator<Hit> iterator = pendingHits.values().iterator();
        while (iterator.hasNext()) {
            Hit hit = iterator.next();
            if (now - hit.at > 250_000_000L || hit.player.worldObj != minecraft.theWorld
                    || minecraft.theWorld.getEntityByID(hit.player.getEntityId()) != hit.player) {
                iterator.remove();
                continue;
            }
            Combo combo = attacker(hit);
            if (combo == null)
                continue;
            iterator.remove();
            combo.swungAt = 0;
            combo.localTarget = null;
            if (combo.target != hit.player) {
                combo.target = hit.player;
                combo.hits = 0;
            }
            if (++combo.hits == 3) {
                combo.hits = 0;
                triggered.accept(combo.player, hit.player);
            }
        }
    }

    private Combo attacker(Hit hit) {
        Combo candidate = null;
        AxisAlignedBB box = null;
        for (Combo combo : wearers.values()) {
            if (combo.player == hit.player || combo.swungAt == 0)
                continue;
            long age = hit.at - combo.swungAt;
            if (age < -150_000_000L
                    || age > (combo.localTarget == null ? 350_000_000L : 750_000_000L))
                continue;
            if (combo.localTarget != null) {
                if (combo.localTarget != hit.player
                        || combo.player.getDistanceSqToEntity(hit.player) > 36)
                    continue;
            } else {
                if (box == null)
                    box = hit.player.getEntityBoundingBox().expand(0.4, 0.4, 0.4);
                if (box.calculateIntercept(combo.from, combo.to) == null)
                    continue;
            }
            // Hurt packets do not name the attacker. Leave ambiguous hits uncounted.
            if (candidate != null)
                return null;
            candidate = combo;
        }
        return candidate;
    }

    private static final class Combo {
        final EntityPlayer player;
        EntityPlayer target;
        int hits;
        long swungAt;
        EntityPlayer localTarget;
        Vec3 from;
        Vec3 to;

        Combo(EntityPlayer player) {
            this.player = player;
        }
    }
    private static final class Hit {
        final EntityPlayer player;
        final long at;

        Hit(EntityPlayer player, long at) {
            this.player = player;
            this.at = at;
        }
    }
}
