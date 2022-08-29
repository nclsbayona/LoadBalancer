package com.mycompany.app.Task4;

import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

//
//  Weather update server in Java
//  Binds PUB socket to tcp://*:5556
//  Publishes random weather updates
//
public class wuserver
{
    public static void main(String[] args) throws Exception
    {
        //  Prepare our context and publisher
        try (ZContext context = new ZContext()) {
            ZMQ.Socket publisher = context.createSocket(SocketType.PUB);
            publisher.bind("tcp://*:5556");
            publisher.bind("ipc://weather");

            //random number generator for client 
            Random ranNum = new Random(System.currentTimeMillis());
            
            int[] postal1 = new int[10]; //array size 10
            for (int i=0; i<postal1.length; i++)
            {
                postal1[i] = ranNum.nextInt(999)+1000; //Ranges from numbers 1000-9999 number range
                System.out.println(postal1[i]);
            }


            while (!Thread.currentThread().isInterrupted()) 
            {
                //Given code 
                int postalCode, temperature, relhumidity;
                postalCode = 10000 + ranNum.nextInt(10000);
                temperature = ranNum.nextInt(215) - 80 + 1;
                relhumidity = ranNum.nextInt(50) + 10 + 1;

                //  Send message to all subscribers
                String update = String.format(
                    "%05d %d %d", postalCode, temperature, relhumidity
                );
                publisher.send(update, 0);
            }
        }
    }
}