package com.mycompany.app.Task1;

//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class ZClient
{
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            System.out.println("Connecting to Hello Hamza server");

      		//  Socket to talk to server
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:5555");

            for (int requestNbr = 1; requestNbr != 2; requestNbr++) {
                String request = "Hello";
                System.out.println("Sending Hello " + requestNbr);
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);
                System.out.println(
                    "Received " + new String(reply, ZMQ.CHARSET) + " " +
                    requestNbr
                );
            }
        }
    }
}