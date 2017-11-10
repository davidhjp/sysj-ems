package com.systemjx.ems;

import java.util.Map;

public interface PacketWorker extends Runnable {
	
	public void addSignal(Map config);
	public String getGroup(byte[] b);
	public String getNode(byte[] b);
	public String getPacketType(byte[] b);
}
