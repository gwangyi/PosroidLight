package kr.gwangyi.posroid.light.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import kr.gwangyi.fragments.ExpandableListFragment;
import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.activities.DeliveryActivity;
import kr.gwangyi.posroid.light.utilities.XmlUtility;

public class DeliveryFragment extends ExpandableListFragment
{
	public class Adapter extends BaseExpandableListAdapter implements AdapterView.OnItemLongClickListener, ExpandableListView.OnChildClickListener
	{
		private static final String DELIVERY_URL = "http://posroid.gwangyi.kr:8799/delivery.fcgi";
		private static final int NONE = 0, PHONE = 1, WORKING_HOUR = 2;
		
		private class Restaurant
		{
			public static final int OPEN = 0, CLOSED = 1, PREPARE = 2;
			public String name = null;
			public String id = null;
			public List<String> phone = null;
			public int state = CLOSED;
			public String start = null, end = null;
		}

		private FragmentActivity context;
		
		private Set<String> favorites = new TreeSet<String>();
		
		private List<String> groups = new ArrayList<String>();
		private List<List<Restaurant>> children = new ArrayList<List<Restaurant>>();
		
		public Adapter()
		{
			this.context = getActivity();
			
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			String [] favs = pref.getString("delivery_favorites", "").split(":");
			for(String fav : favs)
				favorites.add(fav);
			
			final DialogFragment dlg = ProgressDialogFragment.newInstance(null, context.getString(R.string.loading));
			dlg.show(context.getSupportFragmentManager(), "dialog");
			
			new AsyncTask<Void, Void, Void>()
			{
				private Adapter adapter = Adapter.this;
				
				@Override
				protected Void doInBackground(Void... params)
				{
					adapter.groups.add(adapter.context.getString(R.string.favorite));
					adapter.children.add(new ArrayList<Restaurant>());
					
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(adapter.context);
					adapter.favorites = new HashSet<String>();
					String[] fav_ids = pref.getString("delivery_favorites", "").split(":");
					try
					{
						XmlPullParser parser = XmlUtility.makeInstanceFromUrl(adapter.context, new URL(DELIVERY_URL));
						
						int parserEvent = parser.getEventType();
						List<Restaurant> children = null;
						Restaurant child = null;
						int mode = NONE;
						String phone = "";
						while(parserEvent != XmlPullParser.END_DOCUMENT)
						{
							switch(parserEvent)
							{
							case XmlPullParser.START_TAG:
								if(parser.getName().equals("category"))
								{
									adapter.groups.add(parser.getAttributeValue(null, "name"));
									children = new ArrayList<Restaurant>();
									adapter.children.add(children);
								}
								else if(parser.getName().equals("restaurant"))
								{
									child = new Restaurant();
									child.name = parser.getAttributeValue(null, "name");
									child.id = parser.getAttributeValue(null, "id");
									child.phone = new ArrayList<String>();
									children.add(child);
									for(String fav : fav_ids)
									{
										if(fav.equals(child.id))
										{
											adapter.favorites.add(fav);
											adapter.children.get(0).add(child);
										}
									}
								}
								else if(parser.getName().equals("phone"))
								{
									mode = PHONE;
									phone = "";
								}
								else if(parser.getName().equals("workingHour"))
								{
									mode = WORKING_HOUR;
								}
								break;
							case XmlPullParser.TEXT:
								switch(mode)
								{
								case PHONE:
									phone += parser.getText();
									break;
								case WORKING_HOUR:
								{
									String [] time = parser.getText().replaceAll(" ", "").split("-");
									String [] hms;
									Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
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
									
									Calendar end_5 = (Calendar)end.clone(), start_5 = (Calendar)start.clone();
									end_5.add(Calendar.MINUTE, -5); start_5.add(Calendar.MINUTE, 5);
									if(start.compareTo(end) > 0)
									{
										if(now.compareTo(end_5) < 0 || now.compareTo(start_5) > 0)
											child.state = Restaurant.OPEN;
										else if(now.compareTo(end) < 0 || now.compareTo(start) > 0)
											child.state = Restaurant.PREPARE;
										else
											child.state = Restaurant.CLOSED;
									}
									else
									{
										if(now.compareTo(end_5) < 0 && now.compareTo(start_5) > 0)
											child.state = Restaurant.OPEN;
										else if(now.compareTo(end) < 0 && now.compareTo(start) > 0)
											child.state = Restaurant.PREPARE;
										else
											child.state = Restaurant.CLOSED;
									}
									child.start = String.format("%tR", start);
									child.end = String.format("%tR", end);
									break;
								}
								}
								break;
							case XmlPullParser.END_TAG:
								if(parser.getName().equals("phone"))
								{
									mode = NONE;
									child.phone.add(phone);
								}
								if(parser.getName().equals("workingHour"))
								{
									mode = NONE;
								}
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
					notifyDataSetChanged();
					dlg.dismiss();
				}
			}.execute();
		}

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
			LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(convertView == null)
				convertView = li.inflate(R.layout.phonecall_expandable_list_item_2, null);
			Restaurant child = children.get(groupPosition).get(childPosition); 

			final String phone = child.phone.size() > 0 ? child.phone.get(0) : null;
			TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
			tv.setText(child.name);
			switch(child.state)
			{
			case Restaurant.OPEN:
				tv.setEnabled(true);
				break;
			case Restaurant.PREPARE:
			case Restaurant.CLOSED:
				tv.setEnabled(false);
				break;
			}
			if(phone != null)
			{
				tv = (TextView)convertView.findViewById(android.R.id.text2);
				tv.setText(phone);
				View call = convertView.findViewById(android.R.id.icon);
				call.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
					}
				});
			}
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
			if(convertView == null)
			{
				LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = li.inflate(android.R.layout.simple_expandable_list_item_2, null);
			}
			TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
			tv.setText(groups.get(groupPosition));
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
		public boolean onItemLongClick(AdapterView<?> parentView, View view, int position, long id)
		{
			if(ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			{
				int groupPosition = ExpandableListView.getPackedPositionGroup(id);
				int childPosition = ExpandableListView.getPackedPositionChild(id);
				if(groupPosition == 0)
				{
					favorites.remove(children.get(0).get(childPosition).id);
					children.get(0).remove(childPosition);
					notifyDataSetChanged();
					
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
					String favs = "";
					for(String fav : favorites)
					{
						favs += ":" + fav;
					}
					if(favs.length() == 0)
						editor.putString("delivery_favorites", "");
					else
						editor.putString("delivery_favorites", favs.substring(1));
					editor.commit();
				}
				else
				{
					if(favorites.add(children.get(groupPosition).get(childPosition).id))
					{
						children.get(0).add(children.get(groupPosition).get(childPosition));
						notifyDataSetChanged();
						
						SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
						String favs = "";
						for(String fav : favorites)
						{
							favs += ":" + fav;
						}
						if(favs.length() == 0)
							editor.putString("delivery_favorites", "");
						else
							editor.putString("delivery_favorites", favs.substring(1));
						editor.commit();
					}
				}
				return true;
			}
			else
				return false;
		}

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
		{
			Intent i = new Intent(context, DeliveryActivity.class);
			i.putExtra("id", children.get(groupPosition).get(childPosition).id);
			i.putExtra("name", children.get(groupPosition).get(childPosition).name);
			i.putExtra("phone", children.get(groupPosition).get(childPosition).phone.get(0));
			i.putExtra("start", children.get(groupPosition).get(childPosition).start);
			i.putExtra("end", children.get(groupPosition).get(childPosition).end);
			context.startActivity(i);
			return true;
		}

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		((TextView)getView().findViewById(android.R.id.empty)).setText(R.string.empty);
		Adapter adapter = new Adapter();
		setListAdapter(adapter);
		getListView().setOnItemLongClickListener(adapter);
		getListView().setOnChildClickListener(adapter);
	}
}
