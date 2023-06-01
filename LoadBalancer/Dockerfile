FROM golang:1.20rc1
RUN apt-get update && apt-get install -y libczmq-dev
ADD ./go.mod /go/src/
ADD ./load-balancer.go /go/src/
WORKDIR /go/src/
RUN go mod tidy
RUN go build -o /go/bin/load-balancer /go/src/load-balancer.go
EXPOSE 8080 30216
CMD [ "/go/bin/load-balancer" ]