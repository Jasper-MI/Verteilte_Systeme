import java.io.*;
import java.net.*;
import java.security.MessageDigest; // Für SHA1-Berechnung

public class MyClient {
    public static void main(String args[]) throws IOException {
        if (args.length != 1 || !args[0].contains(":")) {
            System.out.println("Usage: java MyClient <host:file>");
            return;
        }
        String[] parts = args[0].split(":");
        String host = parts[0]; // Hostname oder IP-Adresse des Servers
        String fileName = parts[1]; // Dateiname

        Socket socket = new Socket(host, 8088);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // zum Senden von Daten an den Server
        DataInputStream in = new DataInputStream(socket.getInputStream()); // zum Empfangen von Daten vom Server

        out.writeUTF(fileName); // sende Dateinamen an den Server

        long startTime = System.nanoTime(); // Zeitmessung starten

        long fileSize = in.readLong(); // Dateigröße lesen die Server sendet

        if (fileSize == -1) { // Wenn der Server -1 sendet --> Datei existiert nicht --> Abbruch
            System.out.println("Datei nicht gefunden auf dem Server.");
            return;
        }

        try (FileOutputStream fileOut = new FileOutputStream("received_" + fileName)) { // neue Datei zum Schreiben
                                                                                        // öffnen
            byte[] buffer = new byte[8192];
            long bytesReceived = 0;
            while (bytesReceived < fileSize) {
                int bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived));
                // Geht durch alle empfangenen Bytes, solange bis die Dateigröße erreicht ist
                // Liest Daten in den Puffer, in.read(...) liest so viele Bytes wie möglich
                // Math.min(...) stellt sicher, dass nicht mehr Bytes gelesen werden, als noch
                // zu empfangen sind

                if (bytesRead == -1) { // -1 --> Stream wurde geschlossen
                    break;
                }
                fileOut.write(buffer, 0, bytesRead); // empfangene Bytes in Datei schreiben
                bytesReceived += bytesRead; // Zähler für empfangene Bytes aktualisieren
            }
        }

        long endTime = System.nanoTime(); // Zeitmessung beenden

        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Übertragung abgeschlossen in %.2f Sekunden.\n", durationSeconds);

        System.out.println("SHA1 (Client-Datei): " + sha1OfFile("received_" + fileName));

        socket.close();
    }

    public static String sha1OfFile(String path) throws IOException {
        try (InputStream fis = new FileInputStream(path)) {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int len; // Anzahl der gelesenen Bytes
            while ((len = fis.read(buffer)) != -1) { // liest datei und gibt sie an sha1 algorithm weiter
                sha1.update(buffer, 0, len);
            }
            byte[] digest = sha1.digest(); // Byte Array mit dem SHA1-Hash der Datei

            StringBuilder sb = new StringBuilder(); // Konvertiert das Byte-Array in einen hexadezimalen String
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Fehler: " + e.getMessage();
        }
    }
}