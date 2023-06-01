package main

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"os"
	"os/signal"
	"time"

	zmq "github.com/pebbe/zmq4"
)

const (
	frontend_port = 8080
	backend_port  = 30216
	WORKER_READY  = "\001"
)

var load_balancer *LoadBalancer

type LoadBalancer struct {
	zmq_context             *zmq.Context
	frontend_socket         *zmq.Socket
	backend_socket          *zmq.Socket
}

func (load_balancer *LoadBalancer) processRequest(request []string) {
	var err error
	_, err = load_balancer.backend_socket.SendMessage(request)
	for err != nil {
		fmt.Println("Error sending message to backend", err.Error())
		_, err = load_balancer.backend_socket.SendMessage(request)
	}
	reply, err := load_balancer.backend_socket.RecvMessage(0)
	for err != nil {
		fmt.Println("Error receiving message from backend", err.Error())
		reply, err = load_balancer.backend_socket.RecvMessage(0)
	}
	fmt.Println("Received message:", reply,"From backend")
	time.Sleep(10 * time.Second)
	_, err = load_balancer.frontend_socket.SendMessage(reply)
	for err != nil {
		fmt.Println("Error sending message to frontend", err.Error())
		_, err = load_balancer.frontend_socket.SendMessage(reply)
	}
}

func (load_balancer *LoadBalancer) GetMessages() {
	for {
		request, err := load_balancer.frontend_socket.RecvMessage(0)
		fmt.Println("Received message:", request,"From frontend")
		for err != nil {
			fmt.Println("Error receiving message from frontend", err.Error())
			request, err = load_balancer.frontend_socket.RecvMessage(0)
		}
		go load_balancer.processRequest(request)
	}
}

func (load_balancer *LoadBalancer) Close() {
	load_balancer.frontend_socket.Close()
	load_balancer.backend_socket.Close()
	load_balancer.zmq_context.Term()
}

func (load_balancer *LoadBalancer) Monitor(addr string) {
	var err error
	monitor, err := load_balancer.zmq_context.NewSocket(zmq.PAIR)
	for err != nil {
		fmt.Println("Error creating monitor", err.Error())
		monitor, err = load_balancer.zmq_context.NewSocket(zmq.PAIR)
	}
	err = monitor.Connect(addr)
	for err != nil {
		fmt.Println("Error connecting monitor", err.Error())
		err = monitor.Connect(addr)
	}
	fmt.Println("Monitoring", addr)
	for {
		var myfirstint int64
		event, err := monitor.RecvMessageBytes(0)
		for err != nil {
			fmt.Println("Error receiving message from monitor", err.Error())
			event, err = monitor.RecvMessageBytes(0)
		}
		myfirstint, err = binary.ReadVarint(bytes.NewBuffer(event[0]))
		fmt.Println("happened ", zmq.Event(myfirstint), " on ", string(event[1]))
	}
}

func (load_balancer *LoadBalancer) Init(frontend_port int, backend_port int) {
	var err error
	load_balancer.zmq_context, err = zmq.NewContext()
	for err != nil {
		fmt.Println("Error creating context for ZeroMQ", err.Error())
		load_balancer.zmq_context, err = zmq.NewContext()
	}
	frontend_dir := fmt.Sprintf("tcp://*:%d", frontend_port)
	backend_dir := fmt.Sprintf("tcp://*:%d", backend_port)
	backend_monitor_addr := "inproc://monitor.blb"
	frontend_monitor_addr := "inproc://monitor.flb"
	fmt.Println("Load Balancer creating sockets...")
	load_balancer.frontend_socket, err = load_balancer.zmq_context.NewSocket(zmq.ROUTER)
	for err != nil {
		fmt.Println("Error creating frontend socket", err.Error())
		load_balancer.frontend_socket, err = load_balancer.zmq_context.NewSocket(zmq.ROUTER)
	}
	load_balancer.backend_socket, err = load_balancer.zmq_context.NewSocket(zmq.DEALER)
	for err != nil {
		fmt.Println("Error creating backend socket", err.Error())
		load_balancer.backend_socket, err = load_balancer.zmq_context.NewSocket(zmq.DEALER)
	}
	go load_balancer.backend_socket.Monitor(backend_monitor_addr, zmq.EVENT_ACCEPTED|zmq.EVENT_CLOSED|zmq.EVENT_DISCONNECTED|zmq.EVENT_MONITOR_STOPPED|zmq.EVENT_CONNECTED|zmq.EVENT_HANDSHAKE_SUCCEEDED)
	go load_balancer.frontend_socket.Monitor(frontend_monitor_addr, zmq.EVENT_ACCEPTED|zmq.EVENT_CLOSED|zmq.EVENT_DISCONNECTED|zmq.EVENT_MONITOR_STOPPED|zmq.EVENT_CONNECTED|zmq.EVENT_HANDSHAKE_SUCCEEDED)
	go load_balancer.Monitor(backend_monitor_addr)
	go load_balancer.Monitor(frontend_monitor_addr)
	<-time.After(5 * time.Second)
	fmt.Println("Load Balancer binding sockets...")
	err = load_balancer.frontend_socket.Bind(frontend_dir)
	for err != nil {
		fmt.Println("Error binding frontend socket", err.Error())
		err = load_balancer.frontend_socket.Bind(frontend_dir)
	}
	err = load_balancer.backend_socket.Bind(backend_dir)
	for err != nil {
		fmt.Println("Error binding backend socket", err.Error())
		err = load_balancer.backend_socket.Bind(backend_dir)
	}
}

func unwrap(msg []string) (head string, tail []string) {
	head = msg[0]
	if len(msg) > 1 && msg[1] == "" {
		tail = msg[2:]
	} else {
		tail = msg[1:]
	}
	return
}

func listenAndCancel(c chan os.Signal) {
	oscall := <-c
	fmt.Println("Load Balancer received: " + oscall.String())
	os.Exit(0)
}

func init() {
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Kill)
	signal.Notify(c, os.Interrupt)
	// Goroutine (Concurrent routine) to stop the server
	go listenAndCancel(c)
	//
	load_balancer = new(LoadBalancer)
	fmt.Println("Load Balancer starting...")
	load_balancer.Init(frontend_port, backend_port)
	fmt.Println("Load Balancer up...")
}

func main() {
	fmt.Println("Ready")
	defer load_balancer.Close()
	load_balancer.GetMessages()
}
