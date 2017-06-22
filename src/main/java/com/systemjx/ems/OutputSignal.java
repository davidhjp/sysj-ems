package com.systemjx.ems;

import static com.systemjx.ems.SharedResource.logException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.systemj.ipc.GenericSignalSender;

public class OutputSignal extends GenericSignalSender {
	private static final int PACKET_SIZE = 5;
	private String ip;
	private int port;
	private int actuatorId;
	private int groupId;
	private int nodeId;
	private boolean fsent = true;
	private int preVal = Integer.MAX_VALUE;
	private static Map<Thread, Map<String, Socket>> socketMap = new HashMap<>();
	private String urn;

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void configure(Hashtable t) throws RuntimeException {
		this.ip = (String) t.get("IP");
		this.port = Integer.parseInt((String) t.get("Port"));
		this.actuatorId = Integer.parseInt((String) t.get("Actuator"), 16);
		this.groupId = Integer.parseInt((String) t.get("Group"), 16);
		this.nodeId = Integer.parseInt((String) t.get("Node"), 16);
		this.urn = ip + ":" + port;
		socketMap.putIfAbsent(Thread.currentThread(), new HashMap<>());
	}
	
	public Map<String, Socket> getSockets(){
		return socketMap.get(Thread.currentThread());
	}
	
	protected byte[] buildPacket(byte v) {
		ByteBuffer b = ByteBuffer.allocate(PACKET_SIZE + 3);
		b.putShort((short)0xAABB);
		b.put((byte)PACKET_SIZE);
		b.putShort((short)(groupId << 8 | nodeId));
		b.put((byte)0xA0); // Packet type -- fixed
		b.put((byte)actuatorId);
		b.put(v);
		b.position(0);
		byte[] bb = new byte[PACKET_SIZE + 3];
		b.get(bb);
		return bb;
	}

	@Override
	public boolean setup(Object[] b) {
		Socket s = getSockets().get(urn);
		if (s == null || !s.isConnected() || s.isClosed()) {
			s = new Socket();
			try {
				s.connect(new InetSocketAddress(ip, port), 50);
				s.setSoTimeout(50);
				getSockets().put(urn, s);
			} catch (SocketTimeoutException e) {
				return false;
			} catch (IOException e){
				logException(e);
				return false;
			}
			super.buffer = b;
		}
		return true;
	}

	private void sendPacket(byte[] b) {
		Socket s = getSockets().get(urn);
		try {
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.write(b);
		} catch (IOException e) {
			try {
				s.close();
				getSockets().remove(urn);
			} catch (IOException e1) {
				logException(e1);
			}
		}
	}
	
	@Override
	public void run() {
		int val;
		try {
			val = (int) super.buffer[1];
		} catch (NullPointerException e) {
			val = 1;
		}
		if(fsent || val != preVal) {
			byte[] b = buildPacket((byte) val);
			sendPacket(b);
			fsent = false;
			preVal = val;
		}
	}

	@Override
	public void arun() {
		if (!fsent) {
			if(setup(super.buffer)){
				byte[] b = buildPacket((byte) 0);
				sendPacket(b);
				fsent = true;
			}
		}
	}

	@Override
	public void cleanUp() {
		synchronized (Thread.currentThread()) {
			socketMap.get(Thread.currentThread()).forEach((k, v) -> {
				try {
					v.close();
				} catch (IOException e) {
					logException(e);
				}
			});
		}
	}
	
	
	

}
