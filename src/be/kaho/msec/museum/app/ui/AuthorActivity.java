/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app.ui;

import org.restlet.resource.ClientResource;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import be.kaho.msec.museum.ServerLocation;
import be.kaho.msec.museum.app.NetworkingHelper;
import be.kaho.msec.museum.app.R;
import be.kaho.msec.museum.app.Util;
import be.kaho.msec.museum.common.Artifact;
import be.kaho.msec.museum.common.Author;
import be.kaho.msec.museum.common.AuthorResource;

public class AuthorActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.author);
		
		final String lang = Util.getSystemLanguage();
		final Artifact artifact = (Artifact)getIntent().getExtras().get("artifact");
		
//		NetworkingHelper.executeInBackground(this, new Runnable() {
//			
//			@Override
//			public void run() {
				
				
				final ClientResource resource = new ClientResource(
						ServerLocation.MUSEUM_SERVER.resolve(
								lang + "/authors/" + artifact.getAuthorRef()));
				
				AuthorResource aur = resource.wrap(AuthorResource.class);

				final Author author = aur.getAuthor();
				
				final Bitmap imageBitmap = Util.downloadBitmap(
						ServerLocation.MUSEUM_SERVER.resolve("media/" + author.getImageRef()));

				AuthorActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						
						TextView name = (TextView)findViewById(R.id.aut_name);
						name.setText(author.getName());
						
						TextView date = (TextView)findViewById(R.id.aut_date);
						date.setText(author.getDate());

						TextView bio = (TextView)findViewById(R.id.aut_bio);
						bio.setText(author.getBiography());

						ImageView img = (ImageView)findViewById(R.id.aut_image);
						img.setImageBitmap(imageBitmap);
						
					}
				});
				
				resource.release();
				
//			}
//		});
	}
}
