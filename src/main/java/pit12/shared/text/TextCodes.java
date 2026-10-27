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
package pit12.shared.text;

public final class TextCodes {
    private TextCodes() {}

    // For unknown reasons, Lunar's vanilla strip doesn't seem to work well for bold.
    public static String strip(String text) {
        if (text.indexOf('§') < 0) {
            return text;
        }
        int length = text.length();
        char[] kept = new char[length];
        int written = 0;
        for (int index = 0; index < length; index++) {
            char current = text.charAt(index);
            if (current == '§' && index + 1 < length && isCode(text.charAt(index + 1))) {
                index++;
                continue;
            }
            kept[written++] = current;
        }
        return new String(kept, 0, written);
    }

    private static boolean isCode(char code) {
        if (code >= '0' && code <= '9') {
            return true;
        }
        char lower = (char) (code | 32);
        return lower >= 'a' && lower <= 'f' || lower >= 'k' && lower <= 'o' || lower == 'r';
    }
}
