package com.mycompany.app.Task4;

import java.util.StringTokenizer;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.Scanner; 

//
//  Weather update client in Java
//  Connects SUB socket to tcp://localhost:5556
//  Collects weather updates and finds avg temp in zipcode
//
public class wuclient
{
    public static void main(String[] args)
    {
        
        try (ZContext context = new ZContext()) 
        {
            //  Socket to talk to server
            System.out.println("Collecting updates from weather server");
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://192.168.10.29:5556");

            //Getting user input 

            Scanner sc = new Scanner(System.in);
            System.out.println("Provide a postal code to subscribe to:");
            String filter = sc.nextLine()+ ""; //Change to user input

            //String filter = (args.length > 0) ? args[0] : "10001 "; 
            subscriber.subscribe(filter.getBytes(ZMQ.CHARSET));

            //  Process 100 updates
            int update_nbr;
            long total_temp = 0;
            for (update_nbr = 0; update_nbr < 100; update_nbr++) {
                //  Use trim to remove the tailing '0' character
                String string = subscriber.recvStr(0).trim();
                System.out.println("Just received "+string);
                sc.next();
                StringTokenizer sscanf = new StringTokenizer(string, " ");
                int zipcode = Integer.valueOf(sscanf.nextToken());
                int temperature = Integer.valueOf(sscanf.nextToken());
                int relhumidity = Integer.valueOf(sscanf.nextToken());

                total_temp += temperature;
            }

            System.out.println(
                String.format(
                    "Average temperature for zipcode '%s' was %d.",
                    filter,
                    (int)(total_temp / update_nbr)
                )
            );
        }
        
    }
}
