package com.systemjx.ems;

import static com.systemjx.ems.SharedResource.es;
import static com.systemjx.ems.SharedResource.tasks;

import java.util.Hashtable;

import com.systemj.ipc.GenericSignalReceiver;

public class InputSignal extends GenericSignalReceiver {
	private static volatile boolean skipRun = false;
	
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
			synchronized (Class.class) {
				tasks.forEach((k, v) -> es.submit(v));
				tasks.clear();
				skipRun = true;
			}
		}
	}

}
