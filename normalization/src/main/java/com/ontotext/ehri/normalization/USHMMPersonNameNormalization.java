package com.ontotext.ehri.normalization;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class USHMMPersonNameNormalization {

    // combining characters to be deleted
    private static final Pattern COMBINING_CHARACTER = Pattern.compile("[" +
            "'’`" +                                         // apostrophes
            "\\p{InSpacingModifierLetters}" +               // http://www.unicode.org/charts/PDF/U02B0.pdf
            "\\p{InCombiningDiacriticalMarks}" +            // http://www.unicode.org/charts/PDF/U0300.pdf
            "\\p{InCombiningDiacriticalMarksSupplement}" +  // http://www.unicode.org/charts/PDF/U1DC0.pdf
            "\\p{InCombiningMarksforSymbols}" +             // http://www.unicode.org/charts/PDF/U20D0.pdf
            "\\p{InCombiningHalfMarks}" +                   // http://www.unicode.org/charts/PDF/UFE20.pdf
            "]");

    // punctuation characters to be replaces with space
    private static final Pattern PUNCTUATION_CHARACTER = Pattern.compile("\\p{IsPunct}");

    // sequence of spaces to be squashed
    private static final Pattern SPACE_SEQUENCE = Pattern.compile("\\s+");

    private static String transliterate(String str) {
        String[] cyrillic = {"а", "б", "в", "г", "д", "е", "ж", "з", "и", "й", "к", "л", "м", "н", "о",
                "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ь", "ю", "я", "ы", "э"};
        String[] cyrillicToLatin = {"a", "b", "v", "g", "d", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o",
                "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sht", "a", "y", "yu", "ya", "y", "ye"};
        str = StringUtils.replaceEach(str, cyrillic, cyrillicToLatin);
        String[] greek = {"α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο",
                "π", "ρ", "σ", "τ", "υ", "φ", "χ", "ψ", "ω", "ι"};
        String[] greekToLatin = {"a", "v", "g", "d", "e", "z", "i", "th", "i", "k", "l", "m", "n", "x", "o",
                "p", "r", "s", "t", "i", "f", "ch", "ps", "o", "i"};
        str = StringUtils.replaceEach(str, greek, greekToLatin);
        String[] other = {"о", "ƶ", "đ", "і", "ß", "ø", "œ", "е", "æ", "ј", "ð", "і", "ƒ", "ћ", "®", "є",
                "đ", "©", "þ", "ł", "ü", "ö", "ä"};
        String[] otherToLatin = {"o", "g", "th", "i", "ss", "oe", "oe", "e", "ae", "y", "th", "i", "f", "ch", "r", "e",
                "th", "c", "th", "l", "ue", "oe", "ae"};
        str = StringUtils.replaceEach(str, other, otherToLatin);
        return str;
    }

    public static String normalize(String name) {
        String normalizedName = name.toLowerCase();
        normalizedName = normalizedName.replaceAll("\\d", "").replace("–", " ").replace("—", " ").replace("\u00AD", " ");
        normalizedName = transliterate(normalizedName);

        normalizedName = Normalizer.normalize(normalizedName, Normalizer.Form.NFKD);
        normalizedName = COMBINING_CHARACTER.matcher(normalizedName).replaceAll("");
        normalizedName = PUNCTUATION_CHARACTER.matcher(normalizedName).replaceAll(" ");
        normalizedName = SPACE_SEQUENCE.matcher(normalizedName).replaceAll(" ");

        String result = "";
        for (int i = 0; i < normalizedName.length(); ++i) {
            if (((normalizedName.charAt(i) >= 'a') && (normalizedName.charAt(i) <= 'z')) ||
                    normalizedName.charAt(i) == ' ')
                result += normalizedName.charAt(i);
        }

        return WordUtils.capitalize(result.trim());
    }

}
