package it.unina.sistemiembedded.main;

import com.fazecast.jSerialComm.SerialPort;
import it.unina.sistemiembedded.net.Client;

import java.util.List;
import java.util.Scanner;

public class ClientApplication {

    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        String ipAddress = "localhost";
        int port = 1234;

        if(args.length>0) {
            ipAddress = args[0];
        }

        if(args.length>1) {
            port = Integer.parseInt(args[1]);
        }

        boolean wrongNumber = false;
        int comPortNumber = -1;

        do {
            System.out.println("Seleziona una COM port specificando il numero associato");
            List<SerialPort> ports = Client.listAvailableCOMPorts();

            if(ports.isEmpty()) {
                System.err.println("Nessuna scheda rilevata!");
                return;
            }

            for (int i = 0; i < ports.size(); i++) {
                System.out.println("[" + i + "] " + ports.get(i).getDescriptivePortName());
            }

            try {
                comPortNumber = scanner.nextInt();

                wrongNumber = comPortNumber < 0 || comPortNumber >= ports.size();

            } catch (Exception e) {
                scanner.nextLine();
                wrongNumber = true;
            }

        } while(wrongNumber);


        Client client = new Client(ipAddress, port);
        try {
            client.startClient(comPortNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
