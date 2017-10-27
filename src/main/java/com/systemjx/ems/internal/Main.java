package com.systemjx.ems.internal;

import jssc.SerialPort;

public class Main {

	public static void main(String[] args) throws Exception {
		SerialPort sp = new SerialPort("COM3");
		sp.openPort();
		sp.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		sp.addEventListener(new PortReader(sp), SerialPort.MASK_RXCHAR);
		Thread.sleep(100000);
	}

}
