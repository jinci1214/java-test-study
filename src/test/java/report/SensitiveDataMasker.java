package report;

import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final Pattern JSON_SECRET_PATTERN =
            Pattern.compile(
                    "(?i)(\"(?:password|token)\"\\s*:\\s*\")"
                            + "[^\"]*"
                            + "(\")"
            );

    private static final Pattern AUTHORIZATION_PATTERN =
            Pattern.compile(
                    "(?i)(Authorization\\s*[:=]\\s*)"
                            + "[^,\\r\\n]+"
            );

    private SensitiveDataMasker() {
    }

    public static String mask(String text) {

        if (text == null || text.isBlank()) {
            return text;
        }

        String maskedText =
                JSON_SECRET_PATTERN
                        .matcher(text)
                        .replaceAll("$1***$2");

        return AUTHORIZATION_PATTERN
                .matcher(maskedText)
                .replaceAll("$1***");
    }
}
