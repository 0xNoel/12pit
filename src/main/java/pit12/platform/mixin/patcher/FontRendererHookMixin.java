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
package pit12.platform.mixin.patcher;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.hooks.FontRendererHook", remap = false)
public abstract class FontRendererHookMixin {
    @Shadow
    @Final
    private FontRenderer fontRenderer;

    // PolyPatcher's display-list key omits the font mode.
    @ModifyArg(method = "renderStringAtPos", at = @At(value = "INVOKE",
            target = "Lclub/sk1er/patcher/util/enhancement/hash/StringHash;<init>(Ljava/lang/String;FFFFZ)V"),
            index = 0, remap = false, require = 1)
    private String pit12$fontCacheKey(String text) {
        return (fontRenderer.getUnicodeFlag() ? "\u0001" : "\u0000") + text;
    }
}
