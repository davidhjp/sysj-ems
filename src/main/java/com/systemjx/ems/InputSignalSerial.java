package com.systemjx.ems;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.systemj.Signal;
import com.systemj.ipc.GenericSignalReceiver;

import jssc.SerialPort;
import jssc.SerialPortException;

public class InputSignalSerial extends GenericSignalReceiver {
	private static Map<String, Signal> isMap = new HashMap<>();
	private static boolean setEvent = false;
	
	@Override
	public synchronized void getBuffer(Object[] obj) {
		super.getBuffer(obj);
		// Clearing the local copy of the signal buffer so signal lasts for only one tick
		super.clearSignal();
	}
	
	public static String buildID(int groupId, int nodeId, String sensor) {
		return groupId + "-" + nodeId + "-" + sensor;
	}
	
	public static String buildID(String groupId, String nodeId, String sensor) {
		return Integer.parseInt(groupId) + "-" + Integer.parseInt(nodeId) + "-" + sensor;
	}
	
	@Override
	public void configure(Hashtable arg) throws RuntimeException {
		Signal signal = (Signal)arg.get("instance");
		String fullName = buildID((String)arg.get("Group"), (String)arg.get("Node"), (String)arg.get("Sensor"));
		isMap.putIfAbsent(fullName, signal);
		final SerialPort sp = SharedResource.getSerialPort();
		synchronized (InputSignalSerial.class) {
			if (!setEvent) {
				try {
					sp.addEventListener(new SerialEventListener(isMap), SerialPort.MASK_RXCHAR);
				} catch (SerialPortException e) {
					SharedResource.logException(e);
				}
				setEvent = true;
			}
		}
	}

}
