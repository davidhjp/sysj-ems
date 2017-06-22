package com.systemjx.ems;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.systemj.ipc.GenericSignalReceiver;

public class InputSignal extends GenericSignalReceiver {
	private static volatile boolean skipRun = false;
	protected static final ExecutorService es = Executors.newCachedThreadPool();
	protected static final Map<String, PacketWorker> tasks = new HashMap<>();
	@SuppressWarnings({ "rawtypes" })
	@Override
	public void configure(Hashtable tb) throws RuntimeException {
		String ip = (String) tb.get("IP");
		int port = Integer.parseInt((String) tb.get("Port"));
		String urn = ip + ":" + port;
		PacketWorker pw = tasks.getOrDefault(urn, new PacketWorker(ip, port));
		pw.addSignal(tb);
		tasks.put(urn, pw);
	}



	@Override
	public synchronized void getBuffer(Object[] obj) {
		super.getBuffer(obj);
		// Clearing the local copy of the signal buffer so signal lasts for only one tick
		super.clearSignal();
	}



	@Override
	public void run() {
		if (!skipRun) {
			synchronized (InputSignal.class) {
				if (!es.isShutdown()) {
					tasks.forEach((k, v) -> es.submit(v));
					tasks.clear();
					skipRun = true;
				}
			}
		}
	}

	
	@Override
	public boolean isShutDown() {
		return shutdown;
	}

	@Override
	public void cleanUp() {
		synchronized (InputSignal.class) {
			if (!es.isShutdown()) {
				es.shutdownNow();
				try {
					es.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		shutdown = true;
	}
	
}
