package com.systemjx.ems;

import java.nio.ByteBuffer;
import java.util.Hashtable;

public class OutputTCP extends OutputSignal {
	private int packetId;
	private static final int PACKET_BASE_SIZE = 4;
	
	protected static int sizeof(Class dataType) {
		if (dataType == null)
			throw new NullPointerException();

		if (dataType == int.class || dataType == Integer.class)
			return 4;
		if (dataType == short.class || dataType == Short.class)
			return 2;
		if (dataType == byte.class || dataType == Byte.class)
			return 1;
		if (dataType == char.class || dataType == Character.class)
			return 2;
		if (dataType == long.class || dataType == Long.class)
			return 8;
		if (dataType == float.class || dataType == Float.class)
			return 4;
		if (dataType == double.class || dataType == Double.class)
			return 8;
		return 4;
	}
	
	protected static byte[] getByteArray(Object b) {
		if (b instanceof Number) {
			Class<? extends Number> dataType = ((Number) b).getClass();
			ByteBuffer buffer = ByteBuffer.allocate(sizeof(dataType));
			if (dataType == int.class || dataType == Integer.class)
				return buffer.putInt((int) b).array();
			if (dataType == short.class || dataType == Short.class)
				return buffer.putShort((short) b).array();
			if (dataType == byte.class || dataType == Byte.class)
				return buffer.put((byte) b).array();
			if (dataType == long.class || dataType == Long.class)
				return buffer.putLong((long) b).array();
			if (dataType == float.class || dataType == Float.class)
				return buffer.putFloat((float) b).array();
			if (dataType == double.class || dataType == Double.class)
				return buffer.putDouble((double) b).array();
		} 
		return b.toString().getBytes();
	}

	@Override
	public void configure(Hashtable args) throws RuntimeException {
		ip = (String)args.get("IP");
		port = Integer.parseInt((String)args.get("Port"));
		groupId = Integer.parseInt((String)args.get("Group"));
		packetId = Integer.parseInt((String)args.get("SubID"));
		nodeId = Integer.parseInt((String)args.get("Node"));
		this.urn = ip + ":" + port;
	}

	@Override
	protected byte[] buildPacket(Object v) {
		final int packetDataSize = v instanceof Number ? sizeof(v.getClass()) : v.toString().getBytes().length;
		ByteBuffer b = ByteBuffer.allocate(packetDataSize + 1 + PACKET_BASE_SIZE);
		b.put((byte)0xBB);
		b.putShort((short)(nodeId << 8 | groupId));
		b.put((byte)(packetDataSize + 1));
		b.put((byte)packetId);
		byte[] data = getByteArray(v);
		b.put(data);
		b.position(0);
		byte[] bb = new byte[packetDataSize + 1 + PACKET_BASE_SIZE];
		b.get(bb);
		return bb;
	}

	
	@Override
	public void run() {
		Object val;
		try {
			val = (Object) super.buffer[1];
		} catch (NullPointerException e) {
			val = 0;
		}
		byte[] b = buildPacket(val);
		sendPacket(b);
	}
	
	@Override 
	public void arun() {
	}
	

}
