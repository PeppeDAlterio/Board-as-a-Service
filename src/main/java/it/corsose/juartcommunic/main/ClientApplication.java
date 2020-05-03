package it.corsose.juartcommunic.main;

import it.corsose.juartcommunic.net.Client;

public class ClientApplication {

    public static void main(String[] args) {

        String ipAddress = "localhost";
        int port = 1234;

        if(args.length>0) {
            ipAddress = args[0];
        }

        if(args.length>1) {
            port = Integer.parseInt(args[1]);
        }

        Client client = new Client(ipAddress, port);
        try {
            client.startClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
