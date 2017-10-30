package com.systemjx.ems;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

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
			logger.warning(sw.toString());
		} catch (IOException e1) {
			logger.severe(e1.getMessage());
		}
	}
}
