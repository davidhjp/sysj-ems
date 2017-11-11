package testcases;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MockTCPServer implements Runnable {
	public static final int PORT = 3000;
	public static final byte GROUP = 9;
	public static final byte NODE = 101;
	public static final byte SUB_ID = 12;
	public static final String IP = "localhost";
	public static final int RESULT = 1234;
	
	@Override
	public void run() {
//		while(!Thread.currentThread().isInterrupted()) {
			try(ServerSocket ss = new ServerSocket(PORT, 0, InetAddress.getByName(IP));
					Socket sock = ss.accept();) {
				DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
				ByteBuffer b = ByteBuffer.allocate(50);
				b.put((byte)0xAA);
				b.put((byte)15);
				b.putShort((short)0); // FCR
				b.put((byte)12); // Packet count
				b.putShort((short)0); // PAN ID
				b.putShort((short)0); // Dest
				b.put(GROUP); // Group
				b.put(NODE); // Node
				b.put(SUB_ID); // Packet type (SubID)
				b.putShort((short)RESULT); // Data
				b.putShort((short)0); // Footer 1 2
				b.put((byte)0); // Fooger 3
				
				byte[] bb = new byte[b.position()];
				b.rewind();
				b.get(bb);
				dos.write(bb);
				dos.flush();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}
	}

}
