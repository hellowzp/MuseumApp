package be.kaho.msec.museum.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

public class NetworkingHelper extends AsyncTask<Runnable, Void, Void> {
	
	
	public static void executeInBackground(Context context, Runnable... tasks)
	{
		executeInBackground(context, true, tasks);
	}
	
	public static void executeInBackground(Context context, boolean withProgressDialog, Runnable... tasks)
	{
		NetworkingHelper networkingTask = new NetworkingHelper(context, withProgressDialog);
		
		
		/* !!! Important
		 * As of Android 3.0, AsyncTask behaves differently.
		 * < 3.0: supports parallel execution by default.
		 * >= 3.0: parallel execution only works properly if executed
		 * on Android's ThreadPoolExecutor
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			networkingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tasks);
		else
		    networkingTask.execute(tasks);
		
	}
	
	private Context context;
	private ProgressDialog progressDialog;
	private boolean withProgressDialog = true;
	
	
	private NetworkingHelper(Context context, boolean withProgressDialog)
	{
		super();
		this.context = context;
		this.withProgressDialog = withProgressDialog;
		progressDialog = new ProgressDialog(this.context);
		progressDialog.setIndeterminate(true);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		
		if (withProgressDialog) {
			progressDialog.setMessage(context.getString(R.string.networktasks_progress));
			progressDialog.show();
		}
		
	}
	
	@Override
	protected Void doInBackground(Runnable... tasks) {
		
		
		if (tasks != null) {
			
			for (Runnable task : tasks) {
				task.run();
			}
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		if (withProgressDialog) {
			progressDialog.setMessage(context.getString(R.string.networktasks_finished));
			progressDialog.dismiss();
		}
	}
	
	/*
	 * This method can be implemented to take actions that relate
	 * to the progress of the computation. It runs on the UI thread.
	 * Examples:
	 * - updating the progress bar
	 * - displaying new messages
	 * - updating other UI elements
	 */
//	@Override
//	protected void onProgressUpdate(Void... values) {
//		super.onProgressUpdate(values);
//	}
	
}
