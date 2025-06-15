import java.io.*;
import java.net.*;

public class CServer {
    public static void main(String args[]) throws IOException 
    {
        // Setup receive
        ServerSocket socket = new ServerSocket(56487);
        System.out.println("Waiting for connection on: "+
                            socket.getLocalSocketAddress());

        // Accept incoming client connection
        Socket clientSocket = socket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);


        System.out.println("Client says: " + in.readLine());
        out.println("Server says: " + System.nanoTime());

        // Close the client socket
        clientSocket.close();
        // Close the server socket
        socket.close();
    }
}