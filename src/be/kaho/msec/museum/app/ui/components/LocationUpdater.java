package be.kaho.msec.museum.app.ui.components;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.AsyncTask;
import android.util.Log;
import be.kaho.msec.museum.common.Location;

public class LocationUpdater extends AsyncTask<Object, Location, Void>
{

	private LocationListener locationListener;
	
	private LocationProvider locationProvider;
	
	private Long locationRefreshInterval = null;
	
	private boolean doTask = false;
	
	private boolean firstRun = true;
	
	private Lock lock;
	private Condition activeCondition;
	
	
	/*
	 * 1st param: Floorplan
	 * 2nd param: condition variable to sync with whether or not the floorplan is available (i.e. in the foreground or not)
	 */
	public LocationUpdater(LocationListener locationListener, Long locationRefreshRate)
    {
		this.locationListener = locationListener;
		this.locationRefreshInterval = locationRefreshRate;
		
		this.lock = new ReentrantLock();
		this.activeCondition = lock.newCondition();
		
		//Spawn a periodic task that "produces" locations
		locationProvider = new LocationProvider();
		
    }
	
	@Override
    protected Void doInBackground(Object... params)
    {
		
		Thread locationProviderThread = new Thread(locationProvider);
		locationProviderThread.setDaemon(true);
		locationProviderThread.start();
		Log.i(this.getClass().getName(), "Started the location provider...");
		//First run --> set doTask to true
		doTask = true;
		firstRun = false;
		
		while (true) {
			
			lock.lock();
			
			try {
				while(!doTask) {
					activeCondition.await();
				}
			} catch(InterruptedException ie) {}
			
			lock.unlock();
			
			Log.i(this.getClass().getName(), "Retrieving next location...");
			//Retrieve location
			Location location = locationProvider.nextLocation();			
			
			Log.d(this.getClass().getName(), "Moving marker to next location...");
			//Push new location as a progress update
			publishProgress(location);
			
			//Wait during interval that is specified in user preferences
			try {
				Thread.sleep(locationRefreshInterval);
			} catch (InterruptedException ie) {
				Log.e(this.getClass().getName(), "Interrupted while sleeping after location refresh ... ", ie);
			}
		}
		

    }
	
	@Override
	protected void onCancelled()
	{
		doTask = false;
		locationProvider.stop();
	    super.onCancelled();
	}
	
	/*
	 * 1ste argument: the new location to update the floorplan with
	 */
	@Override
	protected void onProgressUpdate(Location... values)
	{
	    super.onProgressUpdate(values);
	    Location location = values[0];
	    
		//Move persona to the new location
		locationListener.onLocationChanged(location);
		Log.i(this.getClass().getName(), "LOCATION CHANGED. new location is: (" + location.getX() + ", " + location.getY() + ").");
	}
	
	public void pause()
	{
		lock.lock();
		
		if (!firstRun && doTask) {
			doTask = false;
		}
		
		lock.unlock();
	}
	
	public void resume()
	{
		lock.lock();
		
		if (!firstRun && !doTask) {
			doTask = true;
			activeCondition.signal();
			
		}
		
		lock.unlock();
	}
	
	public boolean isPaused()
	{
		lock.lock();
		
		boolean result = (!firstRun && !doTask);
		
		lock.unlock();
		
		return result;
	}
	
}
