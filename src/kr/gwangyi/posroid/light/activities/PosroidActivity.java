package kr.gwangyi.posroid.light.activities;

import kr.gwangyi.activities.FragmentTabActivity;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.fragments.CafeteriaFragment;
import kr.gwangyi.posroid.light.fragments.DeliveryFragment;
import kr.gwangyi.posroid.light.fragments.NewsletterFragment;
import kr.gwangyi.posroid.light.fragments.PostechCalendarFragment;
import kr.gwangyi.posroid.light.fragments.WelfareFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class PosroidActivity extends FragmentTabActivity
{
	private static final int PREFERENCES = 0, HELP = 1, QUIT = 2;
	private TabHost tabhost;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.main);
	    tabhost = getTabHost();
	    
	    TabManager man = getTabManager();
	    Bundle args;
	    man.addTab(tabhost.newTabSpec("newsletter").setIndicator(getString(R.string.newsletter)),
	    		NewsletterFragment.class, null);
	    args = new Bundle();
	    args.putInt("monthOffset", 2);
	    man.addTab(tabhost.newTabSpec("calendar").setIndicator(getString(R.string.calendar)),
	    		PostechCalendarFragment.class, args);
	    man.addTab(tabhost.newTabSpec("cafeteria").setIndicator(getString(R.string.cafeteria)),
	    		CafeteriaFragment.class, null);
	    man.addTab(tabhost.newTabSpec("delivery").setIndicator(getString(R.string.delivery)),
	    		DeliveryFragment.class, null);
	    man.addTab(tabhost.newTabSpec("welfare").setIndicator(getString(R.string.welfare)),
	    		WelfareFragment.class, null);
	    
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	    try
	    {
	    	tabhost.setCurrentTab(Integer.parseInt(pref.getString("default_tab", "0")));
	    }
	    catch(NumberFormatException e)
	    {
	    }
	    
	    if (savedInstanceState != null)
	    	tabhost.setCurrentTabByTag(savedInstanceState.getString("tab"));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
        outState.putString("tab", tabhost.getCurrentTabTag());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem item;

		item = menu.add(Menu.NONE, PREFERENCES, Menu.NONE, R.string.preferences);
		item.setIcon(android.R.drawable.ic_menu_preferences);

		item = menu.add(Menu.NONE, HELP, Menu.NONE, R.string.help);
		item.setIcon(android.R.drawable.ic_menu_help);
		
		item = menu.add(Menu.NONE, QUIT, Menu.NONE, R.string.quit);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent i;
		switch(item.getItemId())
		{
		case PREFERENCES:
			i = new Intent(this, PreferenceActivity.class);
			startActivity(i);
			break;
		case HELP:
			i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://posroid.gwangyi.kr:8799/"));
			startActivity(i);
			break;
		case QUIT:
			finish();
			break;
		}
		return true;
	}
}
