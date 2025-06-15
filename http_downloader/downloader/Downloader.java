import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Downloader {

    public static void main(String[] args) throws Exception {
        // Erwartet zwei Argumente: Blockgröße und URL
        if (args.length != 2) {
            System.out.println("Usage: java Downloader <blockSize> <http://host/path/file>");
            return;
        }

        // Konvertiert die angegebene Blockgröße in Bytes
        int blockSize = parseBlockSize(args[0]);

        // Zerlegt die URL in Host, Pfad und Dateiname
        URL url = new URL(args[1]);
        String host = url.getHost();
        String path = url.getPath();
        String fileName = extractFileName(path);
        String statusFile = "download.status"; // Name der Statusdatei

        // Prüft, ob der Server Range-Requests unterstützt
        boolean supportsRange = checkRangeSupport(host, path);
        if (!supportsRange) {
            System.out.println("Server unterstützt keine Range-Requests.");
            return;
        }

        // Lade evtl. vorhandene Statusdatei
        DownloadStatus status = loadStatus(statusFile);

        // Wenn keine Statusdatei vorhanden --> neue anlegen
        if (status == null) {
            status = new DownloadStatus();
            status.uri = args[1];
            status.blockSize = blockSize;
            int contentLength = getContentLength(host, path); // Gesamtgröße ermitteln
            status.totalBlocks = (int) Math.ceil((double) contentLength / blockSize); // Blockanzahl berechnen
        }

        // Nochmals Content-Length abrufen
        int contentLength = getContentLength(host, path);

        for (int i = 0; i < status.totalBlocks; i++) {
            // Überspringe Block, wenn laut Status bereits fertig
            if (status.completedBlocks.contains(i)) {
                System.out.println("Block " + i + " ist laut Status bereits vorhanden.");
                continue;
            }

            // Byte-Bereich dieses Blocks berechnen
            int from = i * status.blockSize; // Wo der der Block beginnt
            int to = Math.min((i + 1) * status.blockSize - 1, contentLength - 1);
            String blockFile = "block_" + i;

            if (Files.exists(Path.of(blockFile))) { // Prüft, ob die Blockdatei bereits existier
                System.out.println("Block " + i + " existiert lokal, markiere als fertig...");
                status.completedBlocks.add(i); // Füge den Block der Liste der fertigen Blöcke hinzu
                saveStatus(statusFile, status); // Speichert die aktualisierte Statusdatei
                continue;
            }

            // Block vom Server laden und lokal speichern
            System.out.println("Lade Block " + i + " (Bytes " + from + "-" + to + ")");
            byte[] data = downloadBlock(host, path, from, to);
            Files.write(Path.of(blockFile), data);

            // Status aktualisieren und speichern
            status.completedBlocks.add(i);
            saveStatus(statusFile, status);

            // Fortschrittsanzeige
            double progress = 100.0 * status.completedBlocks.size() / status.totalBlocks;
            System.out.printf("Fortschritt: %.2f%%\n", progress);
        }

        // Wenn alle Blöcke vorhanden sind → Enddatei zusammenbauen
        if (status.completedBlocks.size() == status.totalBlocks) {
            try (OutputStream out = new FileOutputStream(fileName)) { // Öffnet die Zieldatei zum Schreiben
                for (int i = 0; i < status.totalBlocks; i++) {
                    Path blockPath = Path.of("block_" + i);
                    Files.copy(blockPath, out); // Block in Enddatei schreiben
                    Files.delete(blockPath); // Blockdatei löschen
                }
            }
            Files.deleteIfExists(Path.of(statusFile)); // Statusdatei löschen
            System.out.println("Download abgeschlossen: " + fileName);
        }
    }

    // Konvertiert M oder K in Bytes, wenn kein Buchstabe angegeben ist, wird die
    // Zahl direkt als Bystezahl verwendet
    static int parseBlockSize(String input) {
        input = input.toUpperCase();
        if (input.endsWith("M"))
            return Integer.parseInt(input.replace("M", "")) * 1024 * 1024;
        if (input.endsWith("K"))
            return Integer.parseInt(input.replace("K", "")) * 1024;
        return Integer.parseInt(input);
    }

    // Gibt den Dateinamen aus dem Pfad zurück oder erzeugt zufälligen Namen
    static String extractFileName(String path) {
        if (path.endsWith("/")) {
            return "download_" + System.currentTimeMillis();
        }
        return path.substring(path.lastIndexOf("/") + 1); // sonst wird der Dateiname aus dem Pfad extrahiert
    }

    // Prüft via HEAD-Request, ob der Server "Accept-Ranges: bytes" unterstützt
    static boolean checkRangeSupport(String host, String path) throws IOException {
        Socket socket = new Socket(host, 80);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print("HEAD " + path + " HTTP/1.1\r\n"); // Nur Informationen der Datei, nicht den Inhalt
        out.print("Host: " + host + "\r\n");
        out.print("Connection: close\r\n\r\n"); // \r\n\r\n → trennt den Header vom Body
        out.flush(); // Zwingt den OutputStream, alles, was bisher gepuffert wurde, jetzt sofort zu
                     // senden

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // InputStreamReader
                                                                                                // wandelt die Bytes in
                                                                                                // Zeichen um

        String line;
        boolean supportsRange = false;
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("accept-ranges:")) { // Sobald die Zeile mit "Accept-Ranges:" beginnt,
                                                                   // prüfe, ob sie "bytes" enthält
                supportsRange = line.toLowerCase().contains("bytes"); // Sobald "Accept-Ranges: bytes" in der Zeile
                                                                      // steht, dann supportsRange = true
            }
        }
        socket.close();
        return supportsRange;
    }

    // Ermittelt die Gesamtgröße der Datei via HEAD-Reques
    static int getContentLength(String host, String path) throws IOException {
        Socket socket = new Socket(host, 80);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print("HEAD " + path + " HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("Connection: close\r\n\r\n");
        out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        int length = -1;
        while ((line = in.readLine()) != null) { // Durchläuft jede Antwortzeile vom Server
            if (line.toLowerCase().startsWith("content-length:")) { // Sobald die Zeile mit Content-Length: beginnt
                length = Integer.parseInt(line.split(":")[1].trim()); // --> trenne an : und nimm den zweiten Teil und
                                                                      // wandle in Zahl um
            }
        }
        socket.close();
        return length;
    }

    // Lädt einen Block mit Range-Header herunter und gibt ihn als Byte-Array zurück
    static byte[] downloadBlock(String host, String path, int from, int to) throws IOException {
        Socket socket = new Socket(host, 80);
        OutputStream out = socket.getOutputStream(); // OutputStream zum Senden der Anfrage
        InputStream in = socket.getInputStream();// InputStream zum Empfangen der Antwort

        // HTTP GET mit Range
        String request = "GET " + path + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Range: bytes=" + from + "-" + to + "\r\n" +
                "Connection: close\r\n\r\n";
        out.write(request.getBytes());
        out.flush();

        // Header von Body trennen
        ByteArrayOutputStream headerBuf = new ByteArrayOutputStream(); // Puffer, in dem alle gelesenen Bytes
                                                                       // zwischengespeichert werden, solange sie noch
                                                                       // Teil des Headers sind
        int b;
        while ((b = in.read()) != -1) {
            headerBuf.write(b); // Fügt das gelesene Byte zum Header-Puffer hinzu
            String header = headerBuf.toString(); // Wandelt den Puffer in einen String um
            if (header.contains("\r\n\r\n")) // Prüft, ob der Header vollständig ist (Header endet mit \r\n\r\n), wenn
                                             // ja break
                break;
        }

        // Body lesen (den eigentlichen Datenblock)
        ByteArrayOutputStream body = new ByteArrayOutputStream(); // erstellt einen Puffer für den Body, in dem die
                                                                  // heruntergeladenen Daten gespeichert werden
        byte[] buffer = new byte[8192];
        int bytesRead; // wie viele bystes wirklich gelesen wurden
        while ((bytesRead = in.read(buffer)) != -1) {
            body.write(buffer, 0, bytesRead); // Fügt die gelesenen Bytes zum Body-Puffer hinzu, nur die tatsächlich
                                              // gelesenen Bytes
        }

        socket.close();
        return body.toByteArray();
    }

    // Liest den Download-Status aus Datei
    static DownloadStatus loadStatus(String statusFile) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(statusFile))) {
            return (DownloadStatus) in.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    // Schreibt den Status in die Datei
    static void saveStatus(String statusFile, DownloadStatus status) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(statusFile))) {
            out.writeObject(status);
        }
    }
}

// Klasse zur Verwaltung des Download-Zustands
class DownloadStatus implements Serializable {
    public String uri; // URI der Datei
    public int blockSize; // Größe jedes Blocks in Bytes
    public int totalBlocks; // Gesamtanzahl der Blöcke
    public Set<Integer> completedBlocks = new HashSet<>(); // IDs der bereits geladenen Blöcke
}
