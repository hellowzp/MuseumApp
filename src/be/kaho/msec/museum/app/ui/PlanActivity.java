/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import be.kaho.msec.museum.ServerLocation;
import be.kaho.msec.museum.app.R;
import be.kaho.msec.museum.app.Util;
import be.kaho.msec.museum.app.ui.components.LocationListener;
import be.kaho.msec.museum.app.ui.components.LocationUpdater;
import be.kaho.msec.museum.common.Artifact;
import be.kaho.msec.museum.common.ArtifactResource;
import be.kaho.msec.museum.common.Location;


@SuppressWarnings("deprecation")
public class PlanActivity extends Activity implements LocationListener {


	/*
	 * width of the museum in centimeters
	 */
	private static final int plan_width = 2700;
	/*
	 * height of the museum in centimeters
	 */
	private static final int plan_height = 4500;
	
	/*
	 * Simulation of coordinates enabled (i.e. moving persona...)
	 */
	private Boolean simulateCoords = null;
	
	private LocationUpdater locationUpdater = null;
	
	/*
	 * list of artifacts with reference
	 */
	private List<Artifact> artifacts;
	/*
	 * marker to pinpoint the user's location
	 */
	private ImageView persona;

	private SharedPreferences sharedPrefs;
	
	//Reference to this floorPlan when working out of the scope
	private PlanActivity floorPlan = this;
	
	private boolean floorplanReady = false;
	

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
//		Log.i(this.getClass().getSimpleName(), "OnCreate");
		
		org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;
		
