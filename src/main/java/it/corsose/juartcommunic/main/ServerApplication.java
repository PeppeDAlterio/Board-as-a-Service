package it.corsose.juartcommunic.main;

import it.corsose.juartcommunic.net.Server;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ServerApplication {

    private static Server server;

    private static Scanner scanner = new Scanner(System.in);;

    public static void main(String[] args) throws IOException {

        server = new Server(1234);
        server.startServer();

        boolean continua = true;

        while (continua) {

            int selection = printMenuAndGetSelection();

            switch (selection) {

                case 1:
                    printClients();
                    break;

                case 2:
                    try {
                        sendMessageToClient();
                    } catch (IllegalArgumentException e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        remoteFlash();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                case 0: default:
                    server.stopServer();
                    System.out.println("Premi invio per continuare...");
                    System.out.flush();
                    scanner.nextLine();
                    continua = false;
                    break;

            }

            enterToContinue();

        }

    }

    private static int printMenuAndGetSelection() {
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");

        System.out.println("====================================");
        System.out.println("1. Lista client");
        System.out.println("2. Invio messagio a client");
        System.out.println("3. Flash remoto");
        System.out.println("0. Esci");
        System.out.println("====================================");
        int selection = scanner.nextInt();
        scanner.nextLine();

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        return selection;
    }

    private static void printClients() {
        System.out.println(server.getClients());
    }

    private static void sendMessageToClient() {
        System.out.println(server.getClients());
        System.out.print("\nInserisci id client: ");
        System.out.flush();
        long id = scanner.nextLong();scanner.nextLine();
        String clientName = server.getClientNameById(id);
        if(clientName==null) {
            throw new IllegalArgumentException("Client non trovato");
        }
        System.out.println("Hai inserito l'id: " + id + ". Inserisci il messaggio da inviare a " + clientName + " :");
        System.out.flush();
        String msg = scanner.nextLine();
        try {
            server.sendMessage(id, msg);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Client non trovato o non connesso");
        }
        System.out.println("Messaggio '" + msg + "' inviato al client id " + id + ".");
    }

    private static void remoteFlash() throws IOException {

        System.out.println("Inserisci la path al file .ELF da flashare: ");
        String filePath = scanner.nextLine();

        filePath = filePath.trim();

        if(!new File(filePath).exists()) {
            throw new IllegalArgumentException("Il file specificato non esiste");
        }

        if(!filePath.endsWith(".elf")) {
            throw new IllegalArgumentException("Il file non ha estensione .ELF");
        }

        System.out.println(server.getClients());
        System.out.print("\nInserisci id client: ");
        System.out.flush();
        long id = scanner.nextLong();scanner.nextLine();

        server.sendFile(id, filePath);

    }

    private static void enterToContinue() {
        System.out.println("Premi invio per continuare...");
        System.out.flush();
        scanner.nextLine();
    }

}
