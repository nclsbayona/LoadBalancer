FROM maven:3-jdk-11

RUN apt-get update && apt-get install -y golang libtool pkg-config build-essential autoconf automake libzmq3-dev python3 python3-pip libczmq-dev

WORKDIR /home
RUN wget https://download.libsodium.org/libsodium/releases/libsodium-1.0.17-stable.tar.gz
RUN tar -xvf libsodium-1.0.17-stable.tar.gz
WORKDIR libsodium-stable
RUN ./configure
RUN make && make check
RUN make install 

WORKDIR /home

RUN wget http://download.zeromq.org/zeromq-4.1.4.tar.gz
RUN tar -xvf zeromq-4.1.4.tar.gz
WORKDIR zeromq-4.1.4
RUN ./autogen.sh && ./configure
RUN make install
RUN ldconfig

RUN rm -rf /home
RUN pip install pyzmq
ENTRYPOINT [ "bash" ]