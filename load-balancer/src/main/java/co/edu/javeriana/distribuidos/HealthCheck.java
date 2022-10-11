package co.edu.javeriana.distribuidos;

import java.net.InetAddress;

public class HealthCheck implements Runnable {

    private String[] servers;

    public HealthCheck(String[] servers) {
        this.servers = servers;
    }

    // Sends ping request to a provided IP address
    public static boolean sendPingRequest(String ipAddress) {
        try {
            return InetAddress.getByName(ipAddress).isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean oneDifferent(boolean[] array) {
        for (int i = 0; i < array.length; ++i)
            if (array[i])
                return true;
        return false;
    }

    @Override
    public void run() {
        boolean[] statuses = new boolean[this.servers.length-1];
        for (int i = 0; i < statuses.length; i++)
            statuses[i] = false;

        // This has to check if servers are alive, in case there aren't any servers
        // alive, this should create a server and once there's at least one connected,
        // should kill that server.
        ProcessBuilder builder=new ProcessBuilder("go","run","server.go");
        builder.inheritIO();
        Process process = null;
        for (int i=0; i<statuses.length; ++i){
            statuses[i]=sendPingRequest(this.servers[i]);
        }
        while (true) {
            if (process != null && oneDifferent(statuses)) {
                System.out.println("There's at least one server connected at the moment, so I need to kill mine...");
                try {
                    System.out.println("Killing process: "+String.valueOf(process.pid()));
                    new ProcessBuilder("kill","-9",String.valueOf(process.pid())).start();
                    System.out.println("Process killed successfully!");
                } catch (Exception e) {
                } finally {
                    process=null;
                }
            }
            for (int i = 0; i < statuses.length; i++) {
                if (!statuses[i]) {
                    String server = this.servers[i];
                    boolean status = sendPingRequest(server);
                    statuses[i] = status;
                    if (status) {
                        // Start checking for the health
                        new Thread(new HealthCheckAuxiliary(server, statuses, i)).start();
                    }
                }
            }
            if (process==null && !oneDifferent(statuses))
                try {
                    System.out.println("There's no server connected at the moment, so I need to deploy one...");
                    process = builder.start();
                    System.out.println("Created process: "+String.valueOf(process.pid()));
                } catch (Exception e) {
                }
        }
    // Once there's at least one of the servers connected, the auxiliary server
    // needs to stop
}}
