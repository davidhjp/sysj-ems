package com.systemjx.ems;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedResource {
	static final String TYPE_TEMPERATURE = "temperature";
	static final String TYPE_HUMIDITY = "humidity";
	static final String TYPE_LIGHT = "light";
	static final Map<String, PacketWorker> tasks = new HashMap<>();
	
	static final Map<String, Socket> sockets = new HashMap<>();
	static final ExecutorService es = Executors.newCachedThreadPool();
}
