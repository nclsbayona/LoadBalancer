package co.edu.javeriana.distribuidos;

//ZeroMQ sockets
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.SocketType;

// Signal handling support
import sun.misc.Signal;

public class WebProxy {
    private ZContext context;
    private Socket subscriber;
    private String topic;

    public void receive() {
        this.startHandlers();
        while (true) {
        }
    }

    WebProxy(String url, int port) {
        try {
            context = new ZContext();
            this.subscriber = context.createSocket(SocketType.SUB);
            this.subscriber.connect("tcp://" + url + ":" + String.valueOf(port));
            this.subscriber.subscribe("".getBytes());
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public void communicateWithPublisher(boolean subscribe) {
        if (subscribe)
            this.subscriber.subscribe(topic.getBytes());
        else {
            this.subscriber.unsubscribe(topic.getBytes());
            context.destroySocket(this.subscriber);
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Proxy...");
                    try {
                        this.communicateWithPublisher(false);
                    } catch (Exception e) {
                    }
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Proxy...");
                    try {
                        this.communicateWithPublisher(false);
                    } catch (Exception e) {
                    }
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }
}