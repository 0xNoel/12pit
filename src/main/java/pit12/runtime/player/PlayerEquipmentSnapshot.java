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
package pit12.runtime.player;

import java.util.UUID;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import pit12.runtime.item.PitEnchantments;

public final class PlayerEquipmentSnapshot {
    private final UUID playerId;
    private final ItemStack[] equipment;
    private final PitEnchantments heldEnchantments;
    private final PitEnchantments leggingsEnchantments;
    private final int knownSlots;
    private final long revision;

    PlayerEquipmentSnapshot(UUID playerId, ItemStack[] equipment, PitEnchantments heldEnchantments,
            PitEnchantments leggingsEnchantments, int knownSlots, long revision) {
        this.playerId = playerId;
        // The cache transfers this array. Its stacks are private snapshot data.
        this.equipment = equipment;
        this.heldEnchantments = heldEnchantments;
        this.leggingsEnchantments = leggingsEnchantments;
        this.knownSlots = knownSlots;
        this.revision = revision;
    }

    public boolean hasGoldenLeggings() {
        return equipment[2] != null && equipment[2].getItem() == Items.golden_leggings;
    }

    public UUID playerId() {
        return playerId;
    }

    public long revision() {
        return revision;
    }

    public boolean heldItemKnown() {
        return (knownSlots & PlayerEquipmentCache.HELD_ITEM) != 0;
    }

    public boolean leggingsKnown() {
        return (knownSlots & PlayerEquipmentCache.LEGGINGS) != 0;
    }

    /** Slots are held item, boots, leggings, chestplate and helmet. Empty or unknown slots return null. */
    public ItemStack copyEquipment(int slot) {
        return copy(equipment[slot]);
    }

    ItemStack[] equipmentForUpdate() {
        // Unchanged stacks stay private and can be shared by later snapshots.
        return equipment.clone();
    }

    int knownSlots() {
        return knownSlots;
    }

    public PitEnchantments heldEnchantments() {
        return heldEnchantments;
    }

    public PitEnchantments leggingsEnchantments() {
        return leggingsEnchantments;
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? null : stack.copy();
    }
}
