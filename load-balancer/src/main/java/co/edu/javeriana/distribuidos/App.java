package co.edu.javeriana.distribuidos;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException, IOException
    {
        LoadBalancer LB=new LoadBalancer();
        System.out.println("Ready...");
        LB.receive();
    }
}
