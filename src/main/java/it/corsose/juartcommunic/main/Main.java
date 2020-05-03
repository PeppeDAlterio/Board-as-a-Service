package it.corsose.juartcommunic.main;

import it.corsose.juartcommunic.net.Server;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Server server = new Server(1234);
        try {
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(15000);
            System.out.println("TERMINATA ATTESA CONNESSIONI");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Lista client");
        System.out.println(server.getClients());

        long id;
        String msg;

        System.out.print("\nInserisci id client: ");
        Scanner myObj = new Scanner(System.in);
        id = myObj.nextLong();
        System.out.println("Hai inserito l'id: " + id + ". Inserisci il messaggio: ");
        myObj.nextLine();
        msg = myObj.nextLine();

        server.sendMessage(id, msg);

    }

}
