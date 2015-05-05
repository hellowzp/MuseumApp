/**
 * Copyright MSEC - KAHO Sint Lieven 2011
 */
package be.kaho.msec.museum.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import be.kaho.msec.museum.app.R;

public class WelcomeActivity extends Activity {
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button settingsButton = (Button) this.findViewById(R.id.configButton);
        Button continueButton = (Button) this.findViewById(R.id.continueButton);
        
        OnClickListener settingsButtonListener = new OnClickListener()
		{
			
			@Override
			public void onClick(View view)
			{
				showActivity(UserPreferenceActivity.class);
			}
		};		
		settingsButton.setOnClickListener(settingsButtonListener);
		
		OnClickListener continueButtonListener = new OnClickListener(){
			@Override
			public void onClick(View view) {
				showActivity(PlanActivity.class);
			}
		};
		continueButton.setOnClickListener(continueButtonListener);
		
    }
    
    /**
     * Show an activity (without parameters)
     * @param c the class to the activity to be started
     */
    private void showActivity(Class<?> c){
		Intent i = new Intent();
		i.setClass(WelcomeActivity.this, c);
		startActivity(i);
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.welcome_menu, menu);
	    return true;
	}
    	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.configOption:   
	        	showActivity(UserPreferenceActivity.class);
	        	break;
//	        case R.id.aboutOption: 
//	        	showActivity(AboutActivity.class);
//	        	break;
	    }
	    return true;
	}
    
}