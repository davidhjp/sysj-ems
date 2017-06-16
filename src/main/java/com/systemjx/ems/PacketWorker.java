package com.systemjx.ems;

import static com.systemjx.ems.SharedResource.TYPE_HUMIDITY;
import static com.systemjx.ems.SharedResource.TYPE_LIGHT;
import static com.systemjx.ems.SharedResource.TYPE_TEMPERATURE;
import static com.systemj.Utils.log;

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
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

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

	public static String buildID(String groupId, String nodeId, String type) {
		return groupId + "-" + nodeId + "-" + type;
	}

	public static short getShort(byte[] b) {
		// Returning 16-bit short value
		return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).getShort();
	}

	@SuppressWarnings("rawtypes")
	public void addSignal(Map opt) {
		String id = buildID((String) opt.get("Group"), (String) opt.get("Node"), (String) opt.get("Type"));
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

	private static float getTemperature(byte[] b) {
		return b[3] + b[4] / 100;
	}

	private static float getHumidity(byte[] b) {
		return b[5] + b[6] / 100;
	}

	private static float getLight(byte[] b) {
		return ((b[7] << 8) + b[8]) * 16;
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
								// Reading the first 8 bytes
								is.readFully(payload);

								// Building IDs for the Map
								String idTemp = buildID(getGroup(payload), getNode(payload), TYPE_TEMPERATURE);
								String idHumidity = buildID(getGroup(payload), getNode(payload), TYPE_HUMIDITY);
								String idLight = buildID(getGroup(payload), getNode(payload), TYPE_LIGHT);
								log.info("Resolved IDs for fetching signals : \n" + idTemp + "\n" + idHumidity + "\n"
										+ idLight);

								// Extracting a number for each sensor input
								float t = getTemperature(payload);
								float h = getHumidity(payload);
								float l = getLight(payload);

								// Setting values for all signals
								List<Signal> signals = this.is.getOrDefault(idHumidity, new ArrayList<Signal>());
								signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, h }));
								signals = this.is.getOrDefault(idLight, new ArrayList<Signal>());
								signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, l }));
								signals = this.is.getOrDefault(idTemp, new ArrayList<Signal>());
								signals.stream().forEach(s -> s.getServer().setBuffer(new Object[] { true, t }));
								log.info("Received data - Temperature: " + t + "Humidity: " + h + " Light: " + l);
							}
						} catch (SocketTimeoutException e) {
						}
					}
				}
			} catch (SocketTimeoutException e) {
			} catch (Exception e) {
				log.info(e.getMessage() + "\n"
						+ Stream.of(e.getStackTrace()).map(v -> v.toString()).reduce((r, l) -> r + l + "\n").get());
			}
		}
	}

}
