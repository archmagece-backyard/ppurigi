뿌리기
====

## 개요

카카오톡 대화방의 참여자에게 선물을 뿌리는 기능


## 기획서 리뷰

* 뿌릴금액 * 뿌릴인원 = 뿌릴총액인가? or 뿌릴금액 / 뿌릴인원 = 1인당금액인가
* 3자리토큰 unique
* 예상 사용자 수
* 비동기 응답이 허용될까


## 작동목표

* 취소기능이 없고 10분간 유효한 기능 - DB저장과 동시에 Redis 시간제한으로 저장
* 사용자에게 즉각적으로 응답을 보여주는 동기식 API
* 조회 기능은 10분 이전에는 DB조회 이후에는 변경이 없는 값으로 조회시 캐시처리


## 사용기술

* kotlin
* ktor
* exposed
* gradle
* redis
* mariadb
* docker
* junit


## 테스트 형태

### 유닛 테스트

* 메모리DB 사용 CRUD 테스트

### 통합 테스트

* 수동실행 (또는 CI)
* docker container 사용

## 실행

```shell script
docker build . -t kakapay-ppurigi
docker run --rm -p 8080:8080 kakaopay-ppurigi
```