package kr.gwangyi.posroid.light.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import kr.gwangyi.fragments.ExpandableListFragment;
import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.utilities.XmlUtility;

public class CafeteriaFragment extends ExpandableListFragment
{
	private class Adapter extends BaseExpandableListAdapter
	{
		private static final String FREEDOM_URL = "http://posroid.gwangyi.kr:8799/freedom.xml",
				WISDOM_URL = "http://posroid.gwangyi.kr:8799/wisdom.xml";
		
		private FragmentActivity context;
		
		private class GroupItem
		{
			public boolean category;
			public String name;
			public int month, day;
			
			public GroupItem(boolean category, String name, int month, int day)
			{
				this.category = category; this.name = name; this.month = month; this.day = day;
			}
			
			public GroupItem(boolean category, String name)
			{
				this.category = category; this.name = name; this.month = 0; this.day = 0;
			}
		}
		private class ChildItem
		{
			public int corner;
			public String detail;
			public int calory;
			
			public ChildItem()
			{
				this.corner = 0; this.detail = ""; this.calory = 0;
			}
		}
		
		private List<GroupItem> group = new ArrayList<GroupItem>();
		private List<List<ChildItem>> children = new ArrayList<List<ChildItem>>();

		public Adapter()
		{
			this.context = getActivity();
			
			final DialogFragment dlg = ProgressDialogFragment.newInstance(null, context.getString(R.string.loading));
			dlg.show(context.getSupportFragmentManager(), "dialog");
			
			new AsyncTask<Void, Void, Void>()
			{
				private final Adapter adapter = Adapter.this;
				private int today_group = -1;
				@Override
				protected Void doInBackground(Void... params)
				{
					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")),
							now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
					try
					{
						XmlPullParser parser = XmlUtility.makeInstanceFromUrl(adapter.context, new URL(FREEDOM_URL));
						
						int parserEvent = parser.getEventType();
						HashMap<String, List<ChildItem>> map = new HashMap<String, List<ChildItem>>();
						List<ChildItem> children = null;
						ChildItem child = null;
						int month = 0, day = 0;
						while(parserEvent != XmlPullParser.END_DOCUMENT)
						{
							switch(parserEvent)
							{
							case XmlPullParser.START_TAG:
								if(parser.getName().equals("menu"))
								{
									String a[] = parser.getAttributeValue(null, "date").split("/");
									month = Integer.parseInt(a[0]) - 1; day = Integer.parseInt(a[1]);
									cal.set(Calendar.MONTH, month);
									cal.set(Calendar.DAY_OF_MONTH, day);
									adapter.group.add(new GroupItem(true, adapter.context.getString(R.string.cafeteria_group_format, cal), month, day));
									adapter.children.add(new ArrayList<ChildItem>());
									if(now.get(Calendar.DAY_OF_MONTH) == day)
									{
										today_group = adapter.group.size() - 1;
									}
									map.clear();
								}
								else if(parser.getName().equals("item"))
								{
									String kind = parser.getAttributeValue(null, "kind");
									children = map.get(kind);
									if(children == null)
									{
										GroupItem item = null;
										if("breakfast".equals(kind))
											item = new GroupItem(false, adapter.context.getResources().getStringArray(R.array.cafeteria_kind)[0], month, day);
										else if("lunch".equals(kind))
											item = new GroupItem(false, adapter.context.getResources().getStringArray(R.array.cafeteria_kind)[1], month, day);
										else if("supper".equals(kind))
											item = new GroupItem(false, adapter.context.getResources().getStringArray(R.array.cafeteria_kind)[2], month, day);
										adapter.group.add(item);
										children = new ArrayList<ChildItem>();
										adapter.children.add(children);
										map.put(kind, children);
									}
									child = new ChildItem();
									String corner = parser.getAttributeValue(null, "corner"); 
									if("A".equals(corner))
										child.corner = 0;
									else if("B".equals(corner))
										child.corner = 1;
									else if("C".equals(corner))
										child.corner = 2;
									else if("D".equals(corner))
										child.corner = 3;
									child.calory = Integer.parseInt(parser.getAttributeValue(null, "calory"));
									children.add(child);
								}
								break;
							case XmlPullParser.TEXT:
								if(child != null)
									child.detail += parser.getText();
								break;
							case XmlPullParser.END_TAG:
								if(child != null)
								{
									String [] detail = child.detail.split("\n");
									int i = Locale.getDefault().equals(Locale.KOREAN) ? 0 : 1;
									child.detail = "";
									for(; i < detail.length; i += 2)
									{
										detail[i] = detail[i].trim();
										if(detail[i].length() != 0)
											child.detail += "\n" + detail[i];
									}
									if(child.detail.length() != 0)
										child.detail = child.detail.substring(1);
									child = null;
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
					
					try
					{
						XmlPullParser parser = XmlUtility.makeInstanceFromUrl(adapter.context, new URL(WISDOM_URL));
						
						int parserEvent = parser.getEventType();
						List<ChildItem> children = null;
						ChildItem child = null;
						int month = 0, day = 0;
						while(parserEvent != XmlPullParser.END_DOCUMENT)
						{
							switch(parserEvent)
							{
							case XmlPullParser.START_TAG:
								if(parser.getName().equals("menu"))
								{
									String a[] = parser.getAttributeValue(null, "date").split("/");
									month = Integer.parseInt(a[0]) - 1; day = Integer.parseInt(a[1]);
									children = null;
								}
								else if(parser.getName().equals("item"))
								{
									int x = month * 100 + day;
									int i = 0;
									boolean check = false;
									for(i = 0; i < group.size(); i++)
									{
										GroupItem g = group.get(i);
										if(g.month * 100 + g.day == x)
											check = true;
										else if(check)
											break;
									}
									child = null;
									if(i < group.size() || check)
									{
										//String kind = parser.getAttributeValue(null, "kind");
										if(children == null)
										{
											GroupItem item = null;
											item = new GroupItem(false, adapter.context.getResources().getStringArray(R.array.cafeteria_kind)[3]);
											adapter.group.add(i, item);
											children = new ArrayList<ChildItem>();
											adapter.children.add(i, children);
										}
										child = new ChildItem();
										child.corner = 4;
										child.calory = Integer.parseInt(parser.getAttributeValue(null, "calory"));
										children.add(child);
									}
								}
								break;
							case XmlPullParser.TEXT:
								if(child != null)
									child.detail += parser.getText();
								break;
							case XmlPullParser.END_TAG:
								if(child != null)
								{
									String [] detail = child.detail.split("\n");
									int i = Locale.getDefault().equals(Locale.KOREAN) ? 0 : 1;
									child.detail = "";
									for(; i < detail.length; i += 2)
									{
										detail[i] = detail[i].trim();
										if(detail[i].length() != 0)
											child.detail += "\n" + detail[i];
									}
									if(child.detail.length() != 0)
										child.detail = child.detail.substring(1);
									child = null;
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
					dlg.dismiss();
					if(today_group != -1)
					{
						getListView().expandGroup(today_group);
						getListView().smoothScrollToPosition(getListView().getFlatListPosition(ExpandableListView.getPackedPositionForGroup(today_group)));
					}
					notifyDataSetChanged();
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
			ChildItem child = children.get(groupPosition).get(childPosition);
			View ret = null;
			if(convertView == null)
			{
				LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ret = li.inflate(R.layout.cafeteria_item, null);
			}
			else
				ret = convertView;
			TextView tv = (TextView) ret.findViewById(android.R.id.text1);
			tv.setText(child.detail);
			tv = (TextView) ret.findViewById(android.R.id.text2);
			tv.setText(Integer.toString(child.calory));
			ImageView icon = (ImageView)ret.findViewById(android.R.id.icon);
			switch(child.corner)
			{
			case 0:
				icon.setImageResource(R.drawable.a);
				break;
			case 1:
				icon.setImageResource(R.drawable.b);
				break;
			case 2:
				icon.setImageResource(R.drawable.c);
				break;
			case 3:
				icon.setImageResource(R.drawable.d);
				break;
			case 4:
				icon.setImageResource(R.drawable.arrow_icon);
				break;
			}
			return ret;
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
			LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View ret = null;
			if(group.get(groupPosition).category)
			{
				ret = li.inflate(android.R.layout.preference_category, null);
				TextView tv = (TextView) ret.findViewById(android.R.id.title);
				tv.setText(group.get(groupPosition).name);
				
			}
			else
			{
				View cap;
				ImageView icon;
				ret = new RelativeLayout(context);
				icon = new ImageView(context);
				((ViewGroup)ret).addView(icon);
				cap = li.inflate(android.R.layout.simple_expandable_list_item_1, (ViewGroup)ret);
				if(isExpanded)
					icon.setImageResource(R.drawable.expander_ic_maximized);
				else
					icon.setImageResource(R.drawable.expander_ic_minimized);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
				lp.addRule(RelativeLayout.CENTER_VERTICAL);
				icon.setLayoutParams(lp);
				TextView tv = (TextView) cap.findViewById(android.R.id.text1);
				tv.setText(group.get(groupPosition).name);
			}
			return ret;
		}

		@Override
		public boolean hasStableIds()
		{
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return false;
		}

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		getListView().setGroupIndicator(getResources().getDrawable(R.drawable.empty_selector));
		setListAdapter(new Adapter());
		
		((TextView)getView().findViewById(android.R.id.empty)).setText(getString(R.string.empty));
	}
}
