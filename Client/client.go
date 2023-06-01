package main

import (
	"fmt"
	"math/rand"
	"os"
	"os/signal"
	"time"

	zmq "github.com/pebbe/zmq4"
)

const (
	frontend_url  = "load-balancer"
	frontend_port = 8080
)

var zmq_context *zmq.Context
var client *zmq.Socket
var err error

func listenAndCancel(c chan os.Signal) {
	oscall := <-c
	fmt.Println("Client received: " + oscall.String())
	os.Exit(0)
}

func init() {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Kill)
	signal.Notify(c, os.Interrupt)
	// Goroutine (Concurrent routine) to stop the server
	go listenAndCancel(c)
	//
	zmq_context, err = zmq.NewContext()
	for err != nil {
		zmq_context, err = zmq.NewContext()
	}
	frontend_dir := fmt.Sprintf("tcp://%s:%d", frontend_url, frontend_port)
	client, err = zmq_context.NewSocket(zmq.REQ)
	for err != nil {
		client, err = zmq_context.NewSocket(zmq.REQ)
	}
	fmt.Println("Connecting to ", frontend_dir)
	err = client.Connect(frontend_dir)
	for err != nil {
		err = client.Connect(frontend_dir)
	}
}

func main() {
	time.Sleep(15 * time.Second)
	defer client.Close()
	defer zmq_context.Term()
	fmt.Println("Ready...")
	var reply string
	var messages []string
	messages = make([]string, 0)
	messages = append(messages, "help")
	messages = append(messages, "consult")
	messages = append(messages, "This is just to check everything is fine")
	defer client.Close()
	for {
		time.Sleep(10 * time.Second)
		var msg *string = &messages[rand.Int()%len(messages)]
		fmt.Println("Sending: " + *msg)
		client.Send(*msg, zmq.DONTWAIT)
		reply, err = client.Recv(0)
		for err != nil {
			reply, err = client.Recv(zmq.DONTWAIT)
		}
		fmt.Printf("Received: %s\n", reply)
	}
}
