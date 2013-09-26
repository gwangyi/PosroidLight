package kr.gwangyi.posroid.light.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

public abstract class VersionChecker {
    private final String CURRENT_VERSION;
    private AsyncTask<Void, Void, String> task;
    
    public VersionChecker(Context context) {
    	String version;
    	
	    try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "?";
		}
	    CURRENT_VERSION = version;
	    
	    task = new AsyncTask<Void, Void, String>() {
	    	@Override
	    	protected String doInBackground(Void... params) {
	    		try
	    	    {
	    		    String latest;
	    	    	URL url = new URL("http://posroid.gwangyi.kr:8799/version.txt");
	    	    	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	    	    	latest = in.readLine();
	    	    	return latest;
	    	    }
	    	    catch (Exception e)
	    	    {
	    	    	return null;
	    	    }
	    	}
	    	
	    	@Override
	    	protected void onPostExecute(String result) {
	    		doVersionCheck(result);
	    	}
	    };
	}
    
    public String getCurrentVersion() { return CURRENT_VERSION; }
    
    protected abstract void doVersionCheck(String latest);
	
	public void execute() {
		task.execute();
	}
}
