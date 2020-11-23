ARG VERSION=11

FROM openjdk:${VERSION}-jdk as BUILD

COPY . /src
WORKDIR /src
# FIXME testcontainer inside docker
RUN ./gradlew --no-daemon clean build -x test shadowJar

FROM openjdk:${VERSION}-jre

COPY --from=BUILD /src/build/libs/kakaopay-ppurigi-1.0-SNAPSHOT-all.jar /bin/runner/run.jar
WORKDIR /bin/runner

CMD ["java","-jar","run.jar"]