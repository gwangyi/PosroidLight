package kr.gwangyi.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ExpandableListFragment extends ListFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		FrameLayout layout = new FrameLayout(inflater.getContext());
		TextView empty = new TextView(inflater.getContext());
		ExpandableListView listView = new ExpandableListView(inflater.getContext());
		empty.setLayoutParams(params);
		empty.setGravity(Gravity.CENTER);
		listView.setLayoutParams(params);
		empty.setId(android.R.id.empty);
		listView.setId(android.R.id.list);
		layout.addView(empty);
		layout.addView(listView);
		return layout;
	}
	
	@Override
	public ExpandableListView getListView()
	{
		return (ExpandableListView)super.getListView();
	}
	
	public ExpandableListAdapter getExpandableListAdapter()
	{
		return getListView().getExpandableListAdapter();
	}
	
	public void setListAdapter(ExpandableListAdapter adapter)
	{
		getListView().setAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		int groupPosition, childPosition;
		super.onListItemClick(l, v, position, id);
		switch(ExpandableListView.getPackedPositionType(id))
		{
		case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
			groupPosition = ExpandableListView.getPackedPositionGroup(id);
			childPosition = ExpandableListView.getPackedPositionChild(id);
			if(onChildClick(getListView(), v, groupPosition, childPosition, getExpandableListAdapter().getChildId(groupPosition, childPosition)))
				return;
			break;
		}
	}
	
	protected boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	{
		return false;
	}
}
