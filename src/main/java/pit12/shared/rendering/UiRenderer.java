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
package pit12.shared.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class UiRenderer {
    private final Minecraft minecraft;
    protected final FontRenderer fontRenderer;
    private float pixelScale = Float.NaN;

    public UiRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        fontRenderer = minecraft.fontRendererObj;
    }

    public void resize(float pixelScale) {
        this.pixelScale = pixelScale;
    }

    public void rect(int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        float left = snap(x);
        float top = snap(y);
        float right = snap(x + width);
        float bottom = snap(y + height);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        color(color);
        GL11.glBegin(GL11.GL_QUADS);
        try {
            GL11.glVertex2f(left, bottom);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(right, top);
            GL11.glVertex2f(left, top);
        } finally {
            try {
                GL11.glEnd();
            } finally {
                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    public void texture(ResourceLocation texture, int x, int y, int width, int height, int color,
            float rotationDegrees) {
        if (width <= 0 || height <= 0 || Float.isNaN(pixelScale)) {
            return;
        }
        int pixelX = Math.round(x * pixelScale);
        int pixelY = Math.round(y * pixelScale);
        int pixelWidth = Math.max(1, Math.round(width * pixelScale));
        int pixelHeight = Math.max(1, Math.round(height * pixelScale));
        float inverseScale = 1.0F / pixelScale;
        GlStateManager.pushMatrix();
        GlStateManager.scale(inverseScale, inverseScale, 1.0F);
        GlStateManager.translate(pixelX + pixelWidth / 2.0F, pixelY + pixelHeight / 2.0F, 0.0F);
        GlStateManager.rotate(rotationDegrees, 0.0F, 0.0F, 1.0F);
        try {
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1,
                    0);
            minecraft.getTextureManager().bindTexture(texture);
            color(color);
            Gui.drawModalRectWithCustomSizedTexture(-pixelWidth / 2, -pixelHeight / 2, 0.0F, 0.0F,
                    pixelWidth, pixelHeight, pixelWidth, pixelHeight);
        } finally {
            color(0xFFFFFFFF);
            GlStateManager.popMatrix();
        }
    }

    public int fontHeight() {
        return fontRenderer.FONT_HEIGHT;
    }

    private float snap(float coordinate) {
        return Float.isNaN(pixelScale) ? coordinate
                : Math.round(coordinate * pixelScale) / pixelScale;
    }

    private static void color(int color) {
        GlStateManager.color((color >>> 16 & 0xFF) / 255.0F, (color >>> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, (color >>> 24 & 0xFF) / 255.0F);
    }
}
