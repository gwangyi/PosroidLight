package kr.gwangyi.posroid.light.fragments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import kr.gwangyi.fragments.ExpandableListFragment;
import kr.gwangyi.posroid.light.R;

public class DeliveryMenuFragment extends ExpandableListFragment
{
	private List<List<Item>> children;
	private List<String> groups;
	
	public static class ChildrenWrapper implements Parcelable
	{
		public List<List<Item>> children;
		
		public ChildrenWrapper(List<List<Item>> children)
		{
			this.children = children;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeList(children);
		}
		
		public static final Parcelable.Creator<ChildrenWrapper> CREATOR = new Creator<ChildrenWrapper>() {
			public ChildrenWrapper createFromParcel(Parcel source)
			{
				ChildrenWrapper i = new ChildrenWrapper(new ArrayList<List<Item>>());
				source.readList(i.children, null);
				return i;
			}

			public ChildrenWrapper[] newArray(int size)
			{
				return new ChildrenWrapper[size];
			}
		};
	}
	
	public static class Item
	{
		public String name = "";
		public String note = "";
		public List<String> pictures = new ArrayList<String>();
		public String detail = null;
	}
	
	public static class DetailDialogFragment extends DialogFragment
	{
		private String name;
		private String detail;
		private String [] pictures;
		
		public static DetailDialogFragment newInstance(String name, String detail, String[] pictures)
		{
			DetailDialogFragment dlg = new DetailDialogFragment();
			Bundle args = new Bundle();
			args.putString("name", name);
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
			name = args.getString("name");
			detail = args.getString("detail");
			pictures = args.getStringArray("pictures");
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dlg = super.onCreateDialog(savedInstanceState);
			dlg.setTitle(name);
			return dlg;
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
	
	public static DeliveryMenuFragment newInstance(ArrayList<String> groups, List<List<Item>> children)
	{
		DeliveryMenuFragment frag = new DeliveryMenuFragment();
		Bundle args = new Bundle();
		ChildrenWrapper myChildren = new ChildrenWrapper(children);
		args.putStringArrayList("groups", groups);
		args.putParcelable("children", myChildren);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		groups = args.getStringArrayList("groups");
		ChildrenWrapper myChildren = (ChildrenWrapper)args.getParcelable("children");
		children = myChildren.children;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		((TextView)getView().findViewById(android.R.id.empty)).setText(R.string.empty);
		MenuAdapter adapter = new MenuAdapter();
		setListAdapter(adapter);
		getListView().setOnChildClickListener(adapter);
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
			LayoutInflater li = getActivity().getLayoutInflater();
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
			LayoutInflater li = getActivity().getLayoutInflater();
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
				String [] pictures = null;
				if(i.pictures.size() > 0)
					pictures = i.pictures.toArray(new String[0]);
				
				DetailDialogFragment dlg = DetailDialogFragment.newInstance(i.name, i.detail, pictures);
				dlg.show(getActivity().getSupportFragmentManager(), "dialog");
				return true;
			}
		}
	}
}
