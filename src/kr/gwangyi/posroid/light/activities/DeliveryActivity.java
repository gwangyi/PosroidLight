package kr.gwangyi.posroid.light.activities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import kr.gwangyi.fragments.ExpandableListFragment;
import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.fragments.DeliveryOverviewFragment;
import kr.gwangyi.posroid.light.utilities.XmlUtility;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

public class DeliveryActivity extends FragmentActivity
{
	private static final String DELIVERY_URL = "http://posroid.gwangyi.kr:8799/delivery.fcgi";
	
	private List<String> groups = new ArrayList<String>();
	private List<List<Item>> children = new ArrayList<List<Item>>();
	private List<String> phone = new ArrayList<String>();
	private String id, name, start = "", end = "", detail;
	
	private ViewPager pager;

	private class Item
	{
		public String name = "";
		public String note = "";
		public List<String> pictures = new ArrayList<String>();
		public String detail = null;
	}
	
	private class Adapter extends FragmentStatePagerAdapter
	{
		public Adapter()
		{
			super(getSupportFragmentManager());
			
			final DialogFragment dlg = ProgressDialogFragment.newInstance(null, getString(R.string.loading));
			dlg.show(getSupportFragmentManager(), "dialog");
			
			new AsyncTask<Void, Void, Void>()
			{
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
				
				protected void onPostExecute(Void result)
				{
				    ((TextView)findViewById(R.id.name)).setText(name);
					pager.setAdapter(null);
					pager.setAdapter(Adapter.this);
					dlg.dismiss();
				}
			}.execute();
		}

		@Override
		public Fragment getItem(int position)
		{
			switch(position)
			{
			case 0:
				return DeliveryOverviewFragment.newInstance(name, start, end, detail, phone.toArray(new String[0]));
			case 1:
				return new MenuFragment();
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
	
	private class MenuFragment extends ExpandableListFragment
	{
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			
			((TextView)getView().findViewById(android.R.id.empty)).setText(R.string.empty);
			MenuAdapter adapter = new MenuAdapter();
			setListAdapter(adapter);
			getListView().setOnChildClickListener(adapter);
		}
	}
	
	private class MenuAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener
	{
		@Override
		public Object getChild(int groupPosition, int childPosition)
		{
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent)
		{
			LayoutInflater li = getLayoutInflater();
			Item i = children.get(groupPosition).get(childPosition);
			if(convertView == null)
				convertView = li.inflate(R.layout.delivery_menu_item, parent, false);
			
			((TextView)convertView.findViewById(android.R.id.text1)).setText(i.name);
			((TextView)convertView.findViewById(android.R.id.text2)).setText(i.note);
			
			if(i.pictures.size() == 0) convertView.findViewById(R.id.picture).setVisibility(View.GONE);
			else convertView.findViewById(R.id.picture).setVisibility(View.VISIBLE);
			if(i.detail == null) convertView.findViewById(R.id.detail).setVisibility(View.GONE);
			else convertView.findViewById(R.id.detail).setVisibility(View.VISIBLE);
			
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition)
		{
			return children.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition)
		{
			return null;
		}

		@Override
		public int getGroupCount()
		{
			return children.size();
		}

		@Override
		public long getGroupId(int groupPosition)
		{
			return ExpandableListView.getPackedPositionForGroup(groupPosition);
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			LayoutInflater li = getLayoutInflater();
			if(convertView == null)
				convertView = li.inflate(android.R.layout.simple_expandable_list_item_2, parent, false);
			
			((TextView)convertView.findViewById(android.R.id.text1)).setText(groups.get(groupPosition));
			
			return convertView;
		}

		@Override
		public boolean hasStableIds()
		{
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return true;
		}

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
		{
			Item i = children.get(groupPosition).get(childPosition);
			if(i.detail == null && i.pictures.size() == 0)
				return false;
			else
			{
				String detail = null;
				String [] pictures = null;
				detail = i.detail;
				if(i.pictures.size() > 0)
					pictures = i.pictures.toArray(new String[0]);
				
				DetailDialogFragment dlg = DetailDialogFragment.newInstance(detail, pictures);
				dlg.show(getSupportFragmentManager(), "dialog");
				return true;
			}
		}
	}
	
	private static class DetailDialogFragment extends DialogFragment
	{
		private String detail;
		private String [] pictures;
		
		public static DetailDialogFragment newInstance(String detail, String[] pictures)
		{
			DetailDialogFragment dlg = new DetailDialogFragment();
			Bundle args = new Bundle();
			args.putString("detail", detail);
			args.putStringArray("pictures", pictures);
			dlg.setArguments(args);
			
			return dlg;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			
			Bundle args = getArguments();
			detail = args.getString("detail");
			pictures = args.getStringArray("pictures");
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.delivery_menu_item_detail, container, false);
			if(detail == null)
				view.findViewById(R.id.note).setVisibility(View.GONE);
			else
				((TextView)view.findViewById(R.id.note)).setText(detail);
			if(pictures == null)
				view.findViewById(R.id.pictures).setVisibility(View.GONE);
			else
			{
				final ViewPager pager = (ViewPager)view.findViewById(R.id.gallery);
				final View loading = view.findViewById(R.id.loading);
				new AsyncTask<String[], Void, List<Drawable>>()
				{
					@Override
					protected List<Drawable> doInBackground(String[]... params)
					{
						List<Drawable> pictures = new ArrayList<Drawable>();
						for(String picture : params[0])
						{
							try
							{
								pictures.add(Drawable.createFromStream(new URL(picture).openStream(), "src"));
							}
							catch (MalformedURLException e)
							{
								e.printStackTrace();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
						return pictures;
					}
					
					@Override
					protected void onPostExecute(List<Drawable> result)
					{
						final List<Drawable> pictures = result;
						pager.setVisibility(View.VISIBLE);
						loading.setVisibility(View.GONE);
						pager.setAdapter(new PagerAdapter()
						{
							@Override
							public Object instantiateItem(ViewGroup container, int position)
							{
								ImageView img = new ImageView(container.getContext());
								img.setImageDrawable(pictures.get(position));
								container.addView(img);
								return img;
							}
							
							@Override
							public CharSequence getPageTitle(int position)
							{
								return Integer.toString(position + 1);
							}
							
							@Override
							public void destroyItem(ViewGroup container, int position, Object object)
							{
								container.removeView((View)object);
							}
							
							@Override
							public boolean isViewFromObject(View view, Object object)
							{
								return view == (View)object;
							}
							
							@Override
							public int getCount()
							{
								return pictures.size();
							}
						});
					}
				}.execute(pictures);
			}
			return view;
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
