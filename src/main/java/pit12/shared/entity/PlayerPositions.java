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
package pit12.shared.entity;

import net.minecraft.entity.player.EntityPlayer;

public final class PlayerPositions {
    private PlayerPositions() {}

    /** Remote players interpolate toward serverPos, so compare server coordinates. */
    public static double serverDistanceSquared(EntityPlayer player, EntityPlayer localPlayer,
            double x, double y, double z) {
        boolean local = player == localPlayer;
        double dx = x - (local ? player.posX : player.serverPosX / 32.0);
        double dy = y - (local ? player.posY : player.serverPosY / 32.0);
        double dz = z - (local ? player.posZ : player.serverPosZ / 32.0);
        return dx * dx + dy * dy + dz * dz;
    }
}
