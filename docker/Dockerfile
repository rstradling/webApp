# syntax=docker/dockerfile:1

ARG CORRETTO_VERSION=21
ARG BUILD_DIR=/home/webApp
ARG IVY_REPO_DIR=/root/.ivy2

# Install sbt
FROM amazoncorretto:${CORRETTO_VERSION}-alpine3.18 AS sbt-installer
ARG BUILD_DIR
ARG IVY_REPO_DIR
WORKDIR $BUILD_DIR

RUN apk update
RUN apk add curl
RUN apk add bash
RUN apk add gzip
RUN curl -fLo coursier https://github.com/coursier/launchers/raw/master/coursier
RUN chmod +x ./coursier
RUN ./coursier install scala
RUN ./coursier install sbt
RUN ~/.local/share/coursier/bin/scala --version
RUN ~/.local/share/coursier/bin/sbt --version


# Download the dependencies
FROM sbt-installer as poms-dependency-fetcher

ARG BUILD_DIR
ARG IVY_REPO_DIR
WORKDIR $BUILD_DIR

COPY build.sbt ./build.sbt
RUN ~/.local/share/coursier/bin/sbt update

# Build the scala code
FROM sbt-installer as builder
ARG BUILD_DIR
ARG IVY_REPO_DIR
WORKDIR $BUILD_DIR

COPY src ./src
COPY build.sbt ./build.sbt
COPY project ./project

RUN ~/.local/share/coursier/bin/sbt "set offline := true" compile
RUN ~/.local/share/coursier/bin/sbt "set offline := true" test:compile

# Run the tests 
FROM builder AS unit-tests
ARG BUILD_DIR
ARG IVY_REPO_DIR
WORKDIR $BUILD_DIR

RUN ~/.local/share/coursier/bin/sbt "set offline := true" test

# Build the assembly
FROM builder AS assembly
RUN ~/.local/share/coursier/bin/sbt "set offline := true" assembly
RUN cp target/scala-3.3.1/webApp-assembly-0.0.1-SNAPSHOT.jar $BUILD_DIR/webApp.jar

# Run the image
FROM amazoncorretto:${CORRETTO_VERSION}-alpine3.18 AS runner
ARG BUILD_DIR
WORKDIR $BUILD_DIR

RUN mkdir -p /usr/local/bin
WORKDIR /usr/local/bin/
RUN apk add --no-cache --update bash

ENV WEB_JAR=webApp.jar
COPY --from=assembly $BUILD_DIR/$WEB_JAR ./$WEB_JAR

VOLUME /etc/web
EXPOSE 8080

CMD [ "java", "-jar", "webApp.jar", "server"]








