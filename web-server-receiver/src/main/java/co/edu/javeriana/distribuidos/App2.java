package co.edu.javeriana.distribuidos;

/**
 * Hello world!
 *
 */
public class App2 {
    public static void main(String... args) {
        WebProxy2 WP = new WebProxy2("192.168.10.29", 30216);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Ready to accept messages");
        WP.receive();
    }
}