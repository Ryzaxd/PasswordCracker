package util;

public class StringUtilities {


    public static String capitalize(final String str) {
        if (str == null) {
            throw new IllegalArgumentException("str is null");
        }
        if (str.isEmpty()) {
            return str;
        }
        final String firstLetterUppercase = str.substring(0, 1).toUpperCase();
        final String theRest = str.substring(1);
        return firstLetterUppercase + theRest;
    }
}
