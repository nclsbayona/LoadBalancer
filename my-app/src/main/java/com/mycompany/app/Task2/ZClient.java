package com.mycompany.app.Task2;

//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.Scanner; 

public class ZClient
{
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            System.out.println("Connecting to Hello Hamza's server");

      		//  Socket to talk to server
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:5555");

            Scanner reqNum = new Scanner(System.in); //the number to calculate up to 
            System.out.println("Enter Number to calculate Fib Series up to: ");
            
            String request = reqNum.nextLine(); 
            System.out.println("Your Sending Number is: "+request); //Sending to server for calculation 
            
            socket.send(request.getBytes(ZMQ.CHARSET), 0);

            byte[] reply = socket.recv(0);

            //int recievedNum = Integer.parseInt(new String (reply,ZMQ.CHARSET)); 
            System.out.println("Fibbonaci Sequence Received: 0," + new String(reply, ZMQ.CHARSET));
            
        }
    }
}