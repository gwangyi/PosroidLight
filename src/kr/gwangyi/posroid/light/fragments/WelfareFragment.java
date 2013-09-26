package kr.gwangyi.posroid.light.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import kr.gwangyi.fragments.ExpandableListFragment;
import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.activities.WelfareActivity;
import kr.gwangyi.posroid.light.utilities.OpenAt;
import kr.gwangyi.posroid.light.utilities.XmlUtility;

public class WelfareFragment extends ExpandableListFragment
{
	private static enum NameState
	{
		NONE, CATEGORY, FACILITY, STATE
	}
	
	private class Adapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener
	{
		private static final String URL = "http://posroid.gwangyi.kr:8799/welfare";
		
		private FragmentActivity context;
		
		private class Facility
		{
			public String name, phone;
			public String state, start, end;
			public String detail = "";
		}
		
		private List<String> groups = new ArrayList<String>();
		private List<List<Facility>> children = new ArrayList<List<Facility>>();
		
		private class AdapterTask extends AsyncTask<Void, Void, Void> implements DialogInterface.OnCancelListener
		{
			private ProgressDialogFragment dlg;
			
			private Adapter adapter = Adapter.this;
			
			@Override
			protected Void doInBackground(Void... params)
			{
				PostechCalendarAdapter calendar = new PostechCalendarAdapter(context);
				String url = URL;
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")); 
				String [] schedules = calendar.scheduleOf(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).split("\n");
				boolean check = false;
				for(String schedule : schedules)
				{
					if(schedule.equals("하기방학") || schedule.equals("Summer Recess"))
					{
						url += "_summervacation.xml";
						check = true;
						break;
					}
					else if(schedule.equals("동기방학") || schedule.equals("Winter Recess"))
					{
						url += "_wintervacation.xml";
						check = true;
						break;
					}
				}
				if(!check) url += "_nonvacation.xml";
				
				try
				{
					XmlPullParser parser = XmlUtility.makeInstanceFromUrl(adapter.context, new URL(url));
					
					String name = null, neut_name = null, phone = null, state_name = null;
					OpenAt open_at = null;
					List<Facility> children = null;
					Facility child = null;
					Stack<NameState> name_mode = new Stack<NameState>();
					boolean match = false;
					name_mode.push(NameState.NONE);
					
					int parserEvent = parser.getEventType();
					while(parserEvent != XmlPullParser.END_DOCUMENT)
					{
						switch(parserEvent)
						{
						case XmlPullParser.START_TAG:
							if(parser.getName().equals("category"))
							{
								name_mode.push(NameState.CATEGORY);
								children = new ArrayList<WelfareFragment.Adapter.Facility>();
								adapter.children.add(children);
							}
							else if(parser.getName().equals("facility"))
							{
								name_mode.push(NameState.FACILITY);
								child = new Facility();
								children.add(child);
							}
							else if(parser.getName().equals("open_at"))
							{
								open_at = new OpenAt(parser);
								if(open_at.isConditionOk()) name_mode.push(NameState.STATE);
								match = open_at.isOpened();
								if(match)
								{
									child.start = open_at.getStart();
									child.end = open_at.getEnd();
								}
								else if(child.start == null || child.end == null)
								{
									child.start = open_at.getStart();
									child.end = open_at.getEnd();
								}
							}
							else if(parser.getName().equals("name"))
							{
								if(name_mode.peek() != NameState.NONE)
								{
									String lang = parser.getAttributeValue(null, "lang");
									if(lang != null && lang.equals(Locale.getDefault().getLanguage()))
										name = "";
									else if(lang == null || lang.equals("en"))
										neut_name = "";
								}
							}
							else if(parser.getName().equals("phone"))
							{
								phone = "";
							}
							break;
						case XmlPullParser.TEXT:
							if(name != null) name += parser.getText();
							else if(neut_name != null) neut_name += parser.getText();
							else if(phone != null) phone += parser.getText();
							break;
						case XmlPullParser.END_TAG:
							if(parser.getName().equals("name"))
							{
								if(name == null) name = neut_name;
								if(name != null && name_mode.peek() != NameState.NONE)
								{
									NameState state = name_mode.pop();
									switch(state)
									{
									case CATEGORY:
										groups.add(name);
										break;
									case FACILITY:
										child.name = name;
										break;
									case STATE:
										if(name.length() == 0) name = null;
										state_name = name;
										break;
									default:
									}
									if(neut_name != null) name_mode.push(state);
								}
								name = null;
								neut_name = null;
							}
							else if(parser.getName().equals("phone"))
							{
								child.phone = phone;
								phone = null;
							}
							else if(parser.getName().equals("open_at"))
							{
								if(open_at != null)
								{
									if(state_name == null)
										state_name = getActivity().getString(R.string.open);
									child.detail += String.format("\n%s: %s - %s", state_name, open_at.getStart(), open_at.getEnd());
									
									if(match)
										child.state = state_name;
								}
								state_name = null;
								open_at = null;
								if(name_mode.peek() == NameState.STATE) name_mode.pop();
							}
							else if(parser.getName().equals("facility"))
							{
								if(name_mode.peek() == NameState.FACILITY) name_mode.pop();
							}
							else if(parser.getName().equals("category"))
							{
								if(name_mode.peek() == NameState.CATEGORY) name_mode.pop();
							}
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
				dlg = ProgressDialogFragment.newInstance(null, context.getString(R.string.loading));
				dlg.show(context.getSupportFragmentManager(), "dialog");
				dlg.setOnCancelListener(this);
			}
			
			protected void onPostExecute(Void result)
			{
				notifyDataSetChanged();
				dlg.dismissAllowingStateLoss();
			}

			@Override
			public void onCancel(DialogInterface dialog)
			{
				this.cancel(true);
				Toast.makeText(getActivity(), R.string.cancelled, Toast.LENGTH_SHORT).show();
			}
		}
		
		public Adapter()
		{
			this.context = getActivity();
			
			new AdapterTask().execute();
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
			Facility child = children.get(groupPosition).get(childPosition); 

			final String phone = child.phone;
			TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
			tv.setText(child.name);
			if(child.state == null)
				tv.setEnabled(false);
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
			else
				convertView.findViewById(android.R.id.icon).setVisibility(View.GONE);
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
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
		{
			Facility facility = children.get(groupPosition).get(childPosition);
			Intent i = new Intent(context, WelfareActivity.class);
			i.putExtra("name", facility.name);
			i.putExtra("phone", facility.phone);
			i.putExtra("state", facility.state);
			i.putExtra("start", facility.start);
			i.putExtra("end", facility.end);
			i.putExtra("detail", facility.detail);
			startActivity(i);
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
		getListView().setOnChildClickListener(adapter);
	}
}
