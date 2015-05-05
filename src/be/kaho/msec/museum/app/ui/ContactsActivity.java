package be.kaho.msec.museum.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import be.kaho.msec.museum.app.R;
import be.kaho.msec.museum.app.ui.components.NameEmailPair;

@SuppressLint("InflateParams")
public class ContactsActivity extends Activity implements OnCancelListener, OnClickListener
{
	private Builder dialogBuilder;
	private ListView contactsListView;
	private AlertDialog contactsDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialogBuilder = new Builder(this);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		createContactsDialog();
		
	}
	

	private void createContactsDialog()
	{
		final ArrayAdapter<NameEmailPair> contactsAdapter = new ArrayAdapter<NameEmailPair>(this, 
				android.R.layout.simple_list_item_multiple_choice, getContacts());
		
		View fullLayout = getLayoutInflater().inflate(R.layout.contacts_chooser_dialog, null);
		dialogBuilder.setView(fullLayout);
		dialogBuilder.setTitle(Html.fromHtml("<b>" 
				+ getString(R.string.contactChooserTitle) + "</b>"));
		
		contactsDialog = dialogBuilder.create();
		contactsListView = (ListView) fullLayout.findViewById(R.id.contactsListView);
		contactsListView.setAdapter(contactsAdapter);
		contactsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		Button cancelButton = (Button) fullLayout.findViewById(R.id.contactsCancelButton);
		contactsDialog.setCancelable(true);
		cancelButton.setOnClickListener(this);
		contactsDialog.setOnCancelListener(this);
		Button confirmButton = (Button) fullLayout.findViewById(R.id.contactsContinueButton);
		confirmButton.setOnClickListener(this);
		contactsDialog.show();
	}
	
	private List<NameEmailPair> getContacts()
	{
        List<NameEmailPair> contactList = new ArrayList<NameEmailPair>();
        ContentResolver resolver = getContentResolver();

        
        String where = ContactsContract.CommonDataKinds.Email.DATA + " IS NOT NULL AND "
        		+ "trim(" + ContactsContract.CommonDataKinds.Email.DATA + ") != \'\'";
        
        Cursor cursor = resolver.query( 
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            new String[] {
            		ContactsContract.CommonDataKinds.Email.DATA
            },
            where,
            null,
            //Case-insensitive sorting
            "lower(" + ContactsContract.CommonDataKinds.Email.DATA + ") DESC"
        );
        
        while (cursor.moveToNext()) { 
        	
            String name = cursor.getString(cursor.getColumnIndex(
            		ContactsContract.CommonDataKinds.Email.DATA));
            String email = cursor.getString(cursor.getColumnIndex(
            		ContactsContract.CommonDataKinds.Email.DATA));
            
        	contactList.add(new NameEmailPair(name, email));
            	
        }
        
        cursor.close();
        
        return contactList;
	}

	private void cancel()
	{
		contactsDialog.dismiss();
		ContactsActivity.this.setResult(Activity.RESULT_CANCELED);
		ContactsActivity.this.finish();
	}
	
	@Override
	public void onCancel(DialogInterface dialog)
	{
		cancel();
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		
		case R.id.contactsCancelButton:
			cancel();
			break;
		case R.id.contactsContinueButton:
		
			SparseBooleanArray selectedPositions = contactsListView.getCheckedItemPositions();
			List<String> selectedEmails = new ArrayList<String>();
			for (int i=0; i<contactsListView.getCount(); i++) {
				if (selectedPositions.get(i)) {
					
					NameEmailPair currentPair = 
							(NameEmailPair) contactsListView.getItemAtPosition(i);
					selectedEmails.add(currentPair.getEmail());
				}
			}

			String[] selectedEmailsArray = 
					selectedEmails.toArray(new String[selectedPositions.size()]);
			
			Intent i = ContactsActivity.this.getIntent();
			i.putExtra("contacts", selectedEmailsArray);
			contactsDialog.dismiss();
			ContactsActivity.this.setResult(Activity.RESULT_OK, i);
			ContactsActivity.this.finish();
			break;
			
		default:
			break;
			
		}
		
	}
	
	
}
