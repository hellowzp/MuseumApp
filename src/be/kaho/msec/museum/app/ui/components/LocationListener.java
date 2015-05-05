/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app.ui.components;

import org.restlet.resource.ResourceException;

import be.kaho.msec.museum.common.Location;

public interface LocationListener {
	/**
	 * returns the location of the user
	 * @param location
	 */
	public void onLocationChanged(Location location);

	/**
	 * Returns errors, thrown in the LocationProvider
	 * @param e
	 */
	public void onError(ResourceException e);
	
}
