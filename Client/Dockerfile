FROM golang:1.20rc1
RUN apt-get update && apt-get install -y libczmq-dev
ADD ./go.mod /go/src/
ADD ./client.go /go/src/
WORKDIR /go/src/
RUN go mod tidy
RUN go build -o /go/bin/client /go/src/client.go
CMD [ "/go/bin/client" ]