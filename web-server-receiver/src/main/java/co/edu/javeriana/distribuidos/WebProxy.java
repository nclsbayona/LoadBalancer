package co.edu.javeriana.distribuidos;

//ZeroMQ sockets
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.SocketType;

// Signal handling support
import sun.misc.Signal;

/* public class WebProxy {
    private ZContext context;
    private Socket subscriber;
    private final static String TOPIC="WebProxy";

    public void receive() {
        this.startHandlers();
        Thread subThread = new Thread() {

            @Override
            public void run() {
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (true) {

                        // get the message
                        String count = WebProxy.this.subscriber.recvStr(0);
                        if (count != null) {
                            System.out.println("Aquí recibi "+count);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        subThread.start();
    }

    WebProxy(String url, int port) {
        try{
            context = new ZContext();
            this.subscriber = context.createSocket(SocketType.SUB);
            this.subscriber.connect("tcp://" + url + ":" + String.valueOf(port));
            this.communicateWithPublisher(true);
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public void communicateWithPublisher(boolean subscribe) {
        if (subscribe)
            this.subscriber.subscribe(TOPIC.getBytes());
        else {
            this.subscriber.unsubscribe(TOPIC.getBytes());
            context.destroySocket(this.subscriber);
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Proxy...");
                    this.communicateWithPublisher(false);
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Proxy...");
                    this.communicateWithPublisher(false);
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }
} */
public class WebProxy {
    private ZContext context;
    private Socket subscriber;
    private final static String TOPIC = "A";

    public void receive() {
        this.startHandlers();
        try {
            Thread.sleep(1000);
            while (true) {
                // get the message
                String count = this.subscriber.recvStr(0);
                if (count != null) {
                    System.out.println("Aquí recibi " + count);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    WebProxy(String url, int port) {
        try {
            context = new ZContext();
            this.subscriber = context.createSocket(SocketType.SUB);
            this.subscriber.connect("tcp://" + url + ":" + String.valueOf(port));
            this.communicateWithPublisher(true);
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public void communicateWithPublisher(boolean subscribe) {
        if (subscribe)
            this.subscriber.subscribe(TOPIC.getBytes());
        else {
            this.subscriber.unsubscribe(TOPIC.getBytes());
            context.destroySocket(this.subscriber);
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Proxy...");
                    this.communicateWithPublisher(false);
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Proxy...");
                    this.communicateWithPublisher(false);
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }
}