package co.edu.javeriana.distribuidos;

//ZeroMQ sockets
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.SocketType;

// Signal handling support
import sun.misc.Signal;

public class WebServer {
    private ZContext context;
    private Socket subscriber;

    public static void main(String... args) {
        final String url="127.0.0.1";
        final int port=30216;
        WebServer WP = new WebServer(url, port);
        System.out.println("Ready to accept messages");
        WP.receive();
    }

    public void receive() {
        this.startHandlers();
        while (true) {
            String receivedString = this.subscriber.recvStr();
            // Perform task
            System.out.println("Received message " + receivedString);
            String msg="Server responding";
            System.out.println("Should send "+msg);
            //
            this.subscriber.send(msg);
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Server...");
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Server...");
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }

    WebServer(String url, int port) {
        try {
            context = new ZContext();
            this.subscriber = context.createSocket(SocketType.REP);
            this.subscriber.connect("tcp://" + url + ":" + String.valueOf(port));
            System.out.println("Worker ready...");
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }
}