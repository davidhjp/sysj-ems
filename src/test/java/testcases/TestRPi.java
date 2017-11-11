package testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.systemj.Signal;
import com.systemjx.ems.InputTCP;

public class TestRPi {

	@Test
	public void testInput() throws InterruptedException {
		InputTCP tcp = new InputTCP();	
		Signal signal = new Signal("ldcFrq", Signal.INPUT);
		Hashtable<String, Object> conf = new Hashtable<>();
		conf.put("IP", MockTCPServer.IP);
		conf.put("Port", ""+MockTCPServer.PORT);
		conf.put("Group", ""+MockTCPServer.GROUP);
		conf.put("Node", ""+MockTCPServer.NODE);
		conf.put("SubID", ""+MockTCPServer.SUB_ID);
		conf.put("instance", signal);
		tcp.configure(conf);
		signal.setServer(tcp);
		
		ExecutorService es = Executors.newCachedThreadPool();
		es.submit(tcp);
		MockTCPServer server = new MockTCPServer();
		server.run();
		Thread.sleep(100);
		es.shutdown();
		es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		Object[] buffer = new Object[2];
		tcp.getBuffer(buffer);
		assertEquals(MockTCPServer.RESULT, buffer[1]);
	}
}
