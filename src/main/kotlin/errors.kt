enum class PpooStatusCode(val code: String, val message: String) {
    SUCCESS("P-00", "success"),

    TOKEN_ISSUE_FAIL("S-01", "토큰 발행 실패"),

    ROLE_FORBIDDEN("P-03", "Owner가 자신의 상품을 받아갈 수 없습니다"),
    NOT_EXISTS("P-03", "요청한 이벤트가 없습니다. 토큰과 접근권한을 확인 해 주세요."),
    EXPIRES("P-03", "뿌리기 시간초과. 10분 안에만 받을 수 있습니다."),
    DUPLICATE("P-03", "같은 사람이 상금을 두 번 받을 수 없습니다."),
    FINISHED("P-03", "상금이 다 떨어졌습니다."),

    PARAM("E-01", "fail"),

    UNKNOWN("Z-99", "unknown error"),
}

class AuthorizationException : Exception()

class PpooStatusException(val statusCode: PpooStatusCode) : IllegalArgumentException(statusCode.message)
