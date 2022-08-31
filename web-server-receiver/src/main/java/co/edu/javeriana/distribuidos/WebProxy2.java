package co.edu.javeriana.distribuidos;

//ZeroMQ sockets
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.SocketType;

// Signal handling support
import sun.misc.Signal;

public class WebProxy2 {
    private ZContext context;
    private Socket subscriber;

    public void receive() {
        this.startHandlers();
        
        while (true) {
            String receivedString = this.subscriber.recvStr();
            System.out.println("Received message " + receivedString);
        }
    }

    WebProxy2(String url, int port) {
        try {
            context = new ZContext();
            this.subscriber = context.createSocket(SocketType.REP);
            this.subscriber.connect("tcp://" + url + ":" + String.valueOf(port));
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Proxy...");
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Proxy...");
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }
}