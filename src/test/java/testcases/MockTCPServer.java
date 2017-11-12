package testcases;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MockTCPServer {
	public static final int PORT = 3000;
	public static final byte GROUP = 9;
	public static final byte NODE = 101;
	public static final byte SUB_ID = 12;
	public static final String IP = "localhost";
	public static final int RESULT_CLIENT = 1234;
	public static final int RESULT_SERVER = 9876;
	
	public static void runClient() {
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
				b.putShort((short)RESULT_CLIENT); // Data
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
	
	static class Result {
		public int group;
		public int node;
		public int type;
		public int data;
	}
	
	public static Result runServer() {
		Result r = new Result();
		try(ServerSocket ss = new ServerSocket(PORT, 0, InetAddress.getByName(IP));
				Socket sock = ss.accept();) {
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			byte[] b = new byte[4];
			dis.readFully(b);
			if((b[0] & 0xFF) == 0xBB) {
				r.node = b[1] & 0xff;
				r.group = b[2] & 0xff;
				int len = b[3] & 0xff;
				b = new byte[len];
				dis.readFully(b);
				r.type = b[0] & 0xff;
				r.data = ((b[b.length-2] & 0xff) << 8) | b[b.length-1] & 0xff;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return r;
	}

}
