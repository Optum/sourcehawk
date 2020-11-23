package com.optum.sourcehawk.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * String Utilities
 *
 * @author Brian Wyka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    /**
     * Determine if a string is blank or empty
     *
     * @param string the string to check
     * @return true if null, empty, or blank
     */
    public static boolean isBlankOrEmpty(final String string) {
        return isEmpty(string) || string.trim().length() == 0;
    }

    /**
     * Determine if a string is not blank or empty
     *
     * @param string the string to check
     * @return true if NOT null, empty, or blank
     */
    public static boolean isNotBlankOrEmpty(final String string) {
        return !isBlankOrEmpty(string);
    }

    /**
     * Determine if the sting is empty (null or has zero length)
     *
     * @param charSequence the string to check
     * @return true if empty, false otherwise
     */
    private static boolean isEmpty(final CharSequence charSequence) {
        return (charSequence == null || charSequence.length() == 0);
    }

    /**
     * Determine if a string starts with the provided prefix
     *
     * @param subject the subject to check for prefix
     * @param prefix the sequence to check for at beginning
     * @return true if starts with prefix, false otherwise
     */
    public static boolean startsWith(final CharSequence subject, final CharSequence prefix) {
        if (isEmpty(subject) || isEmpty(prefix)) {
            return false;
        }
        return subject.toString().startsWith(prefix.toString());
    }

    /**
     * Determine if two character sequences are equal to each other
     *
     * @param charSequenceOne character sequence one
     * @param charSequenceTwo character sequence two
     * @return true if equal, false otherwise
     */
    public static boolean equals(final CharSequence charSequenceOne, final CharSequence charSequenceTwo) {
        if (charSequenceOne == charSequenceTwo) {
            return true;
        } else if (charSequenceOne != null && charSequenceTwo != null) {
            if (charSequenceOne.length() != charSequenceTwo.length()) {
                return false;
            } else if (charSequenceOne instanceof String && charSequenceTwo instanceof String) {
                return charSequenceOne.equals(charSequenceTwo);
            }
            val length = charSequenceOne.length();
            for (int i = 0; i < length; ++i) {
                if (charSequenceOne.charAt(i) != charSequenceTwo.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Remove new lines and carriage returns
     *
     * @param string the string to remove from
     * @return the modified string with new lines and carriage returns removed
     */
    public static String removeNewLines(final String string) {
        if (isEmpty(string)) {
            return string;
        } else if (string.length() == 1) {
            val firstCharacter = string.charAt(0);
            return firstCharacter != '\r' && firstCharacter != '\n' ? string : "";
        }
        val lastIndex = string.length() - 1;
        val last = string.charAt(lastIndex);
        if (last == '\n') {
            if (string.charAt(lastIndex - 1) == '\r') {
                return string.substring(0, lastIndex - 1);
            }
        } else if (last != '\r') {
            return string.substring(0, lastIndex + 1);
        }
        return string.substring(0, string.length() - 1);
    }

    /**
     * Determine whether or not the string provided is a URL
     *
     * @param url the string to check
     * @return true if a URL, false otherwise
     */
    public static boolean isUrl(final String url) {
        if (isBlankOrEmpty(url)) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * <p>Returns either the passed in String, or if the String is
     * <code>null</code>, the value of <code>defaultStr</code>.</p>
     *
     * <pre>
     * StringUtils.defaultString(null, "NULL")  = "NULL"
     * StringUtils.defaultString("", "NULL")    = ""
     * StringUtils.defaultString("bat", "NULL") = "bat"
     * </pre>
     *
     * @see String#valueOf(Object)
     * @param str  the String to check, may be null
     * @param defaultStr  the default String to return
     *  if the input is <code>null</code>, may be null
     * @return the passed in String, or the default if it was <code>null</code>
     */
    public static String defaultString(final String str, final String defaultStr){
        return str == null ? defaultStr : str;
    }

}
