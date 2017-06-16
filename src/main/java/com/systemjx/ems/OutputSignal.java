package com.systemjx.ems;

import java.util.Hashtable;

import com.systemj.ipc.GenericSignalSender;

public class OutputSignal extends GenericSignalSender {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void configure(Hashtable t) throws RuntimeException {
		Hashtable<String, String> tb = t;
	}

	@Override
	public void run() {
		
	}

}
