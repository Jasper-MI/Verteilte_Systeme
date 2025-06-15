package http_downloader.downloader;


import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;



public class HttpDownloader {

    
    public static void main(String args[]) throws IOException {
        
        
        LocalTime localTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String time = localTime.format(formatter);

        File path = new File("C:\\Studium\\BHT\\Verteilte Systeme\\http_downloader\\downloader\\savedFiles\\1G_" + time);
        try{

            System.out.print("hier");
            
            Socket socket = new Socket("speedtest.belwue.net", 80);
            
            System.out.print("hier");

            String request = "GET /1G HTTP/1.1\r\n" +
                            "Host: speedtest.belwue.net\r\n"+
                            "Connection: close\r\n" +
                            "\r\n";
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.print(request);
            out.flush();
            
            InputStream is = socket.getInputStream();
            InputStreamReader isReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isReader);
            
            FileOutputStream fos = new FileOutputStream(path);
            BufferedOutputStream bout = new BufferedOutputStream(fos);

            byte[] buffer = new byte[4096];

            double downloaded = 0.00;
            int read = 0;
            

            // read header
            String line;
            while((line = reader.readLine()) != null) {
                if(line.isEmpty()) break;
                System.out.println("Header: " + line);
            }


            // read body
            while((read = is.read(buffer)) >= 0) {
                bout.write(buffer, 0, read);

                System.out.println("Downloaded " +  read + " bytes." + " | Total downloaded " + Math.round(downloaded * Math.pow(10, -6))  +" MegaBytes.");
                downloaded += read;
            }
            bout.close();
            reader.close();
            socket.close();
            System.out.println("Download complet");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }



    public void try2() {

        String link = "http://speedtest.belwue.net/BelWue_logo.svg";
        File out = new File("C:\\Studium\\BHT\\Verteilte Systeme\\http_downloader\\downloader\\savedFiles\\logo.svg");
        try{

            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            double filesize = (double) http.getContentLengthLong();
            BufferedInputStream in = new BufferedInputStream(http.getInputStream());
            FileOutputStream fos = new FileOutputStream(out);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] buffer = new byte[1024];
            double downloaded = 0.00;
            int read = 0;
            double percentDownloaded = 0.00;


            while((read = in.read(buffer, 0, 1024)) >= 0) {
                bout.write(buffer, 0, read);
                downloaded += read;
                percentDownloaded = (downloaded*100) / filesize;
                String percent = String.format("%.4f", percentDownloaded);
                System.out.println("Downloaded " +  percent + "%");
            }
            bout.close();
            in.close();
            System.out.println("Download complet");

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void try1() throws IOException {

        URL url = new URL("http://speedtest.belwue.net/BelWue_logo.svg");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        System.out.println("status: " + status);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        //System.out.println(content);
        in.close();
        con.disconnect();

    }

}
