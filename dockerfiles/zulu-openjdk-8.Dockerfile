FROM azul/zulu-openjdk:8u252-8.46.0.19

RUN apt-get update && apt-get install -y wget gnupg curl

RUN useradd --uid 3434 --user-group --shell /bin/bash --create-home circleci

USER circleci

