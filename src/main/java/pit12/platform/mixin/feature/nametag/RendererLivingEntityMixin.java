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
package pit12.platform.mixin.feature.nametag;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pit12.feature.nametag.NametagNamesBinding;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin implements NametagNamesBinding {
    @Unique
    private Function<UUID, String> pit12$nametagSuffix;

    @Override
    public void pit12$bindNametagNames(Function<UUID, String> suffix) {
        pit12$nametagSuffix = suffix;
    }

    @Override
    public String pit12$nametagSuffix(UUID playerId) {
        return pit12$nametagSuffix == null ? "" : pit12$nametagSuffix.apply(playerId);
    }
}
