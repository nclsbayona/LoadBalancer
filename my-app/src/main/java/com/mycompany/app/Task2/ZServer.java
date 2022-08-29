package com.mycompany.app.Task2;

//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class ZServer
{
  public static void main(String[] args) throws Exception
  {
   
    try (ZContext context = new ZContext()) {
      //  Socket to talk to clients
      ZMQ.Socket socket = context.createSocket(SocketType.REP);
      socket.bind("tcp://*:5555");

      while (!Thread.currentThread().isInterrupted()) 
      {
        byte[] reply = socket.recv(0);
        int recievedNum = Integer.parseInt(new String (reply,ZMQ.CHARSET)); 

        System.out.println("Received Fib Number" + ": [" + new String(reply, ZMQ.CHARSET) + "]");
        
        //Declare variables for use in fib and concetenation 
        int n1=0,n2=1,i, n3;    
        String FinalString= "1"; 

        //System.out.print(n1+" "+n2);//printing 0 and 1    
    
        for(i=2;i<recievedNum;++i)//loop starts from 2 because 0 and 1 are already printed    
        {    
          n3=n1+n2;    
          //System.out.println(" "+n3);   //final answer stored in n3 
          n1=n2;    
          n2=n3; 

          FinalString += "," + n3; //contecanating all INTS TO SEND AS STRING 
        }    
        
        String response = FinalString; //parsing n3 back to string to send to client 
        socket.send(response.getBytes(ZMQ.CHARSET), 0);
        Thread.sleep(1000); //  Do some 'work'
      }
    }
  }
}