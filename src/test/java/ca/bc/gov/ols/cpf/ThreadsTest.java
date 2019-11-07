package ca.bc.gov.ols.cpf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.store.Buffer;

import ca.bc.gov.ols.geocoder.Geocoder;
import ca.bc.gov.ols.geocoder.GeocoderFactory;
import ca.bc.gov.ols.geocoder.IGeocoder;

public class ThreadsTest {
	
	private static final int ITERATION_COUNT = 50;
	private static final int NUM_THREADS = 30;
	
	private final Object startSync = new Object();
	private final Channel<Boolean> startChannel = new Channel<Boolean>(new Buffer<Boolean>(
			NUM_THREADS));
	private final Channel<Boolean> stopChannel = new Channel<Boolean>(new Buffer<Boolean>(
			NUM_THREADS));
	
	private GeocoderFactory factory;
	private IGeocoder geocoder;
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		ThreadsTest test = new ThreadsTest();
		test.execute();
	}
	
	public void execute() throws SQLException, ClassNotFoundException {
		factory = new GeocoderFactory();
		//factory.setUrl("jdbc:oracle:thin:@//192.168.50.44:1521/orcl.animals");
		//factory.setUsername("bgeo");
		//factory.setPassword("bgeo");
		
		geocoder = factory.getGeocoder();
		for(int i = 0; i < NUM_THREADS; i++) {
			Runnable runnable = createRunnable(i);
			Thread thread = new Thread(runnable, "Runner" + i);
			thread.start();
		}
		System.out.println("Waiting for start...");
		waitForAllThreadsToStart();
		System.out.println("Waiting for stop...");
		waitForAllThreadsToStop();
		
		System.out
				.println("All threads completed thier job. Press 'Enter' to continue with cleaning");
		readLine();
		
		System.out.println("Cleaning...");
		((Geocoder)geocoder).close();
		geocoder = null;
		factory = null;
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.out.println("Done! Press 'Enter' to exit");
		readLine();
	}
	
	private void readLine() {
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		try {
			bufferRead.readLine();
		} catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private Runnable createRunnable(final int i) {
		return new Runnable() {
			@Override
			public void run() {
				threadStarted();
				for(int j = 0; j < ITERATION_COUNT; j++) {
					GeocoderPlugin plugin = buildQuery("805 Douglas Ave, Nanaimo, BC");
					plugin.execute();
					plugin = buildQuery("815 Douglas Ave, Nanaimo, BC");
					plugin.execute();
					plugin = buildQuery("5th St and Bruce Ave, Nanaimo, BC");
					plugin.execute();
				}
				threadStopped();
			}
			
			private GeocoderPlugin buildQuery(String address) {
				GeocoderPlugin plugin = new GeocoderPlugin();
				plugin.setGeocoder(geocoder);
				plugin.setAddressString(address);
				plugin.setMaxResults(1);
				plugin.setMinScore(1);
				plugin.setSetBack(0);
				plugin.setEcho(true);
				plugin.setInterpolation("adaptive");
				return plugin;
			}
		};
	}
	
	public void threadStarted() {
		startChannel.write(true);
		synchronized(startSync) {
			try {
				startSync.wait();
			} catch(final InterruptedException e) {
			}
		}
	}
	
	public void threadStopped() {
		stopChannel.write(true);
	}
	
	protected void waitForAllThreadsToStart() {
		for(int i = 0; i < NUM_THREADS; i++) {
			startChannel.read();
		}
		synchronized(startSync) {
			startSync.notifyAll();
		}
	}
	
	protected void waitForAllThreadsToStop() {
		for(int i = 0; i < NUM_THREADS; i++) {
			stopChannel.read();
		}
	}
	
}
