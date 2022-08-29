package co.edu.javeriana.distribuidos;

// ZeroMQ imports
import org.zeromq.ZMQ;
import org.zeromq.ZMonitor;
import org.zeromq.ZPoller;
import org.zeromq.ZSocket;
import org.zeromq.ZThread;
import org.zeromq.ZMonitor.Event;
import org.zeromq.ZMonitor.ZEvent;
import org.zeromq.ZThread.IAttachedRunnable;
import org.zeromq.ZContext;
import org.zeromq.SocketType;

// Socket management
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

// Signal handling support
import sun.misc.Signal;
import zmq.util.function.BiFunction;

// Thread management
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Use time units to define an exit normal time
import java.util.concurrent.TimeUnit;

//Estas importaciones se realizan para poder leer el flujo de datos que se intenta enviar por el socket
import java.io.BufferedReader;
import java.io.InputStreamReader;

//Estas importaciones se realizan para poder escribir em el flujo de salida los datos que se recuperaron de la solicitud
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/*
 class Attend implements Runnable {

    private Socket client;
    private ZMQ.Socket balancing_socket;
    private final static String TOPIC = "WebProxy";

    Attend(Socket c, ZMQ.Socket balancing_socket) {
        this.client = c;
        this.balancing_socket = balancing_socket;
    }

    @Override
    public void run() {
        this.attend();
    }

    public void attend() {
        // this.client has the client socket
        // this.balancing_socket has the LB socket that must perform the queries
        String msg = "";
        String line;
        try {
            BufferedReader reading = new BufferedReader(new InputStreamReader(client.getInputStream()));
            boolean end = false;
            while (!end) {
                // Read all data client is sending
                line = reading.readLine();
                // If line is empty, end, else add it to the message
                if (line == null || line.length() == 0 || line.equals(""))
                    end = true;
                else {
                    // If message is empty, just add line, else, add line in a newlinw
                    msg += (msg.length() > 0) ? "\r\n" + line : line;
                }
            }
        } catch (Exception e) {

        }
        // Send topic before
        if (balancing_socket.send(TOPIC.getBytes(), ZMQ.SNDMORE | ZMQ.DONTWAIT))
            System.out.println("\nMessage sent " + msg + ": " + balancing_socket.send(msg.getBytes(), ZMQ.DONTWAIT));
    }
}

public class LoadBalancer {
    // Thread management
    private final ExecutorService ES = Executors.newCachedThreadPool();
    // LoadBalancer port
    private int port;
    // In order to use Round-Robin, we can use DEALER SocketType
    // https://github.com/zeromq/jeromq/blob/master/src/main/java/org/zeromq/SocketType.java.
    // https://zguide.zeromq.org/docs/chapter5/ mentions Router-Dealer
    private ZMQ.Socket balancing_socket;
    // Load Balancer socket
    private ServerSocket server_socket;

    public void receive() {
        this.startHandlers();
        try (ZContext ctx = new ZContext()) { // To auto close executing threads
            this.balancing_socket = ctx.createSocket(SocketType.PUB);
            final int bind_port = 30216; // Greater than 1024
            System.out.println("Binding to -> tcp://*:" + String.valueOf(bind_port));
            this.balancing_socket.bind("tcp://*:" + String.valueOf(bind_port));
            while (true) {
                // Accept a client trying to access a resource, immediately redirect the query,
                // so LoadBalancer can still work
                Socket client = this.server_socket.accept();
                try {
                    client.setKeepAlive(true);
                } catch (SocketException e) {
                }
                this.ES.execute(new Attend(client, this.balancing_socket));
            }

        } catch (Exception e) {
            System.exit(1);
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Load Balancer...");
                    this.awaitEnding();
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Load Balancer...");
                    this.awaitEnding();
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }

    private void awaitEnding() {
        // Stop creating new threads
        this.ES.shutdown();
        try {
            // Await for thread termination
            if (!this.ES.awaitTermination(10, TimeUnit.SECONDS))
                throw new Exception("No apaga");
            return;
        } catch (Exception e) {
            // Kill all threads
            this.ES.shutdownNow();
            // End process
            Thread.currentThread().interrupt();
            // Exit with error
            System.exit(1);
        }
    }

    public LoadBalancer(int port) {
        this.port = port;
        try {
            this.server_socket = new ServerSocket(this.port);
            System.out.println("Load balancer on port " + String.valueOf(this.port) + "\n\n");
        } catch (Exception e) {
            System.out.println("Error creating the socket listening for http requests");
        }
    }
}
 */

public class LoadBalancer {
    // In order to use Round-Robin, we can use DEALER SocketType
    // https://github.com/zeromq/jeromq/blob/master/src/main/java/org/zeromq/SocketType.java.
    // https://zguide.zeromq.org/docs/chapter5/ mentions Router-Dealer
    private ZContext ctx = new ZContext();
    private ZMQ.Socket balancing_socket;
    private final int bind_port = 30216; // Greater than 1024
    private final static String[] TOPIC = { "A", "B", "C" };
    private static int index = 0;
    private static final int NUM_SERVERS = 3;

    private static class Listener implements IAttachedRunnable{

