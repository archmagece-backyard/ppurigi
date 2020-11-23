enum class ErrorCode(val code: String, val message: String) {
    SUCCESS("P-00", "success"),
    NO_AUTH("P-01", "noauth"),
    DUPLICATE("P-02", "duplicate"),
    EXPIRES("P-03", "expires"),

    PARAM("E-01", "fail"),

    UNKNOWN("Z-99", "unknown error"),
}

class AuthorizationException : Exception()