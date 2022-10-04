package co.edu.javeriana.distribuidos;

import java.net.InetAddress;

public class HealthCheckAuxiliary implements Runnable {
    private String ip;
    private boolean[] statuses;
    private int index;

    // Sends ping request to a provided IP address
    public boolean sendPingRequest() {
        try {
            return InetAddress.getByName(this.ip).isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }

    public HealthCheckAuxiliary(String ip, boolean[] statuses, int index) {
        this.ip = ip;
        this.index = index;
        this.statuses = statuses;
    }

    @Override
    public void run() {
        boolean end=false;
        while (!end) {
            boolean replied = this.sendPingRequest();
            if (!replied) {
                boolean should_continue_trying = true;
                int counter = 0;
                for (int i = 0; i < 3 && should_continue_trying; ++i) {
                    counter += this.sendPingRequest() ? 1 : 0;
                    if (counter > 0)
                        should_continue_trying = false;
                }
                if (should_continue_trying){
                    statuses[index] = false;
                    end=true;
                }
            }
        }
        // Should ping the server, in case it does not reply (3 times), it should mark
        // server as not alive and exit
    }

}
