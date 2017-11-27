package com.systemjx.ems;

import static com.systemjx.ems.SharedResource.logger;

import java.io.DataInputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.systemj.Signal;

public class NativePacketReceiver extends CompactPacketReceiver {
	protected byte[] magic = new byte[2];
	
	public NativePacketReceiver(String ip, int port) {
		super(ip,port);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addSignal(Map opt) {
		String id = buildID((String) opt.get("Group"), (String) opt.get("Node"), (String) opt.getOrDefault("SubID", "dedicated"));
		Signal signal = (Signal) opt.get("instance");
		if (signal.getDir() == Signal.INPUT) {
			List<Signal> l = is.getOrDefault(id, new ArrayList<Signal>());
			l.add(signal);
			is.putIfAbsent(id, l);
		}
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress(ip, port), 1000);
				socket.setSoTimeout(1000);
				try (DataInputStream is = new DataInputStream(socket.getInputStream())) {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							is.readFully(magic);
							if ((magic[0] & 0xff) == 0xAA) {
								int len = magic[1];
								byte[] payload = new byte[len];
								// Reading the remaining bytes
								is.readFully(payload);

								parsePacket(payload);
							}
						} catch (SocketTimeoutException e) {
						}
					}
				}
			} catch (SocketTimeoutException | ConnectException e) {
			} catch (Exception e) {
				logger.fine(e.getMessage());
			}
		}
	}
	
	public final static int PACKET_TYPE_FRQ     = 12;
	public final static int PACKET_TYPE_POWER   = 13;
	public final static int PACKET_TYPE_CURRENT = 14;
	public final static int PACKET_TYPE_VOLTAGE = 15;
	
	protected String buildID(int groupId, int nodeId, int subId) {
		return super.buildID(Integer.toString(groupId), Integer.toString(nodeId), Integer.toString(subId));
	}
	
	private void setValue(List<Signal> signals, Object value) {
		signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, value }));
	}
	
	private void parsePacket(byte[] payload) {
		int pType = Integer.parseInt(getPacketType(payload), 16);
		int group = Integer.parseInt(getGroup(payload), 16);
		int node = Integer.parseInt(getNode(payload), 16);
		String id = buildID(group, node, pType);
		List<Signal> signals = this.is.getOrDefault(id, new ArrayList<Signal>());
		switch (pType) {
		case PACKET_TYPE_POWER:
		case PACKET_TYPE_CURRENT:
		case PACKET_TYPE_VOLTAGE:
			setValue(signals, (float)getShort(payload));
			break;
		case PACKET_TYPE_FRQ: {
			setValue(signals, (int)getShort(payload));
			break;
		}
		default:
			logger.warning("Unexpected packet type: " + pType);
			break;
		}
		logger.fine("Received Group: " + group + " Node: " + node + " Type: " + pType);
	}

	@Override
	public String getGroup(byte[] b) {
		return String.format("%02x", b[7]).toUpperCase();
	}

	@Override
	public String getNode(byte[] b) {
		return String.format("%02x", b[8]).toUpperCase();
	}

	@Override
	public String getPacketType(byte[] b) {
		return String.format("%02x", b[9]).toUpperCase();
	}
	
	private int getByte(byte[] b) {
		return b[10] & 0xff;
	}
	
	@Override
	protected short getShort(byte[] b) {
		return (short)(((b[10] & 0xff) << 8) | b[11] & 0xff);
	}
}
