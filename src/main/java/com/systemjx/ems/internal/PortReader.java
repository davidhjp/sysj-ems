package com.systemjx.ems.internal;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class PortReader implements SerialPortEventListener {

	private SerialPort sp;
	public PortReader(SerialPort sp) {
		this.sp = sp;
	}
	
	
	@Override
	public void serialEvent(SerialPortEvent ev) {
		if (ev.isRXCHAR() && ev.getEventValue() > 0) {
			try {
				byte[] b = sp.readBytes(ev.getEventValue());
				if((b[0] & 0xFF) == 0xAA) {
					System.out.println("DestGroupID: " + Integer.toHexString(getDestGroupID(b)) + " DestNodeID: " + Integer.toHexString(getDestNodeID(b)) + " " + " SourceGroupID: " + Integer.toHexString(getSourceGroupID(b)) + " SourceNodeID: " + Integer.toHexString(getSourceNodeID(b)) + " " + "T:" + getTemperature(b) + " H: " + getHumidity(b) + " L: " + getLight(b));
				}
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private int getDestGroupID(byte[] b) {
		return b[7] & 0xFF;
	}
	
	private int getDestNodeID(byte[] b) {
		return b[8] & 0xFF;
	}
	
	private int getSourceGroupID(byte[] b) {
		return b[9] & 0xFF;
	}

	private int getSourceNodeID(byte[] b) {
		return b[10] & 0xFF;
	}
	
	private int getTemperature(byte[] b) {
		return (b[12] & 0xFF) + (b[13] & 0xFF) / 100;
	}
	
	private int getHumidity(byte[] b) {
		return (b[14] & 0xFF) + (b[15] & 0xFF) / 100;
	}
	
	private int getLight(byte[] b) {
		return (((b[16] & 0xFF) << 8) + (b[17] & 0xFF)) * 16;
	}
}







