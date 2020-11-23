enum class PpooStatusCode(val code: String, val message: String) {
    SUCCESS("P-00", "success"),

    TOKEN_ISSUE_FAIL("S-01", "토큰 발행 실패"),

    GATHER_ROLE_FORBIDDEN("P-03", "Owner가 자신의 상품을 받아갈 수 없습니다"),
    GATHER_NOT_EXISTS("P-03", "요청한 이벤트가 없습니다. 토큰과 접근권한을 확인 해 주세요."),
    GATHER_EXPIRES("P-03", "뿌린 건은 10분간만 유효합니다."),
    GATHER_DUPLICATE("P-03", "같은 사람이 상금을 두 번 받을 수 없습니다."),
    GATHER_FINISHED("P-03", "상금이 다 떨어졌습니다."),

    INSPECTION_NO_DATA("P-03", "요청하신 데이터가 없습니다"),
    INSPECTION_EXPIRES("I-01", "뿌린 건에 대한 조회는 7일 동안 할 수 있습니다"),
    INSPECTION_ROLE_FORBIDDEN("I-01", "뿌린 사람 자신만 조회를 할 수 있습니다."),

    FAIL("E-01", "fail"),

    UNKNOWN("Z-99", "unknown error"),
}

class AuthorizationException : Exception()

class PpooStatusException(val statusCode: PpooStatusCode) : IllegalArgumentException(statusCode.message)
