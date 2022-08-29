package co.edu.javeriana.distribuidos;

// ZeroMQ imports
import org.zeromq.ZMQ;
import org.zeromq.ZMonitor;
import org.zeromq.ZThread;
import org.zeromq.ZMonitor.Event;
import org.zeromq.ZMonitor.ZEvent;
import org.zeromq.ZThread.IAttachedRunnable;
import org.zeromq.ZContext;

import java.io.IOException;

import org.zeromq.SocketType;

// Signal handling support
import sun.misc.Signal;

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

    private static class Listener implements IAttachedRunnable {

        @Override
        public void run(Object[] args, ZContext ctx, org.zeromq.ZMQ.Socket pipe) {
            while (true) {
                ZEvent received = ((ZMonitor) args[0]).nextEvent();
                System.out.println(args[1] + " received event: " + received.code + " - " + received.type + " from: "
                        + received.address);
            }
        }

    }

    public void receive() throws IOException {
        this.startHandlers();
        try (ZMonitor zMonitor = new ZMonitor(ctx, this.balancing_socket)) {

            ZMQ.Socket client = ctx.createSocket(SocketType.SUB);
            /*
             * ZMonitor zMonitor2=new ZMonitor(ctx, client);
             * zMonitor2.add(Event.ALL);
             * zMonitor2.start();
             * ZThread.fork(ctx, new Listener(), zMonitor2, "Client");
             */

            //
            zMonitor.verbose(false); // Verbose Monitor
            zMonitor.add(Event.ALL);
            zMonitor.start();
            ZThread.fork(ctx, new Listener(), zMonitor, "Server");
            this.balancing_socket.bind("tcp://*:" + String.valueOf(this.bind_port));
            ZMQ.sleep(2);
            // https://stackoverflow.com/questions/43329436/asynchronous-client-server-using-java-jeromq
            while (true) {
                String topic = TOPIC[index];
                this.balancing_socket.send(topic.getBytes(), ZMQ.SNDMORE);
                this.balancing_socket.send(("Hola Mundo - " + topic).getBytes());
                index = (index + 1) % NUM_SERVERS;
                ZMQ.sleep(1);
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
