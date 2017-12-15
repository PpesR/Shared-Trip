package utils;

import android.content.res.Resources;

import remm.sharedtrip.R;

/**
 * Created by Mark on 29.11.2017.
 */

public class UtilBase {

    public static final String API_PREFIX = "http://146.185.135.219/api/v1";

    private static final String whitespace_chars =  ""       /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL)
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD
            + "\\u2001" // EM QUAD
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            ;
    /* A \s that actually works for Java’s native character set: Unicode */
    private static final String whitespace_charclass = "["  + whitespace_chars + "]";

    /* A \S that actually works for  Java’s native character set: Unicode */
    private static final String not_whitespace_charclass = "[^" + whitespace_chars + "]";

    public static boolean notNull(Object obj) { return obj != null; }

    public static boolean notNullOrEmpty(String str) { return str != null && str.length() > 0; }

    public static boolean notNullOrWhitespace(String str) { return str != null && str.length() > 0 && !str.matches("^"+whitespace_charclass+"*+$"); }

    public static boolean isNull(Object obj) { return obj == null; }

    public static boolean bothAreNull(Object obj1, Object obj2) {
        return obj1 == null && obj2 == null;
    }

    public static String valueOrNull(String str) { return str==null || str.equals("null") || str.equals("") ? null : str; }

    public static String toNullSafe(String str) { return str == null ? "null" : str; }

    public static String toStringOrNull(Object obj) { return obj == null ? null : obj.toString(); }

    public static String toStringNullSafe(Object obj) { return obj == null ? "null" : obj.toString(); }
}
