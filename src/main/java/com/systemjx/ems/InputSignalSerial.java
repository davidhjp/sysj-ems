package com.systemjx.ems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.systemj.Signal;
import com.systemj.ipc.GenericSignalReceiver;

public class InputSignalSerial extends GenericSignalReceiver {
	private static final Map<String, List<Signal>> isMap = new HashMap<>();
	final SerialPortConnector spc = new SerialPortConnector();
	
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
		List<Signal> sl = isMap.getOrDefault(fullName, new ArrayList<>());
		sl.add(signal);
		isMap.putIfAbsent(fullName, sl);
	}

	public static Map<String, List<Signal>> getIsmap() {
		return isMap;
	}

	@Override
	public void cleanUp() {
		spc.shutDownPortCheckerThread();
	}

}
