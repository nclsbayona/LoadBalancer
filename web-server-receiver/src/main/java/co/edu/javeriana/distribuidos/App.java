package co.edu.javeriana.distribuidos;

import org.zeromq.ZMQ;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String... args) {
        WebProxy WP = new WebProxy("127.0.0.1", 30216);
        ZMQ.sleep(2);
        System.out.println("Ready to accept messages");
        WP.receive();
    }
}
