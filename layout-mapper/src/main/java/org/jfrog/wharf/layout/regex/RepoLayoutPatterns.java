package org.jfrog.wharf.layout.regex;

import com.google.common.collect.Lists;

import javax.print.DocFlavor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 9/13/11
 * Time: 1:22 PM
 *
 * @author Fred Simon
 */
public class RepoLayoutPatterns {
    public static final String MAVEN_REVISION_PATTERN = "[baseRev<[^/]+?>](-[fileItegRev<SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))>])";
    public static final String MAVEN_2_PATTERN = "[orgPath<.+?>]/[module<[^/]+>]/[baseRev<[^/]+?>](-[folderItegRev<SNAPSHOT>])/" +
            "[module<[^/]+>]-[baseRev<[^/]+?>](-[fileItegRev<SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))>])(-[classifier<(?:(?!\\d))[^\\./]+>]).[ext<[^\\-/]+>]";
    public static final String IVY_REVISION_PATTERN = "[baseRev<[^/]+?>](-[fileItegRev<\\d{14}>])";
    public static final String IVY_PATTERN = "[orgPath<.+?>]/[module<[^/]+>]/[baseRev<[^/]+?>](-[fileItegRev<\\d{14}>])/" +
            "[module<[^/]+>]-[baseRev<[^/]+?>](-[fileItegRev<\\d{14}>])(-[classifier<(?:(?!\\d))[^\\./]+>]).[ext<[^\\-/]+>]";

    private static final Pattern OPTIONAL_AREA_PATTERN = Pattern.compile("\\([^\\(]*\\)");
    private static final Pattern REPLACED_OPTIONAL_TOKEN_PATTERN = Pattern.compile("\\([^\\[\\(]*\\)");
    private static final Pattern CUSTOM_TOKEN_PATTERN = Pattern.compile("<[^<]*>");

    public static String clearCustomTokenRegEx(String path) {
        return CUSTOM_TOKEN_PATTERN.matcher(path).replaceAll("");
    }

    /**
     * Find all optional areas that their token values were provided and remove the "optional" brackets that
     * surround them.
     *
     * @param itemPathTemplate     Item path template to modify
     * @param removeBracketContent True if the content of the optional bracket should be disposed
     * @return Modified item path template
     */
    public static String removeReplacedTokenOptionalBrackets(String itemPathTemplate, boolean removeBracketContent) {
        itemPathTemplate = clearCustomTokenRegEx(itemPathTemplate);
        Matcher matcher = REPLACED_OPTIONAL_TOKEN_PATTERN.matcher(itemPathTemplate);

        int latestGroupEnd = 0;
        StringBuilder newPathBuilder = new StringBuilder();

        while (matcher.find()) {
            int replacedOptionalTokenAreaStart = matcher.start();
            int replacedOptionalTokenAreaEnd = matcher.end();
            String replacedOptionalTokenValue = matcher.group(0);

            newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd, replacedOptionalTokenAreaStart));

            if (!removeBracketContent) {
                newPathBuilder.append(replacedOptionalTokenValue.replaceAll("[\\(\\)]", ""));
            }

