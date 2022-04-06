FROM azul/zulu-openjdk:11.0.14.1-11.54.25

RUN apt-get update && apt-get install -y wget gnupg curl

RUN useradd --uid 3434 --user-group --shell /bin/bash --create-home circleci

USER circleci

