/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Util {

	/*
	 * list of supported languages
	 */
    private static List<String> supportedLangs = Arrays.asList(new String[] {"en", "fr", "nl"});

    /**
     * Download a bitmap from the server located at URI, into a Bitmap object
     * @param uri
     * @return
     */
	public static Bitmap downloadBitmap(URI uri){

		try {
			HttpURLConnection conn= (HttpURLConnection)uri.toURL().openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			Bitmap bmp = BitmapFactory.decodeStream(is);
			is.close();
			conn.disconnect();
			return bmp;
		} catch (IOException e) {
			Log.e("Util", "Could not download Bitmap", e);
			return null;

		}
	}
	
	/**
	 * Returns "en" if the system language is not supported at the server side
	 * or if there is no default language
	 */
	public static String getSystemLanguage() {
		String lang = Locale.getDefault().getLanguage();
		return (supportedLangs.contains(lang) ? lang : "en");
	}

}
