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
package pit12.feature.autoclick;

import static pit12.runtime.languages.Languages.source;

import pit12.runtime.config.BooleanSetting;
import pit12.runtime.config.ConfigCategory;
import pit12.runtime.config.FeatureConfig;
import pit12.runtime.config.IntegerSetting;

public final class AutoClickConfig extends FeatureConfig {
    final BooleanSetting leftEnabled;
    final IntegerSetting leftIntervalMin;
    final IntegerSetting leftIntervalMax;
    final BooleanSetting allowWeapons;
    final BooleanSetting allowFist;
    final BooleanSetting leftAllowDragonEgg;
    final BooleanSetting rightEnabled;
    final IntegerSetting rightIntervalMin;
    final IntegerSetting rightIntervalMax;
    final BooleanSetting allowBlocks;
    final BooleanSetting allowCake;
    final BooleanSetting rightAllowDragonEgg;

    public AutoClickConfig() {
        super("auto_click", source("Auto Click"), ConfigCategory.COMBAT, source(
                "Repeats left and right clicks while their keys are held. Turning off all filters for a click type removes its item restrictions."),
                true, false);
        subcategory("left", source("Left click"));
        leftEnabled = booleanSetting("left_enabled", source("Left click enabled"),
                source("Repeats left clicks while the left-click key is held."), true);
        leftIntervalMin = integerSliderSetting("left_interval_min_phases",
                source("Minimum interval (phases)"),
                source("Minimum time between left clicks. CPS is about 40 divided by phases."), 3,
                1, 20, 1);
        leftIntervalMax = integerSliderSetting("left_interval_max_phases",
                source("Maximum interval (phases)"),
                source("Maximum time between left clicks. CPS is about 40 divided by phases."), 7,
                1, 20, 1);
        allowWeapons = booleanSetting("weapon_only", source("Allow weapons"),
                source("Allows left clicks with swords and tools."), true);
        allowFist = booleanSetting("only_fist", source("Allow fist"),
                source("Allows left clicks with an empty hand."), true);
        leftAllowDragonEgg = booleanSetting("left_allow_dragon_egg", source("Allow dragon egg"),
                source("Allows left clicks while pointing at a dragon egg."), true);
        subcategory("right", source("Right click"));
        rightEnabled = booleanSetting("right_enabled", source("Right click enabled"),
                source("Repeats right clicks while the right-click key is held."), true);
        rightIntervalMin = integerSliderSetting("right_interval_min_phases",
                source("Minimum interval (phases)"),
                source("Minimum time between right clicks. CPS is about 40 divided by phases."), 3,
                1, 20, 1);
        rightIntervalMax = integerSliderSetting("right_interval_max_phases",
                source("Maximum interval (phases)"),
                source("Maximum time between right clicks. CPS is about 40 divided by phases."), 7,
                1, 20, 1);
        allowBlocks = booleanSetting("only_blocks", source("Allow blocks"),
                source("Allows right clicks while holding a block."), true);
        allowCake = booleanSetting("only_cake", source("Allow cake"),
                source("Allows right clicks while pointing at cake."), true);
        rightAllowDragonEgg = booleanSetting("right_allow_dragon_egg", source("Allow dragon egg"),
                source("Allows right clicks while pointing at a dragon egg."), true);
    }
}
