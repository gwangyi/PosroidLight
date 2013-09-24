package kr.gwangyi.posroid.light.activities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.fragments.DeliveryMenuFragment;
import kr.gwangyi.posroid.light.fragments.DeliveryMenuFragment.Item;
import kr.gwangyi.posroid.light.fragments.DeliveryOverviewFragment;
import kr.gwangyi.posroid.light.utilities.XmlUtility;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

public class DeliveryActivity extends FragmentActivity
{
	private ViewPager pager;
	
	private static final String DELIVERY_URL = "http://posroid.gwangyi.kr:8799/delivery.fcgi";
	
	private ArrayList<String> groups = new ArrayList<String>();
	private List<List<Item>> children = new ArrayList<List<Item>>();
	private List<String> phone = new ArrayList<String>();
	private String id, name, start = "", end = "", detail;
	
	private class Adapter extends FragmentStatePagerAdapter
	{
		
		private class AdapterTask extends AsyncTask<Void, Void, Void> implements DialogInterface.OnCancelListener
		{
			private ProgressDialogFragment dlg;

			@Override
			protected Void doInBackground(Void... params)
			{
				try
				{
					XmlPullParser parser = XmlUtility.makeInstanceFromUrl(DeliveryActivity.this, new URL(DELIVERY_URL + "?" + id), "delivery_" + id + ".xml");
					
					int parserEvent = parser.getEventType();
					List<Item> children = null;
					Item child = null;
					String text = null;
					DeliveryActivity.this.phone.clear();
					
					while(parserEvent != XmlPullParser.END_DOCUMENT)
					{
						switch(parserEvent)
						{
						case XmlPullParser.START_TAG:
							if(parser.getName().equals("restaurant"))
							{
								name = parser.getAttributeValue(null, "name");
							}
							else if(parser.getName().equals("phone"))
							{
								text = "";
							}
							else if(parser.getName().equals("workingHour"))
							{
								text = "";
							}
							else if(parser.getName().equals("note"))
							{
								text = "";
							}
							else if(parser.getName().equals("category"))
							{
								DeliveryActivity.this.groups.add(parser.getAttributeValue(null, "name"));
								children = new ArrayList<Item>();
								DeliveryActivity.this.children.add(children);
							}
							else if(parser.getName().equals("item2"))
							{
								child = new Item();
								child.name = parser.getAttributeValue(null, "name").replaceAll("\\s*\\[[0-9]*\\]$", "");
								child.note = String.format("£Ü%s (%s/%s)", parser.getAttributeValue(null, "price"),
										parser.getAttributeValue(null, "good"), parser.getAttributeValue(null, "bad"));
								children.add(child);
							}
							else if(parser.getName().equals("picture"))
							{
								text = "";
							}
							break;
						case XmlPullParser.TEXT:
							if(text != null) text += parser.getText();
							break;
						case XmlPullParser.END_TAG:
							if(parser.getName().equals("phone"))
							{
								DeliveryActivity.this.phone.add(text);
							}
							else if(parser.getName().equals("workingHour"))
							{
								String [] time = text.replaceAll(" ", "").split("-");
								String [] hms;
								Calendar start = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
								Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
								
								hms = time[0].split(":");
								start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hms[0]));
								start.set(Calendar.MINUTE, Integer.parseInt(hms[1]));
								start.set(Calendar.SECOND, 0);
								start.set(Calendar.MILLISECOND, 0);
								
								hms = time[1].split(":");
								end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hms[0]));
								end.set(Calendar.MINUTE, Integer.parseInt(hms[1]));
								end.set(Calendar.SECOND, 0);
								end.set(Calendar.MILLISECOND, 0);
								
								DeliveryActivity.this.start = String.format("%tR", start);
								DeliveryActivity.this.end = String.format("%tR", end);
							}
							else if(parser.getName().equals("note"))
							{
								text = text.trim();
								if(child == null)
									detail = text;
								else if(text.length() > 1)
									child.detail = text;
							}
							else if(parser.getName().equals("picture"))
								child.pictures.add(text);
							else if(parser.getName().equals("item2"))
								child = null;
							break;
						}
						parserEvent = parser.next();
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (XmlPullParserException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPreExecute()
			{
				dlg = ProgressDialogFragment.newInstance(null, getString(R.string.loading));
				dlg.setOnCancelListener(this);
				dlg.show(getSupportFragmentManager(), "dialog");
			}
			
			protected void onPostExecute(Void result)
			{
			    ((TextView)findViewById(R.id.name)).setText(name);
				pager.setAdapter(null);
				pager.setAdapter(Adapter.this);
				dlg.dismissAllowingStateLoss();
			}

			@Override
			public void onCancel(DialogInterface dialog)
			{
				this.cancel(true);
				Toast.makeText(DeliveryActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
			}
		}

		
		public Adapter()
		{
			super(getSupportFragmentManager());
			
			new AdapterTask().execute();
		}

		@Override
		public Fragment getItem(int position)
		{
			switch(position)
			{
			case 0:
				return DeliveryOverviewFragment.newInstance(name, start, end, detail, phone.toArray(new String[0]));
			case 1:
				return DeliveryMenuFragment.newInstance(groups, children);
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
	    phone.add(getIntent().getStringExtra("phone"));
	    start = getIntent().getStringExtra("start");
	    end = getIntent().getStringExtra("end");
	    detail = getString(R.string.empty);
	    
	    ((TextView)findViewById(R.id.name)).setText(name);

	    pager = (ViewPager)findViewById(R.id.delivery);
	    pager.setAdapter(new Adapter());
	}

}
