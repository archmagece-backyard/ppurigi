enum class PpooStatusCode(val code: String, val message: String) {
    SUCCESS("P-00", "success"),

    // scatter
    SCATTER_TOKEN_ISSUE_FAIL("S-01", "토큰 발행 실패"),

    // gather
    GATHER_ROLE_FORBIDDEN("G-01", "Owner가 자신의 상품을 받아갈 수 없습니다"),
    GATHER_NOT_EXISTS("G-02", "요청한 이벤트가 없습니다. 토큰과 접근권한을 확인 해 주세요."),
    GATHER_EXPIRES("G-03", "뿌린 건은 10분간만 유효합니다."),
    GATHER_DUPLICATE("G-04", "같은 사람이 상금을 두 번 받을 수 없습니다."),
    GATHER_FINISHED("G-05", "상금이 다 떨어졌습니다."),

    // inspection
//    INSPECTION_NO_DATA("I-01", "요청하신 데이터가 없습니다"),
    INSPECTION_EXPIRES("I-02", "뿌린 건에 대한 조회는 7일 동안 할 수 있습니다"),
    INSPECTION_ROLE_FORBIDDEN("I-03", "뿌린 사람 자신만 조회를 할 수 있습니다."),

    FAIL("E-01", "fail"),

    UNKNOWN("Z-99", "unknown error"),
}

class PpooStatusException(val statusCode: PpooStatusCode) : Exception(statusCode.message)
