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
import java.util.function.BiFunction;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pit12.feature.nametag.NametagNamesBinding;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin implements NametagNamesBinding {
    @Unique
    private BiFunction<UUID, String, String> pit12$nametagNames;

    @Override
    public void pit12$bindNametagNames(BiFunction<UUID, String, String> names) {
        pit12$nametagNames = names;
    }

    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/IChatComponent;getFormattedText()Ljava/lang/String;"))
    private String pit12$displayName(IChatComponent component, EntityLivingBase entity, double x,
            double y, double z) {
        String original = component.getFormattedText();
        return pit12$nametagNames != null && entity instanceof EntityPlayer
                ? pit12$nametagNames.apply(entity.getUniqueID(), original)
                : original;
    }
}
