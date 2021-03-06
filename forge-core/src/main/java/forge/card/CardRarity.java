/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.card;

public enum CardRarity {
    BasicLand("L"),
    Common("C"),
    Uncommon("U"),
    Rare("R"),
    MythicRare("M"),
    Special("S"), // Timeshifted
    Unknown("?"); // In development

    private final String strValue;

    private CardRarity(final String sValue) {
        this.strValue = sValue;
    }

    @Override
    public String toString() {
        return this.strValue;
    }
    
    public static CardRarity smartValueOf(String input) {
        for(CardRarity r : CardRarity.values()) {
            if( r.name().equalsIgnoreCase(input) || r.strValue.equalsIgnoreCase(input))
                return r;
        }
        return Unknown;
    }
}
