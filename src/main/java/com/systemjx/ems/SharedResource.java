package com.systemjx.ems;

import static com.systemj.Utils.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SharedResource {
	static final String TYPE_TEMPERATURE = "temperature";
	static final String TYPE_HUMIDITY = "humidity";
	static final String TYPE_LIGHT = "light";
	
	public static void logException(Throwable e){
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
			e.printStackTrace(pw);
			log.warning(pw.toString());
		} catch (IOException e1) {
			log.severe(e1.getMessage());
		}
	}
}
