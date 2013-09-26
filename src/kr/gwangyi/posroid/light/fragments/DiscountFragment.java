package kr.gwangyi.posroid.light.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import kr.gwangyi.posroid.light.activities.DiscountActivity;
import kr.gwangyi.posroid.light.utilities.OpenAt;
import kr.gwangyi.posroid.light.utilities.XmlUtility;

public class DiscountFragment extends ExpandableListFragment {
	private static enum NameState
	{
		NONE, REGION, STORE, STATE
	}
	
	private class Adapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener
	{
		private static final String URL = "http://posroid.gwangyi.kr:8799/discount.xml";
		
		private FragmentActivity context;
		
		private class Store
		{
			public String name, category;
			public String detail;
			public String phone, businessDay;
			public String start, end;
			public String id;
			public String[] menu;
			public double longitude, latitude;
			public boolean open = false;
		}
		
		private List<String> groups = new ArrayList<String>();
		private List<List<Store>> children = new ArrayList<List<Store>>();
		
		private class AdapterTask extends AsyncTask<Void, Void, Void> implements DialogInterface.OnCancelListener
		{
			private Adapter adapter = Adapter.this;
			private ProgressDialogFragment dlg;
			
			@Override
			protected Void doInBackground(Void... params)
			{
				try
				{
					XmlPullParser parser = XmlUtility.makeInstanceFromUrl(adapter.context, new URL(URL));
					
					String name = null, neut_name = null, category = null, neut_category = null, detail = null, neut_detail = null;
					String business = null, neut_business = null, phone = null, item = null;
					OpenAt open_at;
					List<Store> children = null;
					List<String> menu = null;
					Store child = null;
					Stack<NameState> name_mode = new Stack<NameState>();
					int last_region = 0;
					boolean match;
					name_mode.push(NameState.NONE);
					
					int parserEvent = parser.getEventType();
					while(parserEvent != XmlPullParser.END_DOCUMENT)
					{
						switch(parserEvent)
						{
						case XmlPullParser.START_TAG:
							if(parser.getName().equals("region"))
							{
								name_mode.push(NameState.REGION);
								children = new ArrayList<Store>();
								adapter.children.add(children);
							}
							else if(parser.getName().equals("store"))
							{
								name_mode.push(NameState.STORE);
								child = new Store();
								children.add(child);
								child.id = parser.getAttributeValue(null, "id");
							}
							else if(parser.getName().equals("category"))
							{
								String lang = parser.getAttributeValue(null, "lang");
								if(Locale.getDefault().getLanguage().equals(lang))
									category = "";
								else
									neut_category = "";
							}
							else if(parser.getName().equals("name"))
							{
								if(name_mode.peek() != NameState.NONE)
								{
									String lang = parser.getAttributeValue(null, "lang");
									if(Locale.getDefault().getLanguage().equals(lang))
										name = "";
									else
										neut_name = "";
								}
							}
							else if(parser.getName().equals("detail"))
							{
								String lang = parser.getAttributeValue(null, "lang");
								if(Locale.getDefault().getLanguage().equals(lang))
									detail = "";
								else
									neut_detail = "";
							}
							else if(parser.getName().equals("closing"))
							{
								String lang = parser.getAttributeValue(null, "lang");
								if(Locale.getDefault().getLanguage().equals(lang))
									business = "";
								else
									neut_business = "";
							}
							else if(parser.getName().equals("phone"))
							{
								phone = "";
							}
							else if(parser.getName().equals("open_at"))
							{
								open_at = new OpenAt(parser);
								match = open_at.isOpened();
								if(open_at.isConditionOk())
									child.open = open_at.isOpened();
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
							else if(parser.getName().equals("menu"))
							{
								menu = new ArrayList<String>();
							}
							else if(parser.getName().equals("item"))
							{
								item = "";
							}
							else if(parser.getName().equals("pos"))
							{
								if(child != null)
								{
									child.latitude = Double.parseDouble(parser.getAttributeValue(null, "latitude"));
									child.longitude = Double.parseDouble(parser.getAttributeValue(null, "longitude"));
								}
							}
							break;
						case XmlPullParser.TEXT:
							if(name != null) name += parser.getText();
							else if(neut_name != null) neut_name += parser.getText();
							else if(category != null) category += parser.getText();
							else if(neut_category != null) neut_category += parser.getText();
							else if(detail != null) detail += parser.getText();
							else if(neut_detail != null) neut_detail += parser.getText();
							else if(business != null) business += parser.getText();
							else if(neut_business != null) neut_business += parser.getText();
							else if(phone != null) phone += parser.getText();
							else if(item != null) item += parser.getText();
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
									case REGION:
										if(groups.size() <= last_region)
											groups.add(name);
										else
											groups.set(last_region, name);
										break;
									case STORE:
										if(child.name == null || neut_name == null) child.name = name;
										break;
									default:
									}
									if(neut_name != null) name_mode.push(state);
								}
								name = null;
								neut_name = null;
							}
							if(parser.getName().equals("category"))
							{
								if(category == null) category = neut_category;
								if(neut_category == null || child.category == null) child.category = category;
								neut_category = null;
								category = null;
							}
							if(parser.getName().equals("detail"))
							{
								if(detail == null) detail = neut_detail;
								if(neut_detail == null || child.detail == null) child.detail = detail;
								detail = null;
								neut_detail = null;
							}
							else if(parser.getName().equals("store"))
							{
								if(name_mode.peek() == NameState.STORE) name_mode.pop();
							}
							else if(parser.getName().equals("region"))
							{
								if(name_mode.peek() == NameState.REGION) name_mode.pop();
								last_region ++;
							}
							else if(parser.getName().equals("closing"))
							{
								if(business == null) business = neut_business;
								if(neut_business == null || child.businessDay == null) child.businessDay = business;
								neut_business = null;
								business = null;
							}
							else if(parser.getName().equals("phone"))
							{
								child.phone = phone;
								phone = null;
							}
							else if(parser.getName().equals("item"))
							{
								if(menu != null) menu.add(item);
								item = null;
							}
							else if(parser.getName().equals("menu"))
							{
								if(child.menu == null)
								{
									child.menu = menu.toArray(new String[]{});
								}
								menu = null;
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
				dlg.setOnCancelListener(this);
				dlg.show(context.getSupportFragmentManager(), "dialog");
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
			return children.get(groupPosition).get(childPosition);
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
				convertView = li.inflate(android.R.layout.simple_expandable_list_item_2, null);
			Store child = children.get(groupPosition).get(childPosition); 

			TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
			tv.setEnabled(child.open);
			tv.setText(String.format("%s(%s)", child.name, child.category));
			tv = (TextView)convertView.findViewById(android.R.id.text2);
			tv.setText(child.detail);
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
			Store store = children.get(groupPosition).get(childPosition);
			Intent i = new Intent(context, DiscountActivity.class);
			i.putExtra("id", store.id);
			i.putExtra("name", store.name);
			i.putExtra("phone", store.phone);
			i.putExtra("detail", store.detail);
			i.putExtra("businessDay", store.businessDay);
			i.putExtra("start", store.start);
			i.putExtra("end", store.end);
			i.putExtra("menu", store.menu);
			i.putExtra("latitude", store.latitude);
			i.putExtra("longitude", store.longitude);
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
