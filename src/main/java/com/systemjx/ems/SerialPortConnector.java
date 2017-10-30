package com.systemjx.ems;

import static com.systemjx.ems.SharedResource.logException;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialPortConnector {
	private static volatile SerialPort sp = createSerialPort();
	private static ExecutorService portChecker = Executors.newSingleThreadExecutor();
	public final static String LOGGER_TYPE_FILE = "com.mbie.logger.filelogger";
	public final static Logger logger = Logger.getLogger(LOGGER_TYPE_FILE);

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (sp != null) {
				try {
					sp.closePort();
					logger.info("SerialPort closed");
				} catch (SerialPortException e) {
					SharedResource.logException(e);
				}
			}
		}));
		portChecker.submit(SerialPortConnector::daemonSerialPortConnector);
	}
	
	private static void daemonSerialPortConnector() {
		final SerialEventListener sel = new SerialEventListener(InputSignalSerial.getIsmap());
		while (!Thread.currentThread().isInterrupted()) {
			if(sp.isOpened()) {
				final int TIME_OUT = 10; // There should be something received before 10 min otherwise reconn serial port
				if(sel.isLastEventLongerThan(TIME_OUT)) {
					logger.warning("No data received from the serial port for the last "+TIME_OUT+" mins, disconnects: "+sp.getPortName());
					_closeSerialPort();
				}
			}
			if (!sp.isOpened()) {
				sp = createSerialPort();
				try {
					sp.openPort();
					sp.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					sp.addEventListener(sel, SerialPort.MASK_RXCHAR);
					sel.updateLastEvent();
					logger.info("SerialPort opened: " + sp.isOpened() + ", Port name: " + sp.getPortName());
				} catch (SerialPortException e) {
					// logger.info("Could not open serial port "+e.getPortName());
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
		logger.info("Shutting down serial port connection listener");
	}
	
	private static SerialPort createSerialPort() {
		Optional<String> portO = Optional.ofNullable(System.getProperty("ems.serial.port", null));
		String port = portO.orElseGet(() -> {
			String[] portList = SerialPortList.getPortNames();
			if (portList.length > 0)
				return portList[0];
			return "";
		});
		return new SerialPort(port);
	}
	
	public SerialPort getSerialPort() {
		return sp;
	}
	
	public void shutDownPortCheckerThread() {
		portChecker.shutdownNow();
		try {
			portChecker.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logException(e);
		}
	}
	
	private static void _closeSerialPort() {
		try {
			sp.closePort();
			logger.info("Closed SerialPort "+sp.getPortName());
		} catch (SerialPortException e) {
			logger.warning("Could not close serial port "+e.getPortName()+", recreating the instance");
			sp = createSerialPort();
		}
	}
	
	public void closeSerialPort() {
		_closeSerialPort();
	}
	
}
