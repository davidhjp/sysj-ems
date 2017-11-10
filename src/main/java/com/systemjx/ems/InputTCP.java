package com.systemjx.ems;

public class InputTCP extends InputSignal {

	@Override
	protected PacketWorker getPacketWorker(String ip, int port) {
		return new NativePacketReceiver(ip, port);
	}
	
}
