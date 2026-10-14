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
package pit12.runtime.pit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.BlockPos;

public final class PitMid {
    static final PitMid STANDARD = new PitMid(new int[][] {{-2, -9}, {-1, -9}, {0, -9}, {1, -9},
            {2, -9}, {3, -9}, {-4, -8}, {-3, -8}, {4, -8}, {5, -8}, {-5, -7}, {6, -7}, {-6, -6},
            {7, -6}, {-7, -5}, {8, -5}, {-8, -4}, {9, -4}, {-8, -3}, {9, -3}, {-9, -2}, {10, -2},
            {-9, -1}, {10, -1}, {-9, 0}, {10, 0}, {-9, 1}, {10, 1}, {-9, 2}, {10, 2}, {-9, 3},
            {10, 3}, {-8, 4}, {9, 4}, {-8, 5}, {9, 5}, {-7, 6}, {8, 6}, {-6, 7}, {7, 7}, {-5, 8},
            {6, 8}, {-4, 9}, {-3, 9}, {4, 9}, {5, 9}, {-2, 10}, {-1, 10}, {0, 10}, {1, 10}, {2, 10},
            {3, 10}});
    static final PitMid GENESIS = new PitMid(new int[][] {{-2, -10}, {-1, -10}, {0, -10}, {1, -10},
            {2, -10}, {3, -10}, {-4, -9}, {-3, -9}, {4, -9}, {5, -9}, {-6, -8}, {-5, -8}, {6, -8},
            {7, -8}, {-7, -7}, {8, -7}, {-8, -6}, {9, -6}, {-8, -5}, {9, -5}, {-9, -4}, {10, -4},
            {-9, -3}, {10, -3}, {-10, -2}, {11, -2}, {-10, -1}, {11, -1}, {-10, 0}, {11, 0},
            {-10, 1}, {11, 1}, {-10, 2}, {11, 2}, {-10, 3}, {11, 3}, {-9, 4}, {10, 4}, {-9, 5},
            {10, 5}, {-8, 6}, {9, 6}, {-8, 7}, {9, 7}, {-7, 8}, {8, 8}, {-6, 9}, {-5, 9}, {6, 9},
            {7, 9}, {-4, 10}, {-3, 10}, {4, 10}, {5, 10}, {-2, 11}, {-1, 11}, {0, 11}, {1, 11},
            {2, 11}, {3, 11}});
    private final List<BlockPos> edgeColumns;

    private PitMid(int[][] edge) {
        List<BlockPos> columns = new ArrayList<>();
        for (int[] position : edge) {
            columns.add(new BlockPos(position[0], 0, position[1]));
        }
        edgeColumns = Collections.unmodifiableList(columns);
    }

    /** Only X and Z matter here. Y is always zero. */
    public List<BlockPos> edgeColumns() {
        return edgeColumns;
    }
}
