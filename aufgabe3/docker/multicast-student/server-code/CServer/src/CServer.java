import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 1) {
            System.out.println(CServer.class.getName() + ": " + args[0]);
        } else {
            System.out.println(CServer.class.getName());
        }

        String multicastAddress = args[0];
        System.out.println(CServer.class.getName() + ": " + multicastAddress); // Speicher Adresse

        InetAddress group = InetAddress.getByName(multicastAddress);
        int port = 6789;
        MulticastSocket socket = new MulticastSocket(port); // set socket with correct port

        // join group
        socket.joinGroup(group);

        // receive msg
        byte[] buf = new byte[1024];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);

        while (true) {
            socket.receive(recv); // blockiert, bis nachricht empfangen wird --> schreibt dann daten in recv
            String received = new String(recv.getData(), 0, recv.getLength());
            System.out.println("Received from " + recv.getAddress() + ": " + received);

            if (received.trim().equals("heartbeat")) {

                // Uptime ausführen (uptime liefert die ganze kacke im print )
                Process process = Runtime.getRuntime().exec("uptime");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String uptime = reader.readLine();
                process.waitFor();

                if (uptime != null) {
                    byte[] response = uptime.getBytes(); // Ausgabe zu byte array, weil udp nur bytes senden kann

                    // Antwortpacket mit uptime inhalt, und adresse + port des clients der heartbeat
                    // sendet
                    DatagramPacket responsePacket = new DatagramPacket(
                            response, response.length, recv.getAddress(), recv.getPort());

                    // sende antwort
                    socket.send(responsePacket);
                    System.out.println("Sent uptime to " + recv.getAddress() + ":" + recv.getPort());
                }
            }
        }
    }
}
