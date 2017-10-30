package com.systemjx.ems;

import java.util.Hashtable;

import com.systemj.ipc.GenericSignalSender;

public class OutputSignalSerial extends GenericSignalSender {
	final SerialPortConnector spc = new SerialPortConnector();

	@Override
	public void configure(Hashtable arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void cleanUp() {
		spc.closeSerialPort();
	}

}
