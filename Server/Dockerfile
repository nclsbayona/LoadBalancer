FROM golang:1.20rc1
RUN apt-get update && apt-get install -y libczmq-dev
ADD ./go.mod /go/src/
ADD ./server.go /go/src/
WORKDIR /go/src/
RUN go mod tidy
RUN go build -o /go/bin/server /go/src/server.go
CMD [ "/go/bin/server" ]