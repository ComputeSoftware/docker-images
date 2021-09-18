FROM azul/zulu-openjdk:8u252-8.46.0.19

RUN apt-get update && apt-get install -y wget gnupg curl

RUN apt-get update && apt-get install ssh git curl -y

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.981.sh && \
  chmod +x linux-install-1.10.3.981.sh && \
  ./linux-install-1.10.3.981.sh


RUN apt install \
      build-essential \
      wget \
      unzip \
      apt-transport-https \
      ca-certificates \
      curl \
      gnupg-agent \
      software-properties-common \
      vim \
      -y

# AWS CLI
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
  unzip -q awscliv2.zip && \
  ./aws/install

# Docker CLI
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - && \
  add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) \
    stable" && \
  apt update && \
  apt install docker-ce docker-ce-cli containerd.io -y


RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci
