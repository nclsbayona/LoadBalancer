package co.edu.javeriana.distribuidos;

// ZeroMQ imports
import org.zeromq.ZMQ;
import org.zeromq.ZMonitor;
import org.zeromq.ZThread;
import org.zeromq.ZMonitor.Event;
import org.zeromq.ZMonitor.ZEvent;
import org.zeromq.ZThread.IAttachedRunnable;
import org.zeromq.ZContext;
import org.zeromq.ZAuth;

import java.io.IOException;
import java.util.ArrayList;
import org.zeromq.SocketType;

// Signal handling support
import sun.misc.Signal;

public class LoadBalancer {
    // In order to use Round-Robin, we can use DEALER SocketType
    // https://zguide.zeromq.org/docs/chapter5/ mentions Router-Dealer
    // See https://zguide.zeromq.org/docs/chapter4/#reliable-request-reply patterns
    private String[] servers;
    private ZAuth actor;
    private ZContext ctx;
    private ZMQ.Socket frontend_socket;
    private ZMQ.Socket backend_socket;
    private final int SERVICE_PORT = 8080; // The port that LoadBalancer should be receiving requests on
    private final int BIND_PORT = 30216; // The port that LoadBalancer should be using to query servers

    public static void main(String[] args) throws IOException {
        String serverIps[] = { "192.168.122.253", "127.0.0.1" };
        String clientIps[] = { "192.168.122.252" };
        LoadBalancer LB = new LoadBalancer(clientIps, serverIps);
        System.out.println("Ready...");
        LB.receiveAndSend();
    }

    private static class Listener implements IAttachedRunnable {

        @Override
        public void run(Object[] args, ZContext ctx, org.zeromq.ZMQ.Socket pipe) {
            while (true) {
                ZEvent received = ((ZMonitor) args[0]).nextEvent();
                String type = String.valueOf(args[1]);
                System.out.println(type + " received event: " + received.code + " - " + received.type + " from: "
                        + received.address);
            }
        }
    }

    private static class ReplySender implements IAttachedRunnable {

        @Override
        public void run(Object[] args, ZContext ctx, org.zeromq.ZMQ.Socket pipe) {
            // In order to communicate between ROUTER and REQ Sockets, It's neccessary to
            // send the ID first
            byte[] id = (byte[]) (args[0]); // Get the ID of the REQ socket
            byte[] msg = (byte[]) (args[1]); // Get the message that needs to be processed
            ZMQ.Socket backend = (ZMQ.Socket) args[2]; // Backend Socket
            ZMQ.Socket frontend = (ZMQ.Socket) args[3]; // Frontend Socket
            System.out.println("ID: " + id + " Frontend to backend " + msg);
            // Send to backend
            backend.sendMore(id);
            backend.sendMore(ZMQ.MESSAGE_SEPARATOR);
            backend.send(msg);
            // Get response from backend
            ArrayList<byte[]> response = new ArrayList<>();
            do {
                byte[] received_message;
                received_message = backend.recv(0);
                if (received_message == null) {
                    while (received_message == null) {
                        // Send the message again until I get a response
                        backend.sendMore(id);
                        backend.sendMore(ZMQ.MESSAGE_SEPARATOR);
                        backend.send(msg);
                        received_message = backend.recv(0);
                    }
                }
                response.add(received_message);
            } while (backend.hasReceiveMore());
            System.out.println("Backend to frontend " + response);
            // Send response from backend to frontend
            for (int i = 0; i < response.size() - 1; ++i)
                frontend.sendMore(response.get(i));
            frontend.send(response.get(response.size() - 1));
        }
    }

    public void receiveAndSend() throws IOException {
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
            // Set socket timeouts
            int time = 2000;
            this.backend_socket.setSendTimeOut(time);
            this.backend_socket.setReceiveTimeOut(time);
            this.frontend_socket.setSendTimeOut(time);
            // Bind sockets
            this.frontend_socket.bind("tcp://*:" + String.valueOf(this.SERVICE_PORT));
            this.backend_socket.bind("tcp://*:" + String.valueOf(this.BIND_PORT));
            // Start healthcheck mechanism
            Thread t=new Thread(new HealthCheck(this.servers));
            t.start();
            try{
                t.join();
            }catch(Exception e){
                e.printStackTrace();
            }

            System.out.println("Load Balancer is ready");
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
                 * Receive the next frame and pass that to the application.
                 */
                try {
                    ArrayList<byte[]> message = new ArrayList<>();
                    do {
                        message.add(this.frontend_socket.recv(0)); // Wait for a message and add each part to an array
                                                                   // list
                    } while (this.frontend_socket.hasReceiveMore());
                    ZThread.fork(ctx, new ReplySender(), message.get(0), message.get(2), this.backend_socket,
                            this.frontend_socket); // Create a ReplySender
                } catch (Exception e) {
                }
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

    public LoadBalancer(String[] clientIps, String[] serverIps) {
        // I believe that publisher-subscriber is less
        // adequate than request-reply. See Code Connected
        // by Pieter Hientjens "knowledge is distributed
        // and the more static pieces you have, the more
        // effort it is to change the topology." Page 50 is
        // important for understanding this
        // http://hintjens.wdfiles.com/local--files/main%3Afiles/cc1pe.pdf
        this.ctx = new ZContext();
        this.actor = new ZAuth(this.ctx);
        this.actor = this.actor.setVerbose(true);
        this.servers=serverIps;

        // http://hintjens.com/blog:49
        for (String ip : clientIps) {
            this.actor = this.actor.allow(ip);
        }
        for (String ip : serverIps) {
            this.actor = this.actor.allow(ip);
        }

        this.actor = this.actor.replies(true);

        this.frontend_socket = ctx.createSocket(SocketType.ROUTER);
        this.frontend_socket.setZAPDomain("global");
        this.backend_socket = ctx.createSocket(SocketType.DEALER);
        this.backend_socket.setZAPDomain("global");
    }
}
