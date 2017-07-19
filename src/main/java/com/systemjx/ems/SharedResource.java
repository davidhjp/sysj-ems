package com.systemjx.ems;

import static com.systemj.Utils.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SharedResource {
	static final String SENSOR_TEMPERATURE = "temperature";
	static final String SENSOR_HUMIDITY = "humidity";
	static final String SENSOR_LIGHT = "light";
	static final String SENSOR_HEATER_STATE = "heater_state";
	static final String SENSOR_HEATER_POWER = "heater_power";
	
	static final String PACKET_TYPE_1 = "84";  // THL
	static final String PACKET_TYPE_2 = "30";  // Heater state
	static final String PACKET_TYPE_3 = "31";  // Instantaneous power
	
	public static void logException(Throwable e){
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
			e.printStackTrace(pw);
			log.warning(pw.toString());
		} catch (IOException e1) {
			log.severe(e1.getMessage());
		}
	}
}
