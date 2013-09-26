package kr.gwangyi.posroid.light.activities;

import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.fragments.DiscountMenuFragment;
import kr.gwangyi.posroid.light.fragments.DiscountOverviewFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;
import android.os.Bundle;

public class DiscountActivity extends FragmentActivity
{
//	private List<String> menu = new ArrayList<String>();
	private String id = "";
	private String phone = "";
	private String name = "";
	private String businessDay = "";
	private String start = "", end = "", detail = "";
	private String[] menu = null;
	private double latitude = 0, longitude = 0;
	
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
			switch(position)
			{
			case 0:
				return DiscountOverviewFragment.newInstance(id, name, start, end, detail, phone, businessDay);
			case 1:
				return DiscountMenuFragment.newInstance(name, menu, latitude, longitude);
			}
			return null;
		}

		@Override
		public int getCount()
		{
			return 2;
		}
		
		@Override
		public CharSequence getPageTitle(int position)
		{
			switch(position)
			{
			case 0:
				return getString(R.string.overview);
			case 1:
				return getString(R.string.delivery_menu);
			default:
				return "";
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.delivery);

	    id = getIntent().getStringExtra("id");
	    name = getIntent().getStringExtra("name");
	    phone = getIntent().getStringExtra("phone");
	    start = getIntent().getStringExtra("start");
	    end = getIntent().getStringExtra("end");
	    detail = getIntent().getStringExtra("detail");
	    businessDay = getIntent().getStringExtra("businessDay");
	    menu = getIntent().getStringArrayExtra("menu");
	    longitude = getIntent().getDoubleExtra("longitude", 0);
	    latitude = getIntent().getDoubleExtra("latitude", 0);
	    
	    ((TextView)findViewById(R.id.name)).setText(name);

	    pager = (ViewPager)findViewById(R.id.delivery);
	    pager.setAdapter(new Adapter());
	}

}
