FROM adoptopenjdk/openjdk8:jdk8u212-b04

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN groupadd --gid 3434 appuser \
  && useradd --uid 3434 --gid appuser --shell /bin/bash --create-home appuser \
  && echo 'appuser ALL=NOPASSWD: ALL' >> /etc/sudoers.d/50-appuser \
  && echo 'Defaults    env_keep += "DEBIAN_FRONTEND"' >> /etc/sudoers.d/env_keep
USER appuser

USER root

# Intel install guide: https://software.intel.com/en-us/articles/installing-intel-free-libs-and-python-apt-repo
# Blog post install guide: http://dirk.eddelbuettel.com/blog/2018/04/15/
RUN apt-get update
RUN apt-get install apt-transport-https -y
RUN wget -O - https://apt.repos.intel.com/intel-gpg-keys/GPG-PUB-KEY-INTEL-SW-PRODUCTS-2019.PUB | apt-key add -
RUN sh -c 'echo deb https://apt.repos.intel.com/mkl all main > /etc/apt/sources.list.d/intel-mkl.list'
RUN apt-get update
RUN apt-get install intel-mkl-64bit-2018.4-057 -y

ENV LD_LIBRARY_PATH /opt/intel/compilers_and_libraries_2018.5.274/linux/mkl/lib/intel64_lin:/opt/intel/compilers_and_libraries_2018.5.274/linux/compiler/lib/intel64_lin

USER appuser