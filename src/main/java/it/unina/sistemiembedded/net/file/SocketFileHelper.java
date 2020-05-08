package it.unina.sistemiembedded.net.file;

import it.unina.sistemiembedded.utility.Constants;

import java.io.*;

public class SocketFileHelper {

    /**
     * Transfers a file
     * @param dos DataOutputStream
     * @param file String path to the file to transfer
     * @return long number of bytes transmitted
     * @throws IOException
     */
    public static long sendFile(DataOutputStream dos, String file) throws IOException {

        File myFile = new File(file);

        if(!myFile.exists()) {
            throw new IllegalArgumentException("Il file specificato non esiste");
        }

        FileInputStream fis = new FileInputStream(myFile);

        dos.writeUTF(Constants.BEGIN_FILE_TX);

        dos.writeUTF(myFile.getName());

        dos.writeLong(myFile.length());

        long totalCount = 0L;
        int count;
        byte[] buffer = new byte[1024];
        while ( (count = fis.read(buffer)) > 0) {
            dos.write(buffer, 0, count);
            dos.flush();
            totalCount += count;
        }

        assert totalCount == myFile.length();

        dos.writeUTF(Constants.END_FILE_TX);

        fis.close();

        return totalCount;

    }

    /**
     * Receives a file from a DataInputStream
     * @param dis DataInputStream
     * @return Received file path
     * @throws IOException
     */
    public static String receiveFile(DataInputStream dis, String fileExtension) throws IOException {

        String beginOfTx = dis.readUTF();

        if(!beginOfTx.equals(Constants.BEGIN_FILE_TX)) {
            throw new IllegalStateException("Begin of file transmission expected!");
        }

        String filename = dis.readUTF();

        if(!filename.trim().toLowerCase().endsWith(fileExtension.trim().toLowerCase())) {
            throw new IllegalStateException("File with extension '" + fileExtension + "' expected!");
        }

        filename = filename.trim();

        File newFile = new File("received/" + filename);

        if(newFile.exists() &&!newFile.delete()) {
            throw new IllegalArgumentException("Esiste già un file col nome '" + filename + "' e non è possibile cancellarlo.");
        }

        if(!newFile.createNewFile()) {
            throw new IllegalArgumentException("Impossibile creare il file '" + filename + "'.");
        }

        long fileSize = dis.readLong();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));

        long currentBytes = 0L;

        while( currentBytes<fileSize ) {

            int availableBytes = 0;
            if( (availableBytes = dis.available())>0 ) {

                int bytesToRead = Math.min((int)(fileSize-currentBytes), availableBytes);

                byte[] fileBytes = new byte[bytesToRead];
                currentBytes += dis.read(fileBytes, 0, bytesToRead);
                bos.write(fileBytes, 0, bytesToRead);
                bos.flush();

            }

        }

        String endOfTx = dis.readUTF();

        if(endOfTx.equals(Constants.END_FILE_TX)) {
            System.out.println("Ricezione completata con successo.");
        }

        System.out.println("File " + filename
                + " downloaded (" + currentBytes + " bytes read)");

        bos.close();

        return "received/" + filename;


    }

}
