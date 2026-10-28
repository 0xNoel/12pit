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

import net.minecraft.client.gui.FontRenderer;

public final class FontWidths {
    private FontWidths() {}

    public static int measure(FontRenderer font, String text) {
        Metrics metrics = font instanceof Metrics ? (Metrics) font : null;
        float width = 0.0F;
        boolean bold = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '§' && index + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(++index));
                if (code == 'l') {
                    bold = true;
                } else if (code != 'k' && code != 'm' && code != 'n' && code != 'o') {
                    // Colors and reset codes clear bold when drawing.
                    bold = false;
                }
                continue;
            }
            if (metrics != null) {
                width += metrics.pit12$charAdvance(character, bold);
            } else {
                int characterWidth = font.getCharWidth(character);
                width += characterWidth;
                if (bold && characterWidth > 0) {
                    width++;
                }
            }
        }
        return Math.round(width);
    }

    public interface Metrics {
        float pit12$charAdvance(char character, boolean bold);
    }
}
