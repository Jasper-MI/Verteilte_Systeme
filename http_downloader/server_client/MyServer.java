import java.io.*;
import java.net.*;

public class MyServer {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java MyServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]); 
        ServerSocket serverSocket = new ServerSocket(port); // Seversocket wartet auf client
        System.out.println("Server läuft auf Port " + port + "...");

        Socket clientSocket = serverSocket.accept();
        System.out.println("Client verbunden: " + clientSocket.getInetAddress()); // gibt die IP-Adresse des Clients aus

        DataInputStream in = new DataInputStream(clientSocket.getInputStream()); // Liest Daten vom Client
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        String filename = in.readUTF(); // Client sendet String mit Dateinamen
        System.out.println("Anfrage für Datei: " + filename);

        File file = new File(filename); // Prüft ob die Datei existiert, wenn nicht sende -1 als Fehlercode an Client
        if (!file.exists()) {
            out.writeLong(-1);
            System.out.println("Datei nicht gefunden.");
        } else {
            long fileSize = file.length(); // Dateigröße ermitteln
            out.writeLong(fileSize); // DataOutputstream sendet Größe an Client

            try (FileInputStream fileIn = new FileInputStream(file)) { // Öffnet die Datei zum Lesen
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) { // Läd Bytes in den Puffer
                    out.write(buffer, 0, bytesRead); // Sende Datei in Blöcken
                }
            }
            System.out.println("Datei gesendet.");
        }

        clientSocket.close();
        serverSocket.close();
    }
}