		//Display an indeterminate progress dialog
		final ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.initialising_floorplan), true);
		
		setContentView(R.layout.plan);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		final String lang = Util.getSystemLanguage();
		
		simulateCoords = sharedPrefs.getBoolean("simulate_location", true);
		//If the simulation of coordinates is enabled, create a location updater
		if (simulateCoords) {
			
			persona = loadMarker();
			
			Long locationRefreshInterval = Long.parseLong(sharedPrefs.getString("location_interval", "1000"));
			locationUpdater = new LocationUpdater(floorPlan, locationRefreshInterval);
		}
		
		
		
		//Populate the floorplan with artifacts
		AsyncTask<Void, Void, List<Artifact>> floorplanInitialiser = 
				new AsyncTask<Void, Void, List<Artifact>>() {
					private ResourceException e = null;
					@Override
					protected List<Artifact> doInBackground(Void... params) {
						try {
							return downloadArtifacts(lang);
						} catch (ResourceException e){
							this.e = e;
							return null;
						}
					}
		
					@Override
					protected void onPostExecute(List<Artifact> result) {
						super.onPostExecute(result);
						if (result == null)
							onError(e);
						else {
							
							artifacts = loadArtifacts(result);
							floorplanReady = true;
						}
						
						dialog.dismiss();
					}
		};
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			floorplanInitialiser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
		    floorplanInitialiser.execute();
		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {

		this.resume();
		super.onResume();
//		Log.i(this.getClass().getSimpleName(), "OnResume");
	}
	
	private void resume()
	{
		
		if (simulateCoords) {
			
			if (locationUpdater.isPaused()) {

				Log.i(this.getClass().getName(), "RESUMING FLOORPLAN FROM PAUSED STATE");
				Log.i(this.getClass().getName(), "Resuming the location updater and enabling the map...");
				floorplanReady = true;
				locationUpdater.resume();
				
			} else {
				
				Log.i(this.getClass().getName(), "STARTING FLOORPLAN");
				Log.i(this.getClass().getName(), "Executing the location updater for the first time...");
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					locationUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
				    locationUpdater.execute();
			}
			
		}
	}
	
	private void pause()
	{
		if (simulateCoords) {

			Log.d(this.getClass().getName(), "onPause called. Pausing location updater and disabling floorplan...");
			floorplanReady = false;
			locationUpdater.pause();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		
		this.pause();
		super.onPause();
//		Log.i(this.getClass().getSimpleName(), "OnPause");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (simulateCoords) {
			
			locationUpdater.cancel(false);
		}
		super.onDestroy();
	}

	/**
	 * @param planActivity
	 * @return an ImageView of the Marker placed on the Activity
	 */
	private ImageView loadMarker() {
		
		ImageView v = new ImageView(this);
		v.setImageResource(R.drawable.man);
		v.setVisibility(View.GONE);
		AbsoluteLayout l = (AbsoluteLayout)findViewById(R.id.plattegrondLayout);
		l.addView(v);
		return v;
	}

	/**
	 * Set the position of the marker
	 * @param location
	 */
	private Point setMarker(Location location) {
		
		Point p = locationToPoint(location);
		int w = persona.getDrawable().getIntrinsicWidth();
		int h = persona.getDrawable().getIntrinsicHeight();
		persona.setVisibility(View.VISIBLE);
		persona.setLayoutParams(positionObject(p, w, h));
		return p;
	}

	/*
	 * ****************  HANDLE ARTIFACTS: **********************
	 */

	/**
	 * @param artifacts, a list of artifacts of the museum to be loaded on the map
	 */
	private List<Artifact>  loadArtifacts(List<Artifact> artifacts) {
		//update floorplan
		for (Artifact a : artifacts){

			addArtifactToActivity(a);
		}
		return artifacts;
	}

	/**
	 * add an artifact a, with reference ref to the map
	 * @param ref
	 * @param a
	 */
	private void addArtifactToActivity(Artifact a){

		ImageButton b = new ImageButton(this);
		//set image
		int resID = 0;
		switch (a.getMediaType()){
		case Audio:
			resID = R.drawable.music;
			break;
		case Image:
			resID = R.drawable.photo;
			break;
		case Video:
			resID = R.drawable.video;
			break;
		}
		Drawable image = getResources().getDrawable(resID);
		b.setImageDrawable(image);
		b.setPadding(0, 0, 0, 0);
		b.setScaleType(ScaleType.FIT_XY);

		//set on click action
		b.setTag(a);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showArtifactActivity((Artifact) v.getTag());
			}
		});

		//add object to activity
		AbsoluteLayout l = (AbsoluteLayout)findViewById(R.id.plattegrondLayout);
		Point p = locationToPoint(a.getLocation());
		int w = b.getDrawable().getIntrinsicWidth();
		int h = b.getDrawable().getIntrinsicHeight();
		l.addView(b, positionObject(p, w, h));

	};
	/**
	 * @param lang Language of the artifacts
	 * @return a list of artifacts downloaded from the server
	 * 
	 */
	private List<Artifact> downloadArtifacts(String lang) throws ResourceException {
		
		List<Artifact> artifacts = null;
		ClientResource resource = null;
		try {
			//fetch references to artifacts
			resource = new ClientResource(ServerLocation.MUSEUM_SERVER.resolve(lang + "/artifacts"));
			ArtifactResource asr = resource.wrap(ArtifactResource.class);
			artifacts = Arrays.asList(asr.getArtifacts());

		} catch (ResourceException e){
			artifacts = new ArrayList<Artifact>();
			throw e;
		} finally {
			//release resource
			if (resource != null)
				resource.release();
		}
		return artifacts;
	}

	/**
	 * Start the activity showing an artifact
	 * @param ref
	 */
	private void showArtifactActivity(Artifact artifact) {
		//show Artifact
		Intent intent = new Intent();
		intent.putExtra("artifact", artifact);
		intent.setClass(this, ArtifactActivity.class);
		startActivity(intent);
	}



	/*
	 * (non-Javadoc)
	 * @see be.kaho.msec.museum.app.ui.components.LocationListener#onLocationChanged(be.kaho.msec.museum.common.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		
		if (simulateCoords) {
			
			if (floorplanReady) {
				setMarker(location);
				
				//Check and display alert or immediately forward to artifact activity
				checkNearbyArtifacts(location);
			} else {
				Log.w(this.getClass().getName(), "Persona has not been moved, floorplan is still initialising...");
			}
			
				
		}
			
	}

	/* (non-Javadoc)
	 * @see be.kaho.msec.museum.app.ui.components.LocationListener#onError(org.restlet.resource.ResourceException)
	 */
	@Override
	public void onError(ResourceException e) {
		Toast t = Toast.makeText(this, "Could not load Artifacts from server :" + e.getMessage(), Toast.LENGTH_SHORT);
		t.show();		
	}

	
	private Artifact previousArtifact = null;
	/**
	 * Check if there are any artifacts near 'location'
	 * @param location
	 */
	private void checkNearbyArtifacts(Location location) {
		
		Artifact nearestArtifact = null;
		
		//Look for artifacts nearby
		int maxDist = Integer.parseInt(sharedPrefs.getString("notify_distance", "500"));
		for (Artifact a : artifacts) {
			int d = distance(a.getLocation(), location);
			
			if (d < maxDist && !(previousArtifact != null && a.getArtRef().equals(previousArtifact.getArtRef()))) {
				nearestArtifact = a;
				break;
			}
		}
		
		//alert User if one is found
		if (nearestArtifact != null){
			Boolean showAlert = sharedPrefs.getBoolean("display_alert", false);
						
			
//			if (showAlert) {
//				alertUser(nearestArtifact);
//			} else {
				showArtifactActivity(nearestArtifact);
//			}
			previousArtifact = nearestArtifact;
		}
	}

	/**
	 * Compute the distance between locations l1 and l2.
	 * @param l1
	 * @param l2
	 * @return
	 */
	private int distance(Location l1, Location l2) {
		return (int) Math.sqrt(Math.pow(l1.getX()-l2.getX(), 2)+Math.pow(l1.getY()-l2.getY(),2));
	}



	/**
	 * Request the user to show the artifact activity
	 * @param ref the reference of the artifact
	 */
	private void alertUser(final Artifact artifact) {
		

		this.pause();
		
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(R.string.showArtifact);
		alertbox.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				//show Artifact
				showArtifactActivity(artifact);
			}
		});
		alertbox.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			Log.i(this.getClass().getName(), "Resuming the location updater and enabling the map...");
			floorplanReady = true;
			locationUpdater.resume();
				
			}
		});

		// display box
		alertbox.show();

	}

	/**
	 * generate the Layoutparameters to position the view at the correct location of the map
	 * @param p 
	 * @param w
	 * @param h
	 * @return
	 */
	private LayoutParams positionObject(Point p, int w, int h){
		return new LayoutParams(w,h, p.x, p.y);
	}

	/**
	 * Convert Location (in meters) into points on the display
	 * @param location
	 * @return
	 */
	private Point locationToPoint(Location location){
		Point p = new Point();
		AbsoluteLayout l = (AbsoluteLayout)findViewById(R.id.plattegrondLayout);	
		p.x = (int) (location.getX() * ((l.getWidth()) / (double)plan_width ));
		p.y = (int) (location.getY() * ((l.getHeight()) / (double)plan_height ));
		return p;
	}
	
	
}
