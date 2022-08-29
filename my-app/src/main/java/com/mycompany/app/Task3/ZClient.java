package com.mycompany.app.Task3;

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
        while (true)
        {

        
        try (ZContext context = new ZContext()) 
        {

            System.out.println("Connecting to Hello Hamza's server");
            System.out.println("Type the word to switch its case: ");

      		//  Socket to talk to server
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:5555");

            Scanner reqNum = new Scanner(System.in); //the number to calculate up to 
            String request = reqNum.nextLine(); 
        
                System.out.println("The word to switch case is: "+request); //Sending to server for calculation 
            
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);

                String reply2 = new String(reply, ZMQ.CHARSET);

                //int recievedNum = Integer.parseInt(new String (reply,ZMQ.CHARSET)); 
                System.out.println("The answer recieved after Switching Letters Case: " + reply2);

                //System.out.println("The answer recieved: " + new String(reply, ZMQ.CHARSET));
            
        }
            
        }
    }

}


/* for (int requestNbr = 1; requestNbr != 2; requestNbr++) {
                String request = "Hello";
                System.out.println("Sending Hello " + requestNbr);
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);
                System.out.println(
                    "Received " + new String(reply, ZMQ.CHARSET) + " " +
                    requestNbr
                );*/