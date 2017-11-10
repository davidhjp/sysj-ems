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

import com.systemj.Container;
import com.systemj.Scheduler;
import com.systemj.ipc.GenericSignalSender;

public class OutputSignal extends GenericSignalSender {
	private static final int PACKET_SIZE = 5;
	protected String ip;
	protected int port;
	protected int actuatorId;
	protected int groupId;
	protected int nodeId;
	protected int packetType;
	protected boolean fsent = true;
	protected Object preVal = Integer.MAX_VALUE;
	protected static Map<Scheduler, Map<String, Socket>> socketMap = new HashMap<>();
	protected String urn;
	protected Scheduler mySch;
	
	public Scheduler getScheduler() {
		Container sc = this;
		while(!(sc instanceof Scheduler)) {
			sc = sc.getParent();
		}
		return (Scheduler)sc;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void configure(Hashtable t) throws RuntimeException {
		this.ip = (String) t.get("IP");
		this.port = Integer.parseInt((String) t.get("Port"));
		this.actuatorId = Integer.parseInt((String) t.get("Actuator"), 16);
		this.groupId = Integer.parseInt((String) t.get("Group"), 16);
		this.nodeId = Integer.parseInt((String) t.get("Node"), 16);
		this.packetType = Integer.parseInt((String) t.getOrDefault("Type", "A0"), 16);
		this.urn = ip + ":" + port;
	}
	
	public Map<String, Socket> getSockets(){
		if(mySch == null) {
			mySch = getScheduler();
			socketMap.putIfAbsent(mySch, new HashMap<>());
		}
		return socketMap.get(mySch);
	}
	
	protected byte[] buildPacket(Object v) {
		ByteBuffer b = ByteBuffer.allocate(PACKET_SIZE + 3);
		b.putShort((short)0xAABB);
		b.put((byte)PACKET_SIZE);
		b.putShort((short)(groupId << 8 | nodeId));
		b.put((byte)packetType); // Packet type, default: 0xA0
		b.put((byte)actuatorId);
		b.put((byte)v);
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
		}
		super.buffer = b;
		return true;
	}

	protected void sendPacket(byte[] b) {
		Socket s = getSockets().get(urn);
		try {
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.write(b);
			dos.flush();
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
		Object val;
		try {
			val = super.buffer[1];
		} catch (NullPointerException e) {
			val = 1;
		}
		if(fsent || val != preVal) {
			byte[] b = buildPacket(val);
			sendPacket(b);
			fsent = false;
			preVal = val;
		}
	}

	@Override
	public void arun() {
		if (!fsent) {
			if(setup(super.buffer)){
				byte[] b = buildPacket(0);
				sendPacket(b);
				fsent = true;
			}
		}
	}

	@Override
	public void cleanUp() {
		synchronized (Thread.currentThread()) {
			getSockets().forEach((k, v) -> {
				try {
					v.close();
				} catch (IOException e) {
					logException(e);
				}
			});
		}
	}
	
	
	

}