            //Path after optional area
            latestGroupEnd = replacedOptionalTokenAreaEnd;
        }

        if ((latestGroupEnd != 0) && latestGroupEnd < itemPathTemplate.length()) {
            newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd));
        }

        if (newPathBuilder.length() == 0) {
            return itemPathTemplate;
        }

        return newPathBuilder.toString();
    }

    /**
     * Find all remaining optional areas that were left with un-replaced tokens and remove them completely
     *
     * @param itemPathTemplate Item path template to modify
     * @return Modified item path template
     */
    public static String removeUnReplacedTokenOptionalBrackets(String itemPathTemplate) {
        itemPathTemplate = clearCustomTokenRegEx(itemPathTemplate);
        Matcher matcher = OPTIONAL_AREA_PATTERN.matcher(itemPathTemplate);

        int latestGroupEnd = 0;
        StringBuilder newPathBuilder = new StringBuilder();

        while (matcher.find()) {
            int optionalAreaStart = matcher.start();
            int optionalAreaEnd = matcher.end();
            String optionalAreaValue = matcher.group(0);

            if (optionalAreaValue.contains("[")) {
                newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd, optionalAreaStart));
                latestGroupEnd = optionalAreaEnd;
            }
        }

        if ((latestGroupEnd != 0) && latestGroupEnd < itemPathTemplate.length()) {
            newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd));
        }

        if (newPathBuilder.length() == 0) {
            return itemPathTemplate;
        }

        return newPathBuilder.toString();
    }

    /**
     * Creates a regular expression based on the given path pattern and layout
     *
     * @param patternToUse       Pattern to translate
     * @param failOnUnknownToken Throw exception if the pattern contains an unknown token (neither reserved nor custom)
     * @return Regular expression of given path
     */
    public static String generateNamedRegexFromLayoutPattern(String patternToUse,
                                                             boolean failOnUnknownToken) {
        List<String> tokenAppearance = Lists.newArrayList();
        StringBuilder itemPathPatternRegExpBuilder = new StringBuilder();

        boolean withinToken = false;
        boolean withinCustomToken = false;
        StringBuilder currentTokenBuilder = new StringBuilder();
        StringBuilder customRegExTokenBuilder = new StringBuilder();
        for (char c : patternToUse.toCharArray()) {
            if (('[' == c) && !withinToken && !withinCustomToken) {
                withinToken = true;
            } else if ((']' == c) && withinToken && !withinCustomToken) {
                withinToken = false;
                String currentToken = currentTokenBuilder.toString();
                currentTokenBuilder.delete(0, currentTokenBuilder.length());
                if (isReservedToken(currentToken)) {
                    itemPathPatternRegExpBuilder.append("(?<").append(currentToken).append(">");
                    if (tokenAppearance.contains(currentToken)) {
                        itemPathPatternRegExpBuilder.append("\\").append(tokenAppearance.indexOf(currentToken) + 1);
                    } else {
                        itemPathPatternRegExpBuilder.append(getTokenRegExp(currentToken));
                        tokenAppearance.add(currentToken);
                    }
                    itemPathPatternRegExpBuilder.append(")");
                } else if (customRegExTokenBuilder.length() != 0) {
                    itemPathPatternRegExpBuilder.append("(?<").append(currentToken).append(">");
                    if (tokenAppearance.contains(currentToken)) {
                        itemPathPatternRegExpBuilder.append("\\").append(tokenAppearance.indexOf(currentToken) + 1);
                    } else {
                        itemPathPatternRegExpBuilder.append(customRegExTokenBuilder.toString());
                        tokenAppearance.add(currentToken);
                    }
                    itemPathPatternRegExpBuilder.append(")");
                    customRegExTokenBuilder.delete(0, customRegExTokenBuilder.length());
                } else {
                    String errorMessage = "The token '[" + currentToken + "]' is unknown. If this is not intended, " +
                            "please verify the token name for correctness or add a mapping for this token using the " +
                            "'[$NAME&lt;REGEXP&gt;]' syntax.";
                    /* TODO: Find a good simple log system independent of anything
                    if (log.isDebugEnabled()) {
                        log.debug("Error occurred while generating regular expressions from the repository layout " +
                                "pattern '{}': {}", patternToUse, errorMessage);
                    }
                    */
                    if (failOnUnknownToken) {
                        throw new IllegalArgumentException(errorMessage);
                    }
                }
            } else if ('<' == c) {
                withinCustomToken = true;
            } else if ('>' == c) {
                withinCustomToken = false;
            } else if (withinCustomToken) {
                customRegExTokenBuilder.append(c);
            } else if (!withinToken) {
                appendNonReservedToken(itemPathPatternRegExpBuilder, Character.toString(c));
            } else {
                currentTokenBuilder.append(c);
            }
        }

        return itemPathPatternRegExpBuilder.toString();
    }

    private static void appendNonReservedToken(StringBuilder itemPathPatternRegExpBuilder,
            String itemPathPatternElement) {
        char[] splitPathPatternElement = itemPathPatternElement.toCharArray();
        for (char elementToken : splitPathPatternElement) {
            //Dot and dash are reserved regular expression characters. Escape them
            if (('-' == elementToken) || ('.' == elementToken)) {
                itemPathPatternRegExpBuilder.append("\\");
            }

            itemPathPatternRegExpBuilder.append(elementToken);

            if ('(' == elementToken) {
                itemPathPatternRegExpBuilder.append("?:");
            }

            //Append the '?' character to the end of the parenthesis - optional group
            if (')' == elementToken) {
                itemPathPatternRegExpBuilder.append("?");
            }
        }
    }

    private static String getTokenRegExp(String currentToken) {
        return null;
    }

    private static boolean isReservedToken(String pathElement) {
        return false;
    }
}
