FROM gradle:8.0.2-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:17
COPY --from=build /home/gradle/src/build/libs/blockchain.jar blockchain.jar
ENTRYPOINT ["java","-jar","blockchain.jar"]