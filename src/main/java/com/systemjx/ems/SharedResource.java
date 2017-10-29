package com.systemjx.ems;

import static com.systemj.Utils.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SharedResource {
	static final String SENSOR_TEMPERATURE = "temperature";
	static final String SENSOR_HUMIDITY = "humidity";
	static final String SENSOR_LIGHT = "light";
	static final String SENSOR_HEATER_STATE = "heater_state";
	static final String SENSOR_HEATER_POWER = "heater_power";
	
	static final String PACKET_TYPE_1 = "84";  // THL
	static final String PACKET_TYPE_2 = "30";  // Heater state
	static final String PACKET_TYPE_3 = "31";  // Instantaneous power
	
	static final int PACKET_TYPE_THL = 0x84;  // THL
	
	public final static String LOGGER_TYPE_FILE = "com.mbie.logger.filelogger";
	public final static Logger logger = Logger.getLogger(LOGGER_TYPE_FILE);
	
	public static void logException(Throwable e){
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
			e.printStackTrace(pw);
			log.warning(sw.toString());
		} catch (IOException e1) {
			log.severe(e1.getMessage());
		}
	}
	
	
	// Serial port
	private static SerialPort sp;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if(sp != null) {
				try {
					sp.closePort();
					logger.info("SerialPort closed");
				} catch (SerialPortException e) {
					SharedResource.logException(e);
				}
				
			}
		}));
	}

	public static SerialPort getSerialPort() {
		if (sp == null) {
			Optional<String> portO = Optional.ofNullable(System.getProperty("ems.serial.port", null));
			String port = portO.orElseGet(() -> {
				String[] portList = SerialPortList.getPortNames();
				if(portList.length > 0)
					return portList[0];
				return "";
			});
			SharedResource.sp = new SerialPort(port);
			try {
				sp.openPort();
				sp.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				logger.info("SerialPort opened: "+sp.isOpened()+", Port name: "+sp.getPortName());
			} catch (SerialPortException e) {
				SharedResource.logException(e);
			}
		}
		return sp;
	}
	
	public static void openSerialPort(String port) {

	}
	
}
