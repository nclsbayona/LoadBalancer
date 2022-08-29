package co.edu.javeriana.distribuidos;

import org.zeromq.ZMQ;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class Test2 {
    public static void main(String[] args) {
        try (ZContext ctx = new ZContext()) {
            Socket pull = ctx.createSocket(SocketType.PULL);
            pull.bind("tcp://*:5555");
            pull.monitor("inproc://events", ZMQ.EVENT_ALL);

            Socket push = ctx.createSocket(SocketType.PUSH);

            Runnable r = new Runnable() {
                public void run() {
                    Socket monitor_sock = ctx.createSocket(SocketType.PAIR);
                    monitor_sock.connect("inproc://events");
                    while (true) {
                        try{
                        byte[] event = monitor_sock.recv();
                        // just dump event as string
                        System.out.println(new String(event));
                    }catch(Exception e){}
                }
                }
            };
            (new Thread(r)).start();

            push.setSndHWM(100);
            push.connect("tcp://localhost:5555");
            push.send("hello");
            push.send("world");
        }catch (Exception e){
            
        }
    }
}
