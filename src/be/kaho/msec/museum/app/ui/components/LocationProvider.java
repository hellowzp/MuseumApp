/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app.ui.components;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.restlet.resource.ClientResource;

import android.util.Log;
import be.kaho.msec.museum.ServerLocation;
import be.kaho.msec.museum.common.Location;
import be.kaho.msec.museum.common.LocationResource;


/*
 * Asynchronous task running in a separate thread, giving feedback on the users location
 */
public class LocationProvider implements Runnable {
	
	/*
	 * Randomly generated userID, simply to have a session between user and server
	 */
	private final static String userID = "" + new Random().nextLong();
	
	private boolean doTask;

	/*
	 * location resource
	 */
	private ClientResource resource;
	
	private LocationResource locResource;

	private Lock lock;	
	private static final int MIN_LOCATIONS_NUMBER = 2;
	//To signal that the collection of locations is almost depleted
	private Condition locationsDepleted;
	
	//Signals that there are locations available to consume
	private Condition locationsAvailable;
	
	
	private LinkedList<Location> locations;
	
	public LocationProvider() {
		
		locations = new LinkedList<Location>();		
		lock = new ReentrantLock();
		locationsDepleted = lock.newCondition();
		locationsAvailable = lock.newCondition();
		
	}
	
	
	@Override
	public void run() {
		
		doTask = true;
		
		while (doTask) {
			
			
			lock.lock();
			
			try {
				
					while(locations.size() > MIN_LOCATIONS_NUMBER)
						locationsDepleted.await();
					
					resource = new ClientResource(ServerLocation.MUSEUM_SERVER.resolve(userID + "/locations"));
					locResource = resource.wrap(LocationResource.class);
					Location[] nextLocations = locResource.getLocations();
					for (int i=0 ; i<nextLocations.length ; i++)
						locations.add(nextLocations[i]);
					
					Log.d(this.getClass().getName(), "Added " + nextLocations.length + " locations to list. Size is now: " + locations.size());
					
					locationsAvailable.signalAll();
				
				
			} catch (InterruptedException ie) {
				Log.e(this.getClass().getName(), "Location service was interrupted when waiting for depleted coordinates.", ie);
			}
			
			lock.unlock();
			
		}
		
	}


	@Override
    protected void finalize()
    {
		resource.release();
    }
	
	/*
	 * Using LinkedBlockingQueue --> take and put operations are already sycnhronised
	 */
	public Location nextLocation()
	{
		Location result;
		
		lock.lock();
		
		try {
			
			while(locations.size() == 0) {
				locationsAvailable.await();
			}
			
			result = locations.removeLast();
			
			if (locations.size() <= MIN_LOCATIONS_NUMBER) {
				locationsDepleted.signal();
			}
			
			Log.d(this.getClass().getName(), "TOOK 1 LOCATION FROM LIST: (" + result.getX() + ", " + result.getY() + "). " 
			    + locations.size() + " remaining...");
			
		} catch (InterruptedException ie) {
			Log.e(this.getClass().getName(), "InterruptedException during retrieval of next location.", ie);
			
			result = null;
		}
		
		lock.unlock();
		return result;
	}

	public void stop()
	{
		doTask = false;
	}
	
}
