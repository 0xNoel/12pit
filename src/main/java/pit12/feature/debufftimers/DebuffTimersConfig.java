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
package pit12.feature.debufftimers;

import static pit12.runtime.languages.Languages.source;

import pit12.runtime.config.BooleanSetting;
import pit12.runtime.config.ColorSetting;
import pit12.runtime.config.ConfigCategory;
import pit12.runtime.config.FeatureConfig;
import pit12.runtime.config.HudAnchor;
import pit12.runtime.config.HudConfig;
import pit12.runtime.config.IntegerSetting;

public final class DebuffTimersConfig extends FeatureConfig {
    public final BooleanSetting pinOwn;
    public final BooleanSetting pinFriends;
    public final BooleanSetting pinEnemies;
    public final BooleanSetting pinOthers;
    public final IntegerSetting pinHitWindow;
    public final BooleanSetting venomSelf;
    public final BooleanSetting venomTargets;
    public final BooleanSetting venomOwn;
    public final BooleanSetting venomFriends;
    public final BooleanSetting venomEnemies;
    public final BooleanSetting venomOthers;
    public final BooleanSetting telebowOwn;
    public final BooleanSetting telebowFriends;
    public final BooleanSetting telebowEnemies;
    public final BooleanSetting telebowOthers;
    public final BooleanSetting showGroupNames;
    public final ColorSetting ownColor;
    public final ColorSetting friendColor;
    public final ColorSetting enemyColor;
    public final ColorSetting otherColor;
    public final HudConfig hud;

    public DebuffTimersConfig() {
        super("debufftimers", source("Timers"), new ConfigCategory("render", source("Render")),
                source("Shows estimated effect durations and cooldowns."));
        hud = hudConfig("debuff_timers", HudAnchor.TOP_RIGHT, 6, 6, true, HudGroup.SUBCATEGORY,
                "HUD");
        subcategory("display", source("Display"));
        showGroupNames = booleanSetting("show_group_names", source("Show group names"),
                source("Shows the name above each timer group."), true);
        ownColor = colorPickerSetting("own_color", source("Own color"),
                source("Sets the color of your countdowns."), 0xFF55FFFF);
        friendColor = colorPickerSetting("friend_color", source("Friend color"),
                source("Sets the color of friends' countdowns."), 0xFF55FF55);
        enemyColor = colorPickerSetting("enemy_color", source("Enemy color"),
                source("Sets the color of enemies' countdowns."), 0xFFFF5555);
        otherColor = colorPickerSetting("other_color", source("Other player color"),
                source("Sets the color of other players' countdowns."), 0xFFFFFFFF);
        subcategory("debuffs", source("Debuffs"));
        subsubcategory("pin", "Pin");
        pinOwn = booleanSetting("own", source("Own"), source("Shows Pin on you."), true);
        pinFriends =
                booleanSetting("friends", source("Friends"), source("Shows Pin on friends."), true);
        pinEnemies =
                booleanSetting("enemies", source("Enemies"), source("Shows Pin on enemies."), true);
        pinOthers = booleanSetting("others", source("Others"),
                source("Shows Pin on other players."), true);
        pinHitWindow = integerSliderSetting("pin_hit_window_ms", source("Prediction margin (ms)"),
                source("Sets the time allowed between arrow collision, player damage, and arrow removal."),
                250, 50, 1000, 25);
        subsubcategory("venom", "Venom");
        venomSelf = booleanSetting("venom_self", source("Track wearers"),
                source("Estimates the wearer's 24-second poison from observed hits."), true);
        venomTargets = booleanSetting("venom_targets", source("Track targets"),
                source("Estimates the target's 12-second poison from observed hits."), true);
        venomOwn = booleanSetting("venom_own", source("Own"), source("Shows Venom on you."), true);
        venomFriends = booleanSetting("venom_friends", source("Friends"),
                source("Shows Venom on friends."), true);
        venomEnemies = booleanSetting("venom_enemies", source("Enemies"),
                source("Shows Venom on enemies."), true);
        venomOthers = booleanSetting("venom_others", source("Others"),
                source("Shows Venom on other players."), true);
        subcategory("cooldowns", source("Cooldowns"));
        subsubcategory("telebow", "Telebow");
        telebowOwn = booleanSetting("telebow_own", source("Own"),
                source("Shows your Telebow cooldown."), true);
        telebowFriends = booleanSetting("telebow_friends", source("Friends"),
                source("Shows friends' Telebow cooldowns."), true);
        telebowEnemies = booleanSetting("telebow_enemies", source("Enemies"),
                source("Shows enemies' Telebow cooldowns."), true);
        telebowOthers = booleanSetting("telebow_others", source("Others"),
                source("Shows other players' Telebow cooldowns."), true);
    }
}
