FROM azul/zulu-openjdk:11.0.7-11.39.15

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN apt-get update && apt-get install ssh git curl -y

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.561.sh
RUN chmod +x linux-install-1.10.1.561.sh
RUN ./linux-install-1.10.1.561.sh

RUN apt install build-essential wget unzip -y
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci