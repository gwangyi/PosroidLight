package kr.gwangyi.posroid.light.activities;

import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.fragments.DeliveryOverviewFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;
import android.os.Bundle;

public class WelfareActivity extends FragmentActivity
{
	private String name, phone, start, end, detail;
	private ViewPager pager;
	
	private class Adapter extends FragmentStatePagerAdapter
	{
		public Adapter()
		{
			super(getSupportFragmentManager());
		}

		@Override
		public Fragment getItem(int position)
		{
			return DeliveryOverviewFragment.newInstance(name, start, end, detail, phone == null ? null : new String[] { phone });
		}
		
		@Override
		public CharSequence getPageTitle(int position)
		{
			return getString(R.string.overview);
		}

		@Override
		public int getCount()
		{
			return 1;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.delivery);
	    
	    name = getIntent().getStringExtra("name");
	    phone = getIntent().getStringExtra("phone");
	    start = getIntent().getStringExtra("start");
	    end = getIntent().getStringExtra("end");
	    detail = getIntent().getStringExtra("detail");
	    String state = getIntent().getStringExtra("state");
	    if(state == null)
	    	state = getString(R.string.closed);
	    name = String.format("%s (%s)", name, state);
	    
	    ((TextView)findViewById(R.id.name)).setText(name);
	    
	    pager = (ViewPager)findViewById(R.id.delivery);
	    pager.setAdapter(new Adapter());
	}

}
