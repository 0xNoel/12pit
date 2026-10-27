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
package pit12.runtime.scoreboard;

import net.minecraft.client.gui.FontRenderer;
import pit12.shared.text.TextCodes;

public final class SidebarLine {
    private final String entry;
    private final String text;
    private final int score;
    private String formattedText;

    SidebarLine(String entry, String text, int score) {
        this.entry = entry;
        this.text = text;
        this.score = score;
    }

    SidebarLine(SidebarLine previous, int score) {
        this(previous.entry, previous.text, score);
        formattedText = previous.formattedText;
    }

    public String entry() {
        return entry;
    }

    public String text() {
        return text;
    }

    /** Call on the client thread. This line keeps the first result. */
    public String formattedText(FontRenderer font) {
        if (formattedText == null) {
            String plain = TextCodes.strip(text);
            StringBuilder result = new StringBuilder(plain.length());
            for (int index = 0; index < plain.length(); index++) {
                char character = plain.charAt(index);
                if (font.getCharWidth(character) != 0)
                    result.append(character);
            }
            formattedText = result.toString();
        }
        return formattedText;
    }

    public int score() {
        return score;
    }
}
