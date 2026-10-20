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
package pit12.feature.telebow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import pit12.shared.rendering.UiRenderer;

final class TelebowRenderer {
    private final Minecraft minecraft;
    private final TelebowConfig config;
    private final UiRenderer text;
    private final Map<TelebowPath, Mesh> meshes = new IdentityHashMap<>();
    private final List<Mesh> visible = new ArrayList<>();
    private ClippingHelper clipping;
    private Frustum frustum;

    TelebowRenderer(Minecraft minecraft, TelebowConfig config) {
        this.minecraft = minecraft;
        this.config = config;
        text = new UiRenderer(minecraft);
    }

    void render(Collection<TelebowPath> paths, long now) {
        if (minecraft.getRenderViewEntity() == null)
            return;
        long hold = config.holdTime.get() * 1_000_000L;
        long fade = config.fadeTime.get() * 1_000_000L;
        boolean names = config.showNames.get();
        FontRenderer font = minecraft.fontRendererObj;
        RenderManager camera = minecraft.getRenderManager();
        if (frustum == null) {
            // Vanilla refreshes this shared helper before every world pass.
            clipping = ClippingHelperImpl.getInstance();
            frustum = new Frustum(clipping);
        }
        frustum.setPosition(camera.viewerPosX, camera.viewerPosY, camera.viewerPosZ);
        double pixelMargin =
                4.0 / Math.min(Math.abs(clipping.projectionMatrix[0]) * minecraft.displayWidth,
                        Math.abs(clipping.projectionMatrix[5]) * minecraft.displayHeight);
        boolean unicode = font.getUnicodeFlag();
        boolean drawNames = false;
        for (TelebowPath path : paths) {
            float alpha = (path.color >>> 24) / 255F * path.opacity(now, hold, fade);
            if (alpha <= 0 || path.trailCount < 2 && path.count < 2 && !path.hasImpact)
                continue;
            Mesh mesh = meshes.get(path);
            if (mesh == null) {
                mesh = new Mesh(path);
                meshes.put(path, mesh);
            }
            if (mesh.boundsRevision != path.revision) {
                updateBounds(mesh);
                mesh.boundsRevision = path.revision;
            }
            AxisAlignedBB bounds = mesh.bounds;
            // Leave room for the two-pixel lines at the edge of the screen.
            double margin = Math.max(1,
                    (Math.max(Math.abs(bounds.minX - camera.viewerPosX),
                            Math.abs(bounds.maxX - camera.viewerPosX))
                            + Math.max(Math.abs(bounds.minY - camera.viewerPosY),
                                    Math.abs(bounds.maxY - camera.viewerPosY))
                            + Math.max(Math.abs(bounds.minZ - camera.viewerPosZ),
                                    Math.abs(bounds.maxZ - camera.viewerPosZ)))
                            * pixelMargin);
            mesh.geometryVisible = frustum.isBoxInFrustum(bounds.minX - margin,
                    bounds.minY - margin, bounds.minZ - margin, bounds.maxX + margin,
                    bounds.maxY + margin, bounds.maxZ + margin);
            mesh.nameVisible = false;
            if (names && path.hasImpact && Math.round(alpha * 255) >= 4) {
                if (mesh.nameWidth < 0 || mesh.unicode != unicode) {
                    mesh.nameWidth = text.textWidth(path.displayName);
                    mesh.unicode = unicode;
                }
                int offset = (path.count - 1) * 3;
                double x = path.points[offset];
                double y = path.points[offset + 1];
                double z = path.points[offset + 2];
                double dx = x - camera.viewerPosX;
                double dy = y - camera.viewerPosY;
                double dz = z - camera.viewerPosZ;
                mesh.nameScale = 0.02666667F * (float) Math.max(1,
                        Math.min(4, Math.sqrt(dx * dx + dy * dy + dz * dz) / 32));
                double halfWidth = mesh.nameWidth / 2 + 3;
                double height = font.FONT_HEIGHT + 2;
                double radius = mesh.nameScale * Math.sqrt(halfWidth * halfWidth + height * height)
                        + margin;
                mesh.nameVisible = frustum.isBoxInFrustum(x - radius, y + 1.5 - radius, z - radius,
                        x + radius, y + 1.5 + radius, z + radius);
            }
            if (mesh.geometryVisible || mesh.nameVisible) {
                mesh.alpha = alpha;
                visible.add(mesh);
                drawNames |= mesh.nameVisible;
            }
        }
        if (visible.isEmpty())
            return;
        boolean alphaEnabled = drawNames && GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean textureEnabled = drawNames && GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        int boundTexture = drawNames ? GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D) : 0;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
        GL11.glPushMatrix();
        try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_COLOR_MATERIAL);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE,
                    GL11.GL_ZERO);
            GL11.glLineWidth(2F);
            GL11.glTranslated(-camera.viewerPosX, -camera.viewerPosY, -camera.viewerPosZ);
            for (Mesh mesh : visible) {
                TelebowPath path = mesh.path;
                int color = path.color;
                float alpha = mesh.alpha;
                float red = (color >>> 16 & 255) / 255F;
                float green = (color >>> 8 & 255) / 255F;
                float blue = (color & 255) / 255F;
                if (mesh.geometryVisible) {
                    if (mesh.revision != path.revision) {
                        if (mesh.lines == 0)
                            mesh.lines = GLAllocation.generateDisplayLists(1);
                        compile(mesh, path);
                        mesh.revision = path.revision;
                    }
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    if (path.hasImpact) {
                        GL11.glColor4f(red, green, blue, alpha * 0.2F);
                        GL11.glCallList(mesh.beam);
                    }
                    GL11.glColor4f(red, green, blue, alpha);
                    GL11.glCallList(mesh.lines);
                }
                if (mesh.nameVisible) {
                    drawName(path, mesh.nameWidth, mesh.nameScale, font, camera, alpha);
                }
            }
        } finally {
            visible.clear();
            // FontRenderer changes these cached states even though the geometry uses raw GL.
            if (drawNames) {
                GlStateManager.bindTexture(boundTexture);
                if (textureEnabled)
                    GlStateManager.enableTexture2D();
                else
                    GlStateManager.disableTexture2D();
                if (alphaEnabled)
                    GlStateManager.enableAlpha();
                else
                    GlStateManager.disableAlpha();
            }
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            GlStateManager.resetColor();
        }
    }

    private static void updateBounds(Mesh mesh) {
        TelebowPath path = mesh.path;
        int first = path.futureStart * 3;
        double minX = path.points[first], minY = path.points[first + 1],
                minZ = path.points[first + 2];
        double maxX = minX, maxY = minY, maxZ = minZ;
        for (int index = path.futureStart + 1; index < path.count; index++) {
            int offset = index * 3;
            minX = Math.min(minX, path.points[offset]);
            minY = Math.min(minY, path.points[offset + 1]);
            minZ = Math.min(minZ, path.points[offset + 2]);
            maxX = Math.max(maxX, path.points[offset]);
            maxY = Math.max(maxY, path.points[offset + 1]);
            maxZ = Math.max(maxZ, path.points[offset + 2]);
        }
        for (int index = 0; index < path.trailCount; index++) {
            int offset = index * 3;
            minX = Math.min(minX, path.trail[offset]);
            minY = Math.min(minY, path.trail[offset + 1]);
            minZ = Math.min(minZ, path.trail[offset + 2]);
            maxX = Math.max(maxX, path.trail[offset]);
            maxY = Math.max(maxY, path.trail[offset + 1]);
            maxZ = Math.max(maxZ, path.trail[offset + 2]);
        }
        if (path.hasImpact)
            maxY = Math.max(maxY, beamTop(path));
        mesh.bounds =
                new AxisAlignedBB(minX - 0.18, minY, minZ - 0.18, maxX + 0.18, maxY, maxZ + 0.18);
    }

    private static void compile(Mesh mesh, TelebowPath path) {
        GL11.glNewList(mesh.lines, GL11.GL_COMPILE);
        try {
            drawLines(path);
        } finally {
            GL11.glEndList();
        }
        if (!path.hasImpact)
            return;
        int offset = (path.count - 1) * 3;
        double x = path.points[offset];
        double y = path.points[offset + 1];
        double z = path.points[offset + 2];
        if (mesh.beam != 0 && mesh.impactX == x && mesh.impactY == y && mesh.impactZ == z)
            return;
        if (mesh.beam == 0)
            mesh.beam = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(mesh.beam, GL11.GL_COMPILE);
        try {
            drawBeam(path);
        } finally {
            GL11.glEndList();
        }
        mesh.impactX = x;
        mesh.impactY = y;
        mesh.impactZ = z;
    }

    private static void drawLines(TelebowPath path) {
        GL11.glBegin(GL11.GL_LINES);
        try {
            // Separate segments keep server corrections from drawing jumps.
            for (int index = 1; index < path.trailCount; index += 2) {
                int offset = index * 3;
                GL11.glVertex3d(path.trail[offset - 3], path.trail[offset - 2],
                        path.trail[offset - 1]);
                GL11.glVertex3d(path.trail[offset], path.trail[offset + 1], path.trail[offset + 2]);
            }
            for (int index = path.futureStart + 1; index < path.count; index++) {
                int offset = index * 3;
                GL11.glVertex3d(path.points[offset - 3], path.points[offset - 2],
                        path.points[offset - 1]);
                GL11.glVertex3d(path.points[offset], path.points[offset + 1],
                        path.points[offset + 2]);
            }
            if (!path.hasImpact)
                return;
            int offset = (path.count - 1) * 3;
            double x = path.points[offset];
            double y = path.points[offset + 1] + 0.03;
            double z = path.points[offset + 2];
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x, beamTop(path), z);
        } finally {
            GL11.glEnd();
        }
    }

    private static void drawBeam(TelebowPath path) {
        int offset = (path.count - 1) * 3;
        double x = path.points[offset];
        double y = path.points[offset + 1];
        double z = path.points[offset + 2];
        double top = beamTop(path);
        GL11.glBegin(GL11.GL_QUADS);
        try {
            for (int side = 0; side < 4; side++) {
                int next = (side + 1) % 4;
                double ax = side == 0 || side == 3 ? -0.18 : 0.18;
                double az = side < 2 ? -0.18 : 0.18;
                double bx = next == 0 || next == 3 ? -0.18 : 0.18;
                double bz = next < 2 ? -0.18 : 0.18;
                GL11.glVertex3d(x + ax, y, z + az);
                GL11.glVertex3d(x + ax, top, z + az);
                GL11.glVertex3d(x + bx, top, z + bz);
                GL11.glVertex3d(x + bx, y, z + bz);
            }
        } finally {
            GL11.glEnd();
        }
    }

    private static double beamTop(TelebowPath path) {
        return Math.max(path.points[(path.count - 1) * 3 + 1] + 64,
                path.arrow.worldObj.getActualHeight());
    }

    private void drawName(TelebowPath path, int width, float scale, FontRenderer font,
            RenderManager camera, float alpha) {
        int offset = (path.count - 1) * 3;
        double x = path.points[offset];
        double y = path.points[offset + 1];
        double z = path.points[offset + 2];
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x, y + 1.5, z);
            GL11.glRotatef(-camera.playerViewY, 0, 1, 0);
            GL11.glRotatef(minecraft.gameSettings.thirdPersonView == 2 ? -camera.playerViewX
                    : camera.playerViewX, 1, 0, 0);
            GL11.glScalef(-scale, -scale, scale);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0, 0, 0, alpha * 0.5F);
            GL11.glBegin(GL11.GL_QUADS);
            try {
                GL11.glVertex3d(-width / 2 - 3, -2, 0);
                GL11.glVertex3d(-width / 2 - 3, font.FONT_HEIGHT + 2, 0);
                GL11.glVertex3d(width / 2 + 3, font.FONT_HEIGHT + 2, 0);
                GL11.glVertex3d(width / 2 + 3, -2, 0);
            } finally {
                GL11.glEnd();
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.001F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            GlStateManager.resetColor();
            font.drawString(path.displayName, -width / 2, 0,
                    Math.round(alpha * 255) << 24 | 0xFFFFFF);
        } finally {
            GL11.glPopMatrix();
        }
    }

    void remove(TelebowPath path) {
        Mesh mesh = meshes.remove(path);
        if (mesh != null)
            delete(mesh);
    }

    void clear() {
        for (Mesh mesh : meshes.values()) {
            delete(mesh);
        }
        meshes.clear();
        visible.clear();
    }

    private static void delete(Mesh mesh) {
        if (mesh.lines != 0)
            GLAllocation.deleteDisplayLists(mesh.lines);
        if (mesh.beam != 0)
            GLAllocation.deleteDisplayLists(mesh.beam);
    }

    private static final class Mesh {
        final TelebowPath path;
        int lines;
        int beam;
        long revision = -1;
        long boundsRevision = -1;
        AxisAlignedBB bounds;
        boolean geometryVisible;
        boolean nameVisible;
        float nameScale;
        float alpha;
        int nameWidth = -1;
        boolean unicode;
        double impactX;
        double impactY;
        double impactZ;

        Mesh(TelebowPath path) {
            this.path = path;
        }
    }
}
