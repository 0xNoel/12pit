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
package pit12.feature.denick;

import static pit12.runtime.languages.Languages.source;

import pit12.runtime.config.BooleanSetting;
import pit12.runtime.config.ConfigCategory;
import pit12.runtime.config.FeatureConfig;

public final class DenickConfig extends FeatureConfig {
    final BooleanSetting automatic;
    final BooleanSetting successOnly;
    final BooleanSetting monitor;
    final BooleanSetting nickMarker;
    final BooleanSetting tabNames;

    public DenickConfig() {
        super("denick", source("Denick"), new ConfigCategory("misc", source("Misc")),
                source("Finds nicked players using their current equipment."));
        subsubcategory("queries", source("Queries"));
        automatic = booleanSetting("automatic", source("Auto Denick"),
                source("Queries nicked players when you enter the Pit or they join."), true);
        successOnly = booleanSetting("success_only", source("Only show successful queries"),
                source("Hides failed automatic queries and monitoring messages."), true);
        monitor = booleanSetting("monitor", source("Keep monitoring"),
                source("Queues equipment changes until a real name is found."), true);
        subsubcategory("display", source("Display"));
        nickMarker = booleanSetting("nick_marker", source("Show nick marker"),
                source("Adds N to nicked players' names in Tab and Player List."), true);
        tabNames = booleanSetting("tab_names", source("Show real names while Tab is open"), source(
                "Replaces nicked names in Tab and Player List with real names while Tab is open."),
                true);
    }
}
