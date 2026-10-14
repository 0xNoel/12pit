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
package pit12.platform.mixin.input;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.MouseFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import pit12.runtime.input.MouseInputBinding;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin implements MouseInputBinding {
    @Shadow
    private MouseFilter mouseFilterXAxis;
    @Shadow
    private MouseFilter mouseFilterYAxis;
    @Shadow
    private float smoothCamYaw;
    @Shadow
    private float smoothCamPitch;
    @Shadow
    private float smoothCamFilterX;
    @Shadow
    private float smoothCamFilterY;

    @Override
    public void pit12$clearMouseInput() {
        smoothCamYaw = 0;
        smoothCamPitch = 0;
        smoothCamFilterX = 0;
        smoothCamFilterY = 0;
        mouseFilterXAxis.reset();
        mouseFilterYAxis.reset();
    }
}
