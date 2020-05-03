package it.corsose.juartcommunic.main;

import it.corsose.juartcommunic.net.Server;

import java.io.IOException;
import java.util.Scanner;

public class ServerApplication {

    public static void main(String[] args) throws IOException {

        Server server = new Server(1234);
        server.startServer();

        Scanner scanner = new Scanner(System.in);

        boolean continua = true;

        while (continua) {

            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");

            System.out.println("====================================");
            System.out.println("1. Lista client");
            System.out.println("2. Invio messagio a client");
            System.out.println("0. Esci");
            System.out.println("====================================");
            int selection = scanner.nextInt();scanner.nextLine();

            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");

            switch (selection) {

                case 1:
                    System.out.println(server.getClients());
                    System.out.println("Premi invio per continuare...");
                    System.out.flush();
                    scanner.nextLine();
                    break;
                case 2:
                    System.out.println(server.getClients());
                    System.out.print("\nInserisci id client: ");
                    System.out.flush();
                    long id = scanner.nextLong();scanner.nextLine();
                    String clientName = server.getClientNameById(id);
                    if(clientName==null) {
                        System.err.println("Nessun client con l'id inserito!");
                        break;
                    }
                    System.out.println("Hai inserito l'id: " + id + ". Inserisci il messaggio da inviare a " + clientName + " :");
                    System.out.flush();
                    String msg = scanner.nextLine();
                    try {
                        server.sendMessage(id, msg);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Client non connesso !");
                        break;
                    }
                    System.out.println("Messaggio '" + msg + "' inviato al client id " + id + ".\nPremi invio per continuare...");
                    System.out.flush();
                    scanner.nextLine();
                    break;

                case 0: default:
                    server.stopServer();
                    System.out.println("Premi invio per continuare...");
                    System.out.flush();
                    scanner.nextLine();
                    continua = false;
                    break;

            }

        }

    }

}
