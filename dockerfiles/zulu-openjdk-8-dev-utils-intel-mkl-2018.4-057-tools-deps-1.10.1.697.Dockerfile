FROM azul/zulu-openjdk:8u252-8.46.0.19

RUN apt-get update
RUN apt-get install -y wget gnupg curl

RUN apt-get update && apt-get install ssh git curl -y

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.697.sh
RUN chmod +x linux-install-1.10.1.697.sh
RUN ./linux-install-1.10.1.697.sh

# Intel install guide: https://software.intel.com/en-us/articles/installing-intel-free-libs-and-python-apt-repo
# Blog post install guide: http://dirk.eddelbuettel.com/blog/2018/04/15/
RUN apt-get update
RUN apt-get install apt-transport-https -y
RUN wget -O - https://apt.repos.intel.com/intel-gpg-keys/GPG-PUB-KEY-INTEL-SW-PRODUCTS-2019.PUB | apt-key add -
RUN sh -c 'echo deb https://apt.repos.intel.com/mkl all main > /etc/apt/sources.list.d/intel-mkl.list'
RUN apt-get update
RUN apt-get install intel-mkl-64bit-2018.4-057 -y

ENV LD_LIBRARY_PATH /opt/intel/compilers_and_libraries_2018.5.274/linux/mkl/lib/intel64_lin:/opt/intel/compilers_and_libraries_2018.5.274/linux/compiler/lib/intel64_lin

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
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip -q awscliv2.zip
RUN ./aws/install

RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
RUN add-apt-repository \
       "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
       $(lsb_release -cs) \
       stable"
RUN apt update
RUN apt install docker-ce docker-ce-cli containerd.io -y

RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci