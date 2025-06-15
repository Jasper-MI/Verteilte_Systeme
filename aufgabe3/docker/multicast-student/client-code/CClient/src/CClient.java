import java.io.*;
import java.net.*;

public class CClient {

    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(CClient.class.getName() + ": " + args[0]);
        } else {
            System.out.println(CClient.class.getName());
            return;
        }

        String groupAddress = args[0];
        int port = 6789;
        InetAddress group = null;
        MulticastSocket socket = null;

        try {
            group = InetAddress.getByName(groupAddress);
            socket = new MulticastSocket(port);

            InetAddress finalGroup = group;
            MulticastSocket finalSocket = socket;

            // join group, client kann ab hier nachrichten empfangen
            socket.joinGroup(finalGroup);
            System.out.println("CClient: joined " + groupAddress);

            // Empfangs-Thread
            new Thread(() -> {
                try {
                    byte[] buf = new byte[1024]; // für eingehende Daten
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length); // empfängt UDP-Daten
                        finalSocket.receive(packet); // blockiert bis NAchricht eintrifft
                        String received = new String(packet.getData(), 0, packet.getLength()); // dekodiert Nachricht
                        System.out.println("Client received: " + received);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Sendeschleife
            while (true) {
                String message = "heartbeat";
                byte[] buf = message.getBytes(); // heartbeat zu byte array, wegen udp
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port); // Datapacket
                socket.send(packet);
                Thread.sleep(10000); // 10 Sekunden Pause
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            // leave group
        } finally {
            try {
                if (socket != null && group != null) {
                    socket.leaveGroup(group);
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