        @Override
        public void run(Object[] args, ZContext ctx, org.zeromq.ZMQ.Socket pipe) {
            ZEvent received = ((ZMonitor) args[0]).nextEvent();
            System.out.println("Received event:" + received.code + "- "+received.value+" from: "+received.address );
        }

    }

    public void receive() throws InterruptedException, IOException {
        this.startHandlers();

        try (ZMonitor zMonitor=new ZMonitor(ctx, this.balancing_socket)) {
            zMonitor.verbose(true); //Verbose Monitor
            zMonitor.add(Event.ALL);
            zMonitor.start();
            ZThread.fork(ctx, new Listener(), zMonitor);
            // https://stackoverflow.com/questions/43329436/asynchronous-client-server-using-java-jeromq

            while (true) {
                String topic = TOPIC[index];
                this.balancing_socket.send(topic.getBytes(), ZMQ.SNDMORE);
                this.balancing_socket.send(("Hola Mundo-" + topic).getBytes());
                index = (index + 1) % NUM_SERVERS;
                Thread.sleep(2000);
            }
        }
    }

    public void startHandlers() {
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nSIGINT received. Shutting down Load Balancer...");
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
        Signal.handle(new Signal("TERM"), // SIGTERM
                signal -> {
                    System.out.println("\nSIGTERM received. Shutting down Load Balancer...");
                    System.out.println("Good Bye...");
                    System.exit(0);
                });
    }

    public LoadBalancer() {
        this.balancing_socket = ctx.createSocket(SocketType.PUB);
        this.balancing_socket.bind("tcp://*:" + String.valueOf(this.bind_port));
    }
}

/*
 * Can check this to understand pub-sub
 * package guide;
 * 
 * import java.util.Random;
 * 
 * import org.zeromq.*;
 * import org.zeromq.ZMQ.Socket;
 * import org.zeromq.ZThread.IAttachedRunnable;
 * 
 * // Espresso Pattern
 * // This shows how to capture data using a pub-sub proxy
 * public class espresso
 * {
 * // The subscriber thread requests messages starting with
 * // A and B, then reads and counts incoming messages.
 * private static class Subscriber implements IAttachedRunnable
 * {
 * 
 * @Override
 * public void run(Object[] args, ZContext ctx, Socket pipe)
 * {
 * // Subscribe to "A" and "B"
 * Socket subscriber = ctx.createSocket(SocketType.SUB);
 * subscriber.connect("tcp://localhost:6001");
 * subscriber.subscribe("A".getBytes(ZMQ.CHARSET));
 * subscriber.subscribe("B".getBytes(ZMQ.CHARSET));
 * 
 * int count = 0;
 * while (count < 5) {
 * String string = subscriber.recvStr();
 * if (string == null)
 * break; // Interrupted
 * count++;
 * }
 * ctx.destroySocket(subscriber);
 * }
 * }
 * 
 * // .split publisher thread
 * // The publisher sends random messages starting with A-J:
 * private static class Publisher implements IAttachedRunnable
 * {
 * 
 * @Override
 * public void run(Object[] args, ZContext ctx, Socket pipe)
 * {
 * Socket publisher = ctx.createSocket(SocketType.PUB);
 * publisher.bind("tcp://*:6000");
 * Random rand = new Random(System.currentTimeMillis());
 * 
 * while (!Thread.currentThread().isInterrupted()) {
 * String string = String.format("%c-%05d", 'A' + rand.nextInt(10),
 * rand.nextInt(100000));
 * if (!publisher.send(string))
 * break; // Interrupted
 * try {
 * Thread.sleep(100); // Wait for 1/10th second
 * }
 * catch (InterruptedException e) {
 * }
 * }
 * ctx.destroySocket(publisher);
 * }
 * }
 * 
 * // .split listener thread
 * // The listener receives all messages flowing through the proxy, on its
 * // pipe. In CZMQ, the pipe is a pair of ZMQ_PAIR sockets that connect
 * // attached child threads. In other languages your mileage may vary:
 * private static class Listener implements IAttachedRunnable
 * {
 * 
 * @Override
 * public void run(Object[] args, ZContext ctx, Socket pipe)
 * {
 * // Print everything that arrives on pipe
 * while (true) {
 * ZFrame frame = ZFrame.recvFrame(pipe);
 * if (frame == null)
 * break; // Interrupted
 * frame.print(null);
 * frame.destroy();
 * }
 * }
 * }
 * 
 * // .split main thread
 * // The main task starts the subscriber and publisher, and then sets
 * // itself up as a listening proxy. The listener runs as a child thread:
 * public static void main(String[] argv)
 * {
 * try (ZContext ctx = new ZContext()) {
 * // Start child threads
 * ZThread.fork(ctx, new Publisher());
 * ZThread.fork(ctx, new Subscriber());
 * 
 * Socket subscriber = ctx.createSocket(SocketType.XSUB);
 * subscriber.connect("tcp://localhost:6000");
 * Socket publisher = ctx.createSocket(SocketType.XPUB);
 * publisher.bind("tcp://*:6001");
 * Socket listener = ZThread.fork(ctx, new Listener());
 * ZMQ.proxy(subscriber, publisher, listener);
 * 
 * System.out.println(" interrupted");
 * 
 * // NB: child threads exit here when the context is closed
 * }
 * }
 * }
 * https://www.youtube.com/watch?v=glp3I3Ycl9k Front and back in spring
 */
