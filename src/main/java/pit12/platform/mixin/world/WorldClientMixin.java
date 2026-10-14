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
package pit12.platform.mixin.world;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pit12.runtime.player.PlayerEntityWorldBinding;
import pit12.runtime.player.PlayerEntityWorldObserver;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin implements PlayerEntityWorldBinding {
    @Unique
    private PlayerEntityWorldObserver pit12$playerEntityObserver;

    @Override
    public void bindPlayerEntityObserver(PlayerEntityWorldObserver observer) {
        pit12$playerEntityObserver = observer;
    }

    @Inject(method = "onEntityAdded", at = @At("RETURN"))
    private void pit12$afterEntityAdded(Entity entity, CallbackInfo ci) {
        PlayerEntityWorldObserver observer = pit12$playerEntityObserver;
        if (observer != null && entity instanceof EntityPlayer) {
            observer.onPlayerAdded((EntityPlayer) entity);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("RETURN"))
    private void pit12$afterEntityRemoved(Entity entity, CallbackInfo ci) {
        PlayerEntityWorldObserver observer = pit12$playerEntityObserver;
        if (observer != null && entity instanceof EntityPlayer) {
            observer.onPlayerRemoved((EntityPlayer) entity);
        }
    }
}
