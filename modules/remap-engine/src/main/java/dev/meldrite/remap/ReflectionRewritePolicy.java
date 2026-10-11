package dev.meldrite.remap;

/** Conservative policy for rewriting reflective internal lookups. */
public final class ReflectionRewritePolicy {
    public enum Decision { SUPPORTED_PATTERN_REWRITTEN, UNSUPPORTED_REFLECTION_PATTERN }

    public Decision decide(String ownerExpression, String memberExpression) {
        if (isConstant(ownerExpression) && isConstant(memberExpression)) {
            return Decision.SUPPORTED_PATTERN_REWRITTEN;
        }
        return Decision.UNSUPPORTED_REFLECTION_PATTERN;
    }

    private static boolean isConstant(String value) {
        return value != null && !value.isBlank() && value.indexOf('+') < 0 && value.indexOf('(') < 0;
    }
}
