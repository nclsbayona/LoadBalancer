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
    // https://zguide.zeromq.org/docs/chapter5/ mentions Router-Dealer
    // See https://zguide.zeromq.org/docs/chapter4/#reliable-request-reply patterns
    private ZContext ctx;
    private ZMQ.Socket frontend_socket;
    private ZMQ.Socket backend_socket;
    private final int SERVICE_PORT = 8080; // The port that LoadBalancer should be receiving requests on
    private final int BIND_PORT = 30216; // The port that LoadBalancer should be using to query servers

    // We can follow a Simple pirate pattern approach and include the heartbeats
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
        try (ZMonitor zMonitor = new ZMonitor(ctx, this.backend_socket);
                ZMonitor zMonitor2 = new ZMonitor(ctx, this.frontend_socket)) {
            // Monitor the backend socket
            zMonitor.verbose(true); // Verbose Monitor
            zMonitor.add(Event.ALL);
            zMonitor.start();
            ZThread.fork(ctx, new Listener(), zMonitor, "Backend Server");
            // Monitor the frontend socket
            zMonitor2.verbose(true); // Verbose Monitor
            zMonitor2.add(Event.ALL);
            zMonitor2.start();
            ZThread.fork(ctx, new Listener(), zMonitor2, "Frontend Server");
            //
            this.frontend_socket.bind("tcp://*:" + String.valueOf(this.SERVICE_PORT));
            this.backend_socket.bind("tcp://*:" + String.valueOf(this.BIND_PORT));
            ZMQ.sleep(2);

            System.out.println("Starting to send messages...");
            int i = 0;
            while (true) {
                /*
                 * When we use a DEALER to talk to a REP socket, we must accurately emulate the
                 * envelope that the REQ socket would have sent, or the REP socket will discard
                 * the message as invalid. So, to send a message, we:
                 * 
                 * Send an empty message frame with the MORE flag set; then
                 * Send the message body.
                 * And when we receive a message, we:
                 * 
                 * Receive the first frame and if it’s not empty, discard the whole message;
                 * (This is made automatically)
                 * Receive the next frame and pass that to the application.
                 */
                String msg = "Hola mundo - " + i++;
                this.backend_socket.sendMore("");
                this.backend_socket.send(msg);
                System.out.println("Sent message");
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
        // I believe that publisher-subscriber is less
        // adequate than request-reply. See Code Connected
        // by Pieter Hientjens "knowledge is distributed
        // and the more static pieces you have, the more
        // effort it is to change the topology." Page 50 is
        // important for understanding this
        // http://hintjens.wdfiles.com/local--files/main%3Afiles/cc1pe.pdf
        this.ctx = new ZContext();
        this.frontend_socket = ctx.createSocket(SocketType.ROUTER);
        this.backend_socket = ctx.createSocket(SocketType.DEALER);
    }
}
