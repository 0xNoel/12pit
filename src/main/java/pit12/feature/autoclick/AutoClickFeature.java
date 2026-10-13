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

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.opengl.Display;
import pit12.runtime.config.ConfigCatalog;
import pit12.runtime.config.ConfigChangeListener;
import pit12.runtime.config.ConfigChangeSet;
import pit12.runtime.config.IntegerSetting;
import pit12.runtime.config.Setting;
import pit12.runtime.session.ClientSession;
import pit12.shared.input.MinecraftActions;
import pit12.shared.lifecycle.ClientLifecycle;

public final class AutoClickFeature implements ClientLifecycle, ConfigChangeListener {
    private final Minecraft minecraft;
    private final ConfigCatalog configs;
    private final AutoClickConfig config;
    private final ClientSession session;
    private final AutoClickBinding binding;
    private final MinecraftActions actions;
    private final Runnable sessionChanged = this::reset;
    private boolean started;
    private boolean active;
    private boolean leftClicking;
    private boolean rightClicking;
    private double leftDelayTicks;
    private double rightDelayTicks;

    public AutoClickFeature(Minecraft minecraft, ConfigCatalog configs, AutoClickConfig config,
            ClientSession session, AutoClickBinding binding, MinecraftActions actions) {
        this.minecraft = minecraft;
        this.configs = configs;
        this.config = config;
        this.session = session;
        this.binding = binding;
        this.actions = actions;
    }

    @Override
    public void start() {
        session.checkThread();
        if (started) {
            return;
        }
        started = true;
        configs.addListener(this);
        updateActivation();
    }

    @Override
    public void stop() {
        session.checkThread();
        started = false;
        configs.removeListener(this);
        updateActivation();
    }

    @Override
    public void onConfigChanged(ConfigChangeSet changes) {
        for (Setting<?> setting : config.settings()) {
            if (changes.affects(config.id(), setting.id())) {
                reset();
                updateActivation();
                return;
            }
        }
    }

    private void updateActivation() {
        boolean enabled = started && config.enabled()
                && (config.leftEnabled.get() || config.rightEnabled.get());
        if (enabled == active) {
            return;
        }
        active = enabled;
        if (enabled) {
            session.addListener(sessionChanged);
            binding.pit12$bindAutoClick(this);
        } else {
            binding.pit12$bindAutoClick(null);
            session.removeListener(sessionChanged);
        }
        reset();
    }

    public void inputTick() {
        EntityPlayerSP player = minecraft.thePlayer;
        MovingObjectPosition target = minecraft.objectMouseOver;
        if (player == null || minecraft.theWorld == null || target == null
                || minecraft.currentScreen != null || !minecraft.inGameHasFocus
                || !Display.isActive() || !player.isEntityAlive() || player.isSpectator()
                || player.isUsingItem()) {
            reset();
            return;
        }
        ItemStack held = player.getHeldItem();
        Block targetBlock = target.typeOfHit == MovingObjectType.BLOCK
                ? minecraft.theWorld.getBlockState(target.getBlockPos()).getBlock()
                : null;
        leftClicking = config.leftEnabled.get() && minecraft.gameSettings.keyBindAttack.isKeyDown()
                && ((!config.allowWeapons.get() && !config.allowFist.get()
                        && !config.leftAllowDragonEgg.get())
                        || (config.allowFist.get() && held == null)
                        || (config.allowWeapons.get() && held != null && held.stackSize > 0
                                && (held.getItem() instanceof ItemSword
                                        || held.getItem() instanceof ItemTool))
                        || (config.leftAllowDragonEgg.get() && targetBlock == Blocks.dragon_egg));
        rightClicking = config.rightEnabled.get()
                && minecraft.gameSettings.keyBindUseItem.isKeyDown()
                && ((!config.allowBlocks.get() && !config.allowCake.get()
                        && !config.rightAllowDragonEgg.get())
                        || (config.allowBlocks.get() && held != null && held.stackSize > 0
                                && held.getItem() instanceof ItemBlock)
                        || (config.allowCake.get() && targetBlock == Blocks.cake)
                        || (config.rightAllowDragonEgg.get() && targetBlock == Blocks.dragon_egg));
        if (leftClicking) {
            if (leftDelayTicks <= 0.0) {
                // Keep CPS after misses.
                actions.pit12$setLeftClickCounter(0);
                actions.pit12$leftClick();
                leftDelayTicks += delay(config.leftCpsMin, config.leftCpsMax);
            }
            leftDelayTicks--;
        } else {
            leftDelayTicks = 0.0;
        }
        if (rightClicking) {
            if (rightDelayTicks <= 0.0) {
                actions.pit12$rightClick();
                rightDelayTicks += delay(config.rightCpsMin, config.rightCpsMax);
            }
            rightDelayTicks--;
        } else {
            rightDelayTicks = 0.0;
        }
    }

    public boolean leftClicking() {
        return leftClicking;
    }

    public boolean rightClicking() {
        return rightClicking;
    }

    private static double delay(IntegerSetting minimum, IntegerSetting maximum) {
        int first = minimum.get();
        int second = maximum.get();
        // Keep fractional ticks for accurate CPS.
        return 20.0 / ThreadLocalRandom.current().nextInt(Math.min(first, second),
                Math.max(first, second) + 1);
    }

    private void reset() {
        leftClicking = false;
        rightClicking = false;
        leftDelayTicks = 0.0;
        rightDelayTicks = 0.0;
    }
}
