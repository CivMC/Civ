package net.minelink.ctplus.util;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.*;

public class SimpleRegexSet {
    private final ImmutableSet<String> literals;
    private final ImmutableList<Pattern> patterns;

    public SimpleRegexSet(ImmutableSet<String> literals, ImmutableList<Pattern> patterns) {
        this.literals = checkNotNull(literals, "Null literals");
        this.patterns = checkNotNull(patterns, "Null patterns");
    }

    public boolean containsExactly(String str) {
        return literals.contains(checkNotNull(str, "Null string").toLowerCase());
    }

    public boolean contains(String str) {
        return containsExactly(str) || doesAnyPatternMatch(str);
    }

    public boolean doesAnyPatternMatch(String str) {
        checkNotNull(str, "Null string");
        for (Pattern pattern : patterns) {
            if (pattern.matcher(str).matches()) return true;
        }
        return false;
    }

    public static String escape(String str) {
        StringBuilder quoted = new StringBuilder(checkNotNull(str, "Null string").length() + 16);
        for (int index = 0; index < str.length(); index++) {
            char c = str.charAt(index);
            switch (c) {
                case '*':
                case '\\':
                    // We need to escape the char!
                    quoted.append('\\');
                    // fall-through
                default:
                    quoted.append(c); // Add the char
            }
        }
        return quoted.toString();
    }

    public static SimpleRegexSet parsePatterns(List<String> patterns) {
        ImmutableSet.Builder<String> literalsBuilder = ImmutableSet.builder();
        ImmutableList.Builder<Pattern> patternsBuilder = ImmutableList.builder();
        final StringBuilder builder = new StringBuilder(64); // 64 is a good maximum size
        for (String pattern : checkNotNull(patterns, "Null patterns")) {
            builder.setLength(0); // Clear previous junk
            boolean literal = true;
            literalCheckGroup:
            for (int index = 0; index < pattern.length(); index++) {
                char c = pattern.charAt(index);
                switch (c) {
                    case '*':
                        // Its not literal!
                        literal = false;
                        break literalCheckGroup;
                    case '\\': // Escape char
                        c = pattern.charAt(++index);
                        // Fall through to the default handler to interpret the escaped char literaly
                    default:
                        builder.append(Character.toLowerCase(c)); // convert to lowercase for case insensitive comparisons
                }
            }
            if (literal) {
                literalsBuilder.add(builder.toString());
            } else {
                builder.setLength(0); // Its not literal, so start over
                for (int index = 0; index < pattern.length(); index++) {
                    char c = pattern.charAt(index);
                    switch (c) {
                        case '*': // wildcard char
                            builder.append(".*"); // match everything
                            break;
                        case '\\': // Escape the next char
                            c = pattern.charAt(++index);
                            // Fall through to the default handler to interpret the escaped char literally
                        default:
                            if (!Character.isLetterOrDigit(c)) builder.append('\\'); // Non-alphanumeric characters must be escaped, but alphanumeric chracters may not be
                            builder.append(c);
                    }
                }
                patternsBuilder.add(Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE));
            }
        }
        return new SimpleRegexSet(literalsBuilder.build(), patternsBuilder.build());
    }
}
