import java.io.*;
import java.net.*;

public class CClient {
    public static void main(String args[]) throws IOException 
    {
        Socket socket = new Socket("localhost",56487);
        System.out.println( socket.getLocalAddress()+":"+socket.getLocalPort() + 
                            " --> " + 
                            socket.getInetAddress() + ":" + socket.getPort());

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send & receive
        out.println("Client time: " + System.nanoTime());
        System.out.println(in.readLine());

        socket.close();
    }
}
