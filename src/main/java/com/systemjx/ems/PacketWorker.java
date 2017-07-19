package com.systemjx.ems;

import static com.systemj.Utils.log;
import static com.systemjx.ems.SharedResource.SENSOR_HUMIDITY;
import static com.systemjx.ems.SharedResource.SENSOR_LIGHT;
import static com.systemjx.ems.SharedResource.SENSOR_TEMPERATURE;
import static com.systemjx.ems.SharedResource.PACKET_TYPE_1;
import static com.systemjx.ems.SharedResource.PACKET_TYPE_2;
import static com.systemjx.ems.SharedResource.PACKET_TYPE_3;
import static com.systemjx.ems.SharedResource.SENSOR_HEATER_POWER;
import static com.systemjx.ems.SharedResource.SENSOR_HEATER_STATE;
import static com.systemjx.ems.SharedResource.logException;

import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.systemj.Signal;

public class PacketWorker implements Runnable {
	protected String ip;
	protected int port;
	protected byte[] magic = new byte[3];
	protected Map<String, List<Signal>> is = new HashMap<>();
	// protected Map<String, List<Signal>> os = new HashMap<>();

	public PacketWorker(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public static String buildID(String groupId, String nodeId, String sensor) {
		return groupId + "-" + nodeId + "-" + sensor;
	}

	public static short getShort(byte[] b) {
		// Returning 16-bit short value
		return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).getShort();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addSignal(Map opt) {
		String id = buildID((String) opt.get("Group"), (String) opt.get("Node"), (String) opt.getOrDefault("Sensor", "dedicated"));
		Signal signal = (Signal) opt.get("instance");
		if (signal.getDir() == Signal.INPUT) {
			List<Signal> l = is.getOrDefault(id, new ArrayList<Signal>());
			l.add(signal);
			is.put(id, l);
		}
	}

	private static String getGroup(byte[] b) {
		return String.format("%02x", b[0]).toUpperCase();
	}

	private static String getNode(byte[] b) {
		return String.format("%02x", b[1]).toUpperCase();
	}

	private static String getPacketType(byte[] b) {
		return String.format("%02x", b[2]).toUpperCase();
	}
	
	private int getHeaterState(byte[] b) {
		return b[3];
	}

	private static float getTemperature(byte[] b) {
		return b[3] + b[4] / 100;
	}

	private static float getHumidity(byte[] b) {
		return b[5] + b[6] / 100;
	}

	private static float getLight(byte[] b) {
		return ((b[7] << 8) + b[8]) * 16;
	}
	
	private int getHeaterPower(byte[] b) {
		return ByteBuffer.wrap(b, 3, 2).order(ByteOrder.BIG_ENDIAN).getShort();
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
							if ((getShort(Arrays.copyOfRange(magic, 0, 2)) & 0xffff) == 0xAABB) {
								int len = magic[2];
								byte[] payload = new byte[len];
								// Reading the remaining bytes
								is.readFully(payload);

								parsePacket(payload);
							}
						} catch (SocketTimeoutException e) {
						}
					}
				}
			} catch (SocketTimeoutException e) {
			} catch (Exception e) {
				logException(e);
			}
		}
	}

	private void parsePacket(byte[] payload) {
		String pType = getPacketType(payload);
		switch (pType) {
		case PACKET_TYPE_1: // 84
			// Building IDs for the Map
			String idTemp = buildID(getGroup(payload), getNode(payload), SENSOR_TEMPERATURE);
			String idHumidity = buildID(getGroup(payload), getNode(payload), SENSOR_HUMIDITY);
			String idLight = buildID(getGroup(payload), getNode(payload), SENSOR_LIGHT);
			log.info("Resolved IDs for fetching signals : \n" + idTemp + "\n" + idHumidity + "\n" + idLight);

			// Extracting a number for each sensor input
			float t = getTemperature(payload);
			float h = getHumidity(payload);
			float l = getLight(payload);
			{
				// Setting values for all signals
				List<Signal> signals = this.is.getOrDefault(idHumidity, new ArrayList<Signal>());
				signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, h }));
				signals = this.is.getOrDefault(idLight, new ArrayList<Signal>());
				signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, l }));
				signals = this.is.getOrDefault(idTemp, new ArrayList<Signal>());
				signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, t }));
			}
			log.info("Received data - Temperature: " + t + "Humidity: " + h + " Light: " + l);
			break;
		case PACKET_TYPE_2: // 30, heater state
			String idHS = buildID(getGroup(payload), getNode(payload), SENSOR_HEATER_STATE);
			int st = getHeaterState(payload);
			{
				// Setting values for all signals
				List<Signal> signals = this.is.getOrDefault(idHS, new ArrayList<Signal>());
				signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, st }));
			}
			log.info("Received heater state: " + idHS + " (" + st + ")");
			break;
		case PACKET_TYPE_3: // 31, instantaneous power
			String idP = buildID(getGroup(payload), getNode(payload), SENSOR_HEATER_POWER);
			int p = getHeaterPower(payload);
			{
				// Setting values for all signals
				List<Signal> signals = this.is.getOrDefault(idP, new ArrayList<Signal>());
				signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, p }));
			}
			log.info("Received P: " + idP+ " (" + p + ")");
			break;
		default:
			log.warning("Unexpected packet type: " + pType);
			break;
		}
	}
}
