import java.io.*;
import java.net.*;
import java.nio.file.*;


public class HttpDownloader_3 {


    public static void main(String[] args) throws Exception {
        if (args.length != 2) { // Überprüft obt Blockgröße und URL angegeben wurden
            System.out.println("Usage: java Downloader <blockSize> <http://host/path/file>");
            return;
        }


        // Umwandlung der Blockgröße in Bytes
        int blockSize = parseBlockSize(args[0]); // z.B. 20M = 20 * 1024 * 1024
        URL url = new URL(args[1]);
        String host = url.getHost(); // Extrahiert den Hostnamen, Pfad und Dateinamen aus der URI
        String path = url.getPath();
        String fileName = extractFileName(path);


        // Prüfung durch HEAD-REquest, ob der Server Range-Requests unterstützt
        boolean supportsRange = checkRangeSupport(host, path);
        if (!supportsRange) {
            System.out.println("Server unterstützt keine Range-Requests.");
            return;
        }


        // Dateigröße ermitteln und berechnen, wie viele Blöcke benötigt werden
        int contentLength = getContentLength(host, path);
        int totalBlocks = (int) Math.ceil((double) contentLength / blockSize);


        // Für jeden Block Start und End Bytes berechnen und speichern in block_i
        for (int i = 0; i < totalBlocks; i++) {
            int from = i * blockSize;
            int to = Math.min((i + 1) * blockSize - 1, contentLength - 1);
            String blockFile = "block_" + i;


            // Prüfen, ob der Block bereits heruntergeladen wurde, wenn ja dann skip
            if (Files.exists(Path.of(blockFile))) {
                System.out.println("Block " + i + " existiert bereits, überspringe...");
                continue;
            }


            // Block per HTTP GET mit RANGE Header herunterladen und speichern in block_i
            System.out.println("Lade Block " + i + " (Bytes " + from + "-" + to + ")");
            byte[] data = downloadBlock(host, path, from, to);
            Files.write(Path.of(blockFile), data);
        }


        // Wenn alle Blöcke heruntergeladen wurden, --> öffne Zieldatei, kopiere alle
        // Blöcke hinein und lösche die Blockdatei
        try (OutputStream out = new FileOutputStream(fileName)) {
            for (int i = 0; i < totalBlocks; i++) {
                Path blockPath = Path.of("block_" + i);
                Files.copy(blockPath, out);
                Files.delete(blockPath); // optional
            }
        }


        System.out.println("Download abgeschlossen: " + fileName);
    }


    // Wandelt die Blockgröße in Bytes um, unterstützt M (Mega) und K (Kilo)
    static int parseBlockSize(String input) {
        input = input.toUpperCase();
        if (input.endsWith("M"))
            return Integer.parseInt(input.replace("M", "")) * 1024 * 1024;
        if (input.endsWith("K"))
            return Integer.parseInt(input.replace("K", "")) * 1024;
        return Integer.parseInt(input);
    }


    // Wenn der Pfad mit einem / endet, wird ein Zeitstempel als Dateiname
    // verwendet, sonst der Dateiname aus dem Pfad extrahiert
    static String extractFileName(String path) {
        if (path.endsWith("/")) {
            return "download_" + System.currentTimeMillis();
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }


    // Öffne TCP-Verbindung zum Server, sende HEAD-Request und prüfe, ob der Server
    // Range-Requests unterstützt
    static boolean checkRangeSupport(String host, String path) throws IOException {
        Socket socket = new Socket(host, 80);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print("HEAD " + path + " HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("Connection: close\r\n\r\n");
        out.flush();


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        boolean supportsRange = false;
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("accept-ranges:")) {
                supportsRange = line.toLowerCase().contains("bytes");
            }
        }
        socket.close();
        return supportsRange;

    }

    // Gesamtgröße der Datei ermitteln, indem ein HEAD-Request gesendet wird
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
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("content-length:")) {
                length = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        socket.close();
        return length;
    }


    // GET-Request, lese nur den Body der Antwort, speichert die heruntergeladenen
    // Bytes in einem Byte-Array
    static byte[] downloadBlock(String host, String path, int from, int to) throws IOException {
        Socket socket = new Socket(host, 80);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print("GET " + path + " HTTP/1.1\r\n");
        out.print("Host: " + host + "\r\n");
        out.print("Range: bytes=" + from + "-" + to + "\r\n");
        out.print("Connection: close\r\n\r\n");
        out.flush();


        InputStream in = socket.getInputStream();
        ByteArrayOutputStream body = new ByteArrayOutputStream();


        // Header überspringen
        BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = headerReader.readLine()) != null && !line.isEmpty()) {
            // Nichts tun – nur Header überspringen
        }


        // Body lesen
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            body.write(buffer, 0, bytesRead);
        }


        socket.close();
        return body.toByteArray();
    }
}