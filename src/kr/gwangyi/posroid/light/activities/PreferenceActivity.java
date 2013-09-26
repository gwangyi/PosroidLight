package kr.gwangyi.posroid.light.activities;

import java.io.File;
import java.io.FileFilter;

import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.utilities.PasswordManager;
import kr.gwangyi.posroid.light.utilities.VersionChecker;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferenceActivity extends android.preference.PreferenceActivity implements Preference.OnPreferenceClickListener
{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    addPreferencesFromResource(R.xml.settings);
	    
	    Preference pwd = getPreferenceScreen().findPreference("povis_pwd_raw");
	    pwd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this);
				String id = pref.getString("povis_id", "");
				if(id.equals(""))
					return false;
				
				SharedPreferences.Editor edit = pref.edit();
				if(newValue.equals(""))
				{
					edit.putString("povis_pwd", "");
					edit.commit();
					return false;
				}
				String pass = PasswordManager.encrypt(id, newValue.toString());
				edit.putString("povis_pwd", pass);
				edit.commit();
				return false;
			}
		});
	    
	    new VersionChecker(this) {
		    private Preference version;
		    
    		{
    			version = findPreference("latest");
    	    	version.setSummary(getString(R.string.current_version) + getCurrentVersion());
    		}

    		@Override
			protected void doVersionCheck(String latest) {
				if(latest != null)
				{
			    	if(latest.compareTo(getCurrentVersion()) > 0)
			    	{
			    		version.setOnPreferenceClickListener(PreferenceActivity.this);
			    	}
			    	version.setTitle(getString(R.string.latest) + latest);
				}
				else
				{
			    	version.setTitle(getString(R.string.latest) + "?");
				}
			}
		}.execute();

	    Preference about = findPreference("about");
	    about.setOnPreferenceClickListener(this);
	    Preference clear = findPreference("clear");
	    clear.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if(preference.getKey().equals("latest"))
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=kr.gwangyi.nposroid"));
			startActivity(intent);
			return true;
		}
		else if(preference.getKey().equals("about"))
		{
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://posroid.gwangyi.kr:8799"));
			startActivity(i);
			return true;
		}
		else if(preference.getKey().equals("clear"))
		{
			for(File file : getFilesDir().listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return pathname.getName().endsWith(".xml");
				}
			}))
			{
				file.delete();
			}
			Toast.makeText(this, R.string.clear_finished, Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}

}
