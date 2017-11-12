package testcases;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runners.model.TestTimedOutException;

import com.systemj.Container;
import com.systemj.CyclicScheduler;
import com.systemj.Signal;
import com.systemjx.ems.InputTCP;
import com.systemjx.ems.OutputTCP;

import testcases.MockTCPServer.Result;

public class TestRPi {

	@Test
	public void testInput() throws InterruptedException, TestTimedOutException {
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
		MockTCPServer.runClient();
		Thread.sleep(100);
		es.shutdown();
		boolean done = es.awaitTermination(1, TimeUnit.SECONDS);
		if(!done)
			throw new TestTimedOutException(1, TimeUnit.SECONDS);
		Object[] buffer = new Object[2];
		tcp.getBuffer(buffer);
		assertEquals(MockTCPServer.RESULT_CLIENT, buffer[1]);
	}
	
	@Test
	public void testOutput() throws InterruptedException, ExecutionException, TestTimedOutException {
		OutputTCP tcp = new OutputTCP();
		Signal signal = new Signal("WSet", Signal.OUTPUT);
		Hashtable<String, Object> conf = new Hashtable<>();
		conf.put("IP", MockTCPServer.IP);
		conf.put("Port", ""+MockTCPServer.PORT);
		conf.put("Group", ""+MockTCPServer.GROUP);
		conf.put("Node", ""+MockTCPServer.NODE);
		conf.put("SubID", ""+MockTCPServer.SUB_ID);
		conf.put("instance", signal);
		tcp.configure(conf);
		signal.setClient(tcp);
		CyclicScheduler cs = new CyclicScheduler();
		Container.connect(signal, tcp);
		Container.connect(cs, signal);
		
		ExecutorService es = Executors.newCachedThreadPool();
		Future<Result> fut = es.submit(MockTCPServer::runServer);
		Thread.sleep(100);
		signal.setPresent();
		signal.setValue(MockTCPServer.RESULT_SERVER);
		signal.sethook();
		es.shutdown();
		boolean done = es.awaitTermination(1, TimeUnit.SECONDS);
		if(!done)
			throw new TestTimedOutException(1, TimeUnit.SECONDS);
		Result r = fut.get();
		assertEquals(MockTCPServer.RESULT_SERVER, r.data);
		assertEquals(MockTCPServer.GROUP, r.group);
		assertEquals(MockTCPServer.NODE, r.node);
		assertEquals(MockTCPServer.SUB_ID, r.type);
		
	}
}
