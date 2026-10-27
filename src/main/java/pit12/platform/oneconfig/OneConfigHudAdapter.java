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
package pit12.platform.oneconfig;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.hud.Position;
import cc.polyfrost.oneconfig.hud.Position.AnchorPosition;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.IdentityHashMap;
import java.util.Map;
import pit12.runtime.config.HudAnchor;
import pit12.runtime.config.HudConfig;
import pit12.runtime.config.HudPlacement;
import pit12.runtime.config.IntegerSetting;
import pit12.runtime.hud.HudBounds;
import pit12.runtime.hud.HudElement;
import pit12.runtime.hud.HudRegistry;
import pit12.runtime.hud.HudRenderer;
import pit12.shared.lifecycle.ClientLifecycle;

final class OneConfigHudAdapter implements ClientLifecycle, HudRegistry.Listener {
    // OneConfig uses the same row order for its nine anchors.
    private static final AnchorPosition[] ANCHORS = AnchorPosition.values();
    private final HudRegistry registry;
    private final HudRenderer renderer = new HudRenderer();
    private final Map<HudElement, ElementHud> huds = new IdentityHashMap<>();

    OneConfigHudAdapter(HudRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {
        registry.checkThread();
        registry.setExternalEditor(() -> HudCore.editing);
        registry.addListener(this);
        for (HudElement element : registry.elements()) {
            onRegistered(element);
        }
    }

    @Override
    public void stop() {
        registry.checkThread();
        registry.removeListener(this);
        registry.setExternalEditor(() -> false);
        for (ElementHud hud : huds.values()) {
            hud.registered = false;
            HudCore.huds.remove(hud.key);
        }
        huds.clear();
    }

    @Override
    public void onRegistered(HudElement element) {
        ElementHud hud = new ElementHud(element);
        huds.put(element, hud);
        HudCore.huds.put(hud.key, hud);
    }

    @Override
    public void onUnregistered(HudElement element) {
        ElementHud hud = huds.remove(element);
        hud.registered = false;
        HudCore.huds.remove(hud.key);
    }

    private final class ElementHud extends Hud {
        private final HudElement element;
        private final Map.Entry<Field, Object> key;
        private boolean registered = true;
        // Hud and Position call overrides while their constructors are running.
        private HudBounds bounds;

        ElementHud(HudElement element) {
            super(true);
            this.element = element;
            // OneConfig skips fieldless entries when reloading its profiles.
            key = new SimpleImmutableEntry<>(null, element);
            position = new ElementPosition();
            synchronize();
        }

        @Override
        public boolean isEnabled() {
            // Feature overlay listeners handle normal rendering.
            return registered && HudCore.editing;
        }

        @Override
        protected void preRender(boolean example) {
            synchronize();
        }

        private void synchronize() {
            bounds = HudRenderer.layout(element, UResolution.getScaledWidth(),
                    UResolution.getScaledHeight(), (float) UResolution.getScaleFactor(), true);
            scale = bounds.scale;
            position.setSize(bounds.width, bounds.height);
            position.anchor = ANCHORS[element.config().placement().anchor().id()];
            position.setX(bounds.x);
            position.setY(bounds.y);
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return bounds == null ? 1 : bounds.width;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return bounds == null ? 1 : bounds.height;
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            renderer.render(element, bounds, 0.0F, true);
        }

        @Override
        public void setScale(float scale, boolean example) {
            if (!registered) {
                return;
            }
            float x = position.getX();
            float y = position.getY();
            IntegerSetting setting = element.config().scale();
            int percent = Math.max(setting.minimum(),
                    Math.min(setting.maximum(), Math.round(scale * 100.0F)));
            if (percent == setting.get()) {
                return;
            }
            setting.set(percent);
            synchronize();
            position.setPosition(x, y);
        }

        private final class ElementPosition extends Position {
            ElementPosition() {
                super(ElementHud.this, 0, 0, 1, 1);
            }

            @Override
            public void setPosition(float x, float y, float screenWidth, float screenHeight) {
                super.setPosition(x, y, screenWidth, screenHeight);
                if (bounds == null || !registered) {
                    return;
                }
                int width = Math.round(screenWidth);
                int height = Math.round(screenHeight);
                int originX =
                        Math.max(0, Math.min(Math.max(0, width - bounds.width), Math.round(x)));
                int originY =
                        Math.max(0, Math.min(Math.max(0, height - bounds.height), Math.round(y)));
                if (originX == bounds.x && originY == bounds.y) {
                    return;
                }
                HudConfig config = element.config();
                HudPlacement placement = config.placement();
                HudAnchor anchor = placement.automatic()
                        ? HudPlacement.fromOrigin(originX, originY, bounds.width, bounds.height,
                                width, height).anchor()
                        : placement.anchor();
                config.placement(new HudPlacement(anchor,
                        originX + anchor.elementX(bounds.width) - anchor.screenX(width),
                        originY + anchor.elementY(bounds.height) - anchor.screenY(height),
                        placement.automatic()));
                bounds = bounds.at(originX, originY);
            }
        }
    }
}
