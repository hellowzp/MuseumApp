/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app.ui;

import org.restlet.resource.ClientResource;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import be.kaho.msec.museum.ServerLocation;
import be.kaho.msec.museum.app.NetworkingHelper;
import be.kaho.msec.museum.app.R;
import be.kaho.msec.museum.app.Util;
import be.kaho.msec.museum.common.Artifact;
import be.kaho.msec.museum.common.Artifact.MediaType;
import be.kaho.msec.museum.common.ArtifactInfo;
import be.kaho.msec.museum.common.ArtifactInfoResource;


public class ArtifactActivity extends Activity {

	
	private Artifact artifact;
	private ArtifactInfo artifactInfo;
	
	public final static int CHOOSE_CONTACTS_ACTION = 14859;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		NetworkingHelper.executeInBackground(this, true, new Runnable() {
			
			@Override
			public void run() {
				

				Log.i(this.getClass().getSimpleName(), "Task in progress");
				
				final String lang = Util.getSystemLanguage();
				artifact = (Artifact) getIntent().getExtras().get("artifact");

				ClientResource resource = new ClientResource(
						ServerLocation.MUSEUM_SERVER.resolve(lang + "/artifacts/info/" + artifact.getInfoRef()));
				ArtifactInfoResource air = resource.wrap(ArtifactInfoResource.class);
				
				resource.release();
				
				/*
				 * mediaReference (within ArtifactInfo) is always a filename, which is
				 * concatenated to a url prefix. Except when it's video or audio,
				 * in which case the url must be absolute.
				 */
				artifactInfo = air.getArtifactInfo();
				
				//If the artifact is an image, retrieve its picture
				Bitmap imageBitmap = null;
				if (artifact.getMediaType() == MediaType.Image) {
					imageBitmap = Util.downloadBitmap(
							ServerLocation.MUSEUM_SERVER.resolve(
									"media/" + artifactInfo.getMediaReference()));
				}
				
				switch (artifact.getMediaType()) {
					case Image:
						createImagePanel(artifactInfo, imageBitmap);
						break;
					case Audio:
						createAudioPanel(artifactInfo);
						break;
					case Video:
						createVideoPanel(artifactInfo);
						break;
				}

				runOnUiThread(new Runnable() {
					
					
					@Override
					public void run() {
						
						TextView title = (TextView)findViewById(R.id.art_title);
						title.setText(artifactInfo.getName());
						
						TextView author = (TextView)findViewById(R.id.art_author);
						author.setText(artifactInfo.getAuthor());
						
						TextView year = (TextView)findViewById(R.id.art_year);
						year.setText("(" + artifactInfo.getYear() + ")");
						
						TextView location = (TextView)findViewById(R.id.art_location);
						location.setText("" + artifactInfo.getLocation());
						
						TextView inf = (TextView)findViewById(R.id.art_info);
						inf.setText(artifactInfo.getInfo());
						
						TextView more = (TextView)findViewById(R.id.art_more);
						
						more.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								//show author activity
								Intent i = new Intent();
								i.putExtra("artifact", artifact);
								i.setClass(ArtifactActivity.this, AuthorActivity.class);
								startActivity(i);
							}
						});
						
					}
				});
				
				
			}
		});
		
	}


	private void createImagePanel(final ArtifactInfo artifactInfo, final Bitmap imageBitmap)
	{
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setContentView(R.layout.artifact);
				ImageView img = (ImageView) findViewById(R.id.art_image);
				img.setImageBitmap(imageBitmap);
				
			}
		});
	}
	
	private void createAudioPanel(final ArtifactInfo artifactInfo)
	{
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				setContentView(R.layout.artifact_audio_video);
				TextView audioLink = (TextView) findViewById(R.id.art_medialink);
				audioLink.setText(getString(R.string.listen_to_audio));
				audioLink.setOnClickListener(new View.OnClickListener()
				{

					@Override
					public void onClick(View v)
					{

						/*
						 * Start generic view action for video. We don't care about
						 * the program being used, Android takes care of this for
						 * us.
						 */
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(artifactInfo.getMediaReference())));

					}
				});
				
				
				ImageView img = (ImageView) findViewById(R.id.art_image);
				img.setImageResource(R.drawable.audio_icon);
			}
		});
	}
	
	private void createVideoPanel(final ArtifactInfo artifactInfo)
	{
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setContentView(R.layout.artifact_audio_video);
				ImageView img = (ImageView) findViewById(R.id.art_image);
				img.setImageResource(R.drawable.video_icon);
				TextView videoLink = (TextView) findViewById(R.id.art_medialink);
				videoLink.setText(R.string.view_video);
				videoLink.setOnClickListener(new View.OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						/*
						 * Start generic view action for video. We don't care about
						 * the program being used, Android takes care of this for
						 * us.
						 */
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(artifactInfo.getMediaReference())));
					}
				});
			}
		});
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.artifact_menu, menu);
	    
		//Cannot use this way to populate the options menu,
		//since network communication takes too long
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		boolean consumed;
		
		switch(item.getItemId()) {
		
		case R.id.shareOption:
			
			startActivity(new Intent(this, ContactsActivity.class));
			
		    consumed = true;
		    break;
		    
		default:
			consumed = false;
			break;
		
		}
	    
	    return consumed;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CHOOSE_CONTACTS_ACTION) {
			
			switch(resultCode) {
			
			case Activity.RESULT_OK:
				
				String [] emails = data.getStringArrayExtra("contacts");
				
				if (emails.length > 0) {
				    Intent shareIntent = new Intent(Intent.ACTION_SEND);
				    shareIntent.setType("text/plain");
				    shareIntent.putExtra(Intent.EXTRA_TEXT, "Artifact: " + artifactInfo.getName());
				    
				    String nameLabel = getString(R.string.artifactShareName);
				    String authorLabel = getString(R.string.artifactShareAuthor);
				    String yearLabel = getString(R.string.artifactShareYear);
				    String locationLabel = getString(R.string.artifactShareLocation);
				    String mediaLabel = getString(R.string.artifactShareMedia);
				    
				    String lineSeparator = System.getProperty("line.separator");
				    StringBuffer sharedInfo = new StringBuffer();
				    sharedInfo.append(nameLabel + ": " + artifactInfo.getName())
					    .append(lineSeparator)
					    .append(authorLabel + ": " + artifactInfo.getAuthor())
					    .append(lineSeparator)
					    .append(yearLabel + ": " + artifactInfo.getYear())
					    .append(lineSeparator)
					    .append(locationLabel + ": " + artifactInfo.getLocation())
					    .append(lineSeparator)
					    .append(mediaLabel + ": " + artifactInfo.getMediaReference())
					    .append(lineSeparator);
				    
				    shareIntent.putExtra(Intent.EXTRA_SUBJECT, sharedInfo.toString());
				    startActivity(shareIntent);
				}
				
				break;
				
			default:
				
				break;
			}
			
		}
	}
	
}
