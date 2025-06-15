package http_downloader.downloader;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class HttpDownloader_2 {

    public static void main(String args[]) throws IOException {

        // Filename
        LocalTime localTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String time = localTime.format(formatter);

        // set bufferSize for BufferedOutpurStream
        int bufferSize = 1024;
        if (args.length > 0) {
            bufferSize = Integer.parseInt(args[0]); // take first argument from commandoline
        }
        byte[] buffer = new byte[bufferSize];

        // define start- and endBlock -> later used in the http request
        String startBlock = "0";
        String endBlock = "4096";

        File path = new File("C:\\Studium\\BHT\\Verteilte Systeme\\http_downloader\\downloader\\savedFiles\\logo_" + time + ".svg"); // final file
        
        Path logPath = Paths.get("http_downloader\\downloader\\log\\log.txt"); // log-file path
        
        // checks if a logfile already exists 
        if(Files.exists(logPath)) {
            try (Scanner myReader = new Scanner(logPath)) { // read latest start- and endBlock
                startBlock = myReader.nextLine();
                endBlock = myReader.nextLine();
            }
        } else { // if logfile does not exists, create new logfile
            File logFile = new File("http_downloader\\downloader\\log\\log.txt");
            logFile.createNewFile();
            
            try(PrintWriter writer = new PrintWriter(logFile)) { // setup the first lines in the logfile
                writer.println("0");
                writer.println("4096");
            }
        }

        
        
        double downloaded = 0.00; // for printline
        int read = 0; // later to check if all bytes are been read
        while (true) {

            try {
                Socket socket = new Socket("speedtest.belwue.net", 80); //set up socket

                // building http request
                String request = "GET /BelWue_logo.svg HTTP/1.1\r\n"
                        + "Host: speedtest.belwue.net\r\n"
                        + "Connection: close\r\n"
                        + "Range: bytes=" + startBlock + "-" + endBlock + "\r\n"
                        + "\r\n";
                // send request to the server
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.print(request);
                out.flush();

                // set up InputStream and Reader to read the response from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isReader = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isReader);

                // FileoutputStream to write the new file
                FileOutputStream fos = new FileOutputStream(path, true);
                BufferedOutputStream bout = new BufferedOutputStream(fos); // Buffered is more easy and faster


                // read header -> does not work. Workaround in line "bout.write(buffer, 320 , read - 320);"
                String line;
                int breakPoint = 50000;
                /*
                 * 
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        break;
                    }
                    if(line.startsWith("Content-Range:")) {
                        String[] parts = line.split("[/]");
                        breakPoint = Integer.parseInt(parts[1]);
                    }
                    System.out.println("Header: " + line);
                }
                */
                
                

                // read body  -> for every block read smaller "buffer-blocks"
                boolean firstIteration = true;
                while ((read = is.read(buffer)) >= 0) {
                    // workaround for skipping the header
                    if(firstIteration){ // only do this in the first iteration 
                        bout.write(buffer, 320 , read - 320); // only works for this specific case because the heads size can be different for other files
                        firstIteration = false;
                        continue;
                    }

                    bout.write(buffer, 0, read); // wirtes the rest of the block 

                    System.out.println("Downloaded " + read + " bytes." + " | Total downloaded " + downloaded * Math.pow(10, -6) + " MegaBytes.");
                    downloaded += read; 
                }

                // close everything
                bout.close();
                reader.close();
                socket.close();
                System.out.println("Block complet");

                // checks if the download is finished 
                if(Integer.parseInt(endBlock) >= breakPoint) {
                    System.out.println("Download complet");
                    break;
                }

                // write the log-file
                PrintWriter myWriter = new PrintWriter( "http_downloader\\downloader\\log\\log.txt");
                //myWriter.print("");
                myWriter.println(startBlock);
                myWriter.print(endBlock);
                myWriter.close();

                // setting neu start- and endBlock
                startBlock = endBlock;
                int tempEndBlock = Integer.parseInt(endBlock) + 4096;
                endBlock = Integer.toString(tempEndBlock);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

}
