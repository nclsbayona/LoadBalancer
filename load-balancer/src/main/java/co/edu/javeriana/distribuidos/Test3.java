package co.edu.javeriana.distribuidos;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Test3 {

    private static ZContext context = new ZContext();

    public static void main(String... args) {
        final int totalCount = 1000;
        String topic = "tcp://localhost:30216";
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(totalCount);

        // create a simple subscriber
        final ZMQ.Socket subscribeSocket = context.createSocket(SocketType.SUB);
        subscribeSocket.connect(topic);
        subscribeSocket.subscribe("TestTopic".getBytes());

        Thread subThread = new Thread() {

            @Override
            public void run() {
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    startLatch.countDown();

                    while (latch.getCount() > 0) {

                        // get the message
                        String count = subscribeSocket.recvStr(0);
                        if (count != null) {

                            System.out.println("Aquí recibi "+count);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(latch.getCount());
                } finally {
                    subscribeSocket.close();
                }
            }
        };

        // create a simple publisher - wait 3 sec to make sure its ready
        ZMQ.Socket publishSocket = context.createSocket(SocketType.PUB);
        publishSocket.bind("tcp://*:30216");

        try {
            subThread.start();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // publish a sample message
            long before = System.currentTimeMillis(), after;
            try {
                for (int i = 0; i < totalCount; i++) {
                    publishSocket.send("NotTestTopic".getBytes(), ZMQ.SNDMORE | ZMQ.DONTWAIT);
                    publishSocket.send("Not received".getBytes(), 0);
                    publishSocket.send("TestTopic".getBytes(), ZMQ.SNDMORE | ZMQ.DONTWAIT);
                    publishSocket.send("This is test string".getBytes(), ZMQ.DONTWAIT);
                    latch.await(10000, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                after = System.currentTimeMillis();
                publishSocket.close();
            }
            System.out.println(String.valueOf(totalCount) + " messages took " + (after - before) + " ms.");
        } finally {
            publishSocket.close();
            subscribeSocket.close();
        }
    }
}
