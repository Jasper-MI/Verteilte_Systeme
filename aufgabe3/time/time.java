package aufgabe3.time;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class time {
    
    public static void main (String[] args) throws IOException {

        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(37));

        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();

        channel.receive(buf);
        
        
    } 

}
