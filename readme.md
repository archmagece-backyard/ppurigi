뿌리기
====

## 개요

카카오톡 대화방의 참여자에게 선물을 뿌리는 기능

### 개발목표

* 경량 마이크로 서비스
* API GW 내에서 동작
* 유동적으로 scale out 가능한 설계

### 사용기술

* kotlin
* ktor
* exposed
* gradle
* mariadb
* docker
* junit4


## 테스트

* 메모리DB 사용 기능단위 테스트
* docker-mariadb 사용하는 CRUD 테스트


## 실행

`docker-compose up`
