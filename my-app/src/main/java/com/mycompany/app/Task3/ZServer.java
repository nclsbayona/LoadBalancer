package com.mycompany.app.Task3;

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
   
    try (ZContext context = new ZContext()) 
    {
      //  Socket to talk to clients
      ZMQ.Socket socket = context.createSocket(SocketType.REP);
      socket.bind("tcp://*:5555");
      
      while (!Thread.currentThread().isInterrupted()) 
      {
        byte[] reply = socket.recv(0);
        String recievedString = new String (reply,ZMQ.CHARSET);  
        System.out.println("Received words to switch case: " + recievedString);
        //System.out.println("Received words to switch case: " + new String(reply, ZMQ.CHARSET));
        
        //end server client connection upon user writing close 
        if (recievedString.equals("close"))
        {
          break; 
        }

        else
        {
          String switchedString = ""; 
          for(int i=0; i < recievedString.length(); i++) //decrement so not an infinite loop 
          { 
            char c = recievedString.charAt(i);  

            if(Character.isUpperCase(c))
            {
              switchedString = switchedString + Character.toLowerCase(c); //do it for each letter in input 
            }

            if(Character.isLowerCase(c))
            {
              switchedString = switchedString + Character.toUpperCase(c); //do it for each letter in input 
            }

          }

        String response = switchedString; //store in string  
        socket.send(response.getBytes(ZMQ.CHARSET), 0); //return string as bytes 
        Thread.sleep(1000); //  Do some 'work'
        }
      
      }
   }
  }
}