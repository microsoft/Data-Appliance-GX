FROM gradle:jdk11 AS build

ARG SECURITY

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN if [ -z "$SECURITY" ] ; \
    then \
        gradle -Dsecurity.type=fs runtime:clean runtime:shadowJar --no-daemon ; \
    else \
        gradle runtime:clean runtime:shadowJar --no-daemon ; \
    fi


FROM openjdk:11-jre-slim

ARG VAULT=/etc/dagx/secrets/dagx-vault.properties
ARG KEYSTORE=/etc/dagx/secrets/dagx-test-keystore.jks
RUN mkdir /app

COPY --from=build /home/gradle/src/runtime/build/libs/dagx-runtime.jar /app/dagx-runtime.jar


RUN mkdir -p /etc/dagx


ENTRYPOINT ["java", \
            "-Ddagx.vault=$VAULT", \
            "-Ddagx.keystore=$KEYSTORE", \
            "-Ddagx.keystore.password=test123", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app/dagx-runtime.jar"]

