뿌리기
====

## 개요

카카오톡 대화방의 참여자에게 선물을 뿌리는 기능

### Actor

* Owner
* Hunter
* 도둑넘

### 개발목표

* 경량 마이크로 서비스
* API GW 내에서 동작
* 유동적으로 scale out 가능한 설계

### 사용기술

* kotlin
* ktor
* exposed
* gradle
* redis
* mariadb
* docker
* junit


## 테스트

### 유닛 테스트

* 메모리DB 사용 기능단위 테스트
* mariadb 사용하는 CRUD 테스트

### 통합 테스트

CI/CD 과정에서 실행할 수 있는 테스트 \
docker 활용 앱을 멀티 인스턴스로 실행

### 부하 테스트

지원되는 로컬 부하 테스트로


## 실행

`docker-compose up`

`docker-compose scale app 3`
