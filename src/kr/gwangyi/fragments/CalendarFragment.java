package kr.gwangyi.fragments;

import java.util.Calendar;
import java.util.TimeZone;

import kr.gwangyi.posroid.light.R;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CalendarFragment extends Fragment
{
	private int year;
	private int month;
	
	private ViewPager calendar;
	
	private int monthOffset = 0;
	
	private CalendarAdapter schedule;
	
	private class Adapter extends PagerAdapter // Fragment 안에서는 Fragment를 또 만들 수 없다. 그래서 PageAdapter로 구현
	{
		@Override
		public Object instantiateItem(ViewGroup container, int position) // View를 처음 만들때만 호출됨. update할때는 호출 안됨!
		{
			LayoutInflater inflater = (LayoutInflater)container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.calendar_month, container, false);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")), today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
			cal.set(year, position + monthOffset, 1);
			int month = cal.get(Calendar.MONTH);
			int firstday = cal.getFirstDayOfWeek();
			
			TableLayout tbl = (TableLayout)view.findViewById(android.R.id.list);
			
			Context context = container.getContext();
			TableRow row = new TableRow(context);
			cal.set(Calendar.DAY_OF_WEEK, firstday);
			do
			{
				TextView tv = new TextView(context);
				tv.setText(String.format("%ta", cal));
				tv.setGravity(Gravity.CENTER);
				switch(cal.get(Calendar.DAY_OF_WEEK))
				{
				case Calendar.SUNDAY:
					tv.setTextColor(Color.rgb(255, 160, 128));
					break;
				case Calendar.SATURDAY:
					tv.setTextColor(Color.rgb(160, 128, 255));
					break;
				}
				tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)); // 1.0f 에 조심. float를 넘겨줘야함
				row.addView(tv);
				cal.roll(Calendar.DAY_OF_WEEK, true);
			} while(cal.get(Calendar.DAY_OF_WEEK) != firstday);
			tbl.addView(row);
			
			do
			{
				row = new TableRow(context);
				do
				{
					View item = inflater.inflate(R.layout.calendar_item, row, false);
					item.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
					TextView day = (TextView)item.findViewById(android.R.id.text1);
					day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
					if(cal.get(Calendar.MONTH) != month) day.setEnabled(false);
					else
					{
						switch(cal.get(Calendar.DAY_OF_WEEK))
						{
						case Calendar.SUNDAY:
							day.setTextColor(Color.rgb(255, 160, 128));
							break;
						case Calendar.SATURDAY:
							day.setTextColor(Color.rgb(160, 128, 255));
							break;
						}
						if(today.get(Calendar.MONTH) == month && cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
						{
							day.setTextColor(Color.rgb(255, 255,0));
						}
						if(schedule != null)
						{
							TextView detail = (TextView)item.findViewById(android.R.id.text2);
							final int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH), d = cal.get(Calendar.DAY_OF_MONTH);
							detail.setText(schedule.scheduleOf(y, m, d));
							item.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									schedule.onItemClick(y, m, d);
								}
							});
						}
					}
					row.addView(item);
					cal.add(Calendar.DAY_OF_MONTH, 1);
				} while(cal.get(Calendar.DAY_OF_WEEK) != firstday);
				tbl.addView(row);
			} while(cal.get(Calendar.MONTH) == month);
			
			container.addView(view);
			return view;
		}
		
		@Override
		public CharSequence getPageTitle(int position)
		{
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
			cal.set(year, position + monthOffset, 1);
			return getString(R.string.calendar_title, cal);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView((View)object);
		}

		@Override
		public int getCount()
		{
			return 12;
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return view == (View)object;
		}
	}
	
	public static CalendarFragment newInstance(int monthOffset)
	{
		CalendarFragment frag = new CalendarFragment();
		Bundle args = new Bundle();
		args.putInt("monthOffset", monthOffset);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		monthOffset = getArguments().getInt("monthOffset");
		if(month < monthOffset) year --;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View ret = inflater.inflate(R.layout.calendar, container, false);
		calendar = (ViewPager)ret.findViewById(android.R.id.content);
		calendar.setAdapter(new Adapter());
		calendar.setCurrentItem(month - monthOffset);
		return ret;
	}
	
	public void setAdapter(CalendarAdapter adapter)
	{
		int currentItem = calendar.getCurrentItem();
		calendar.setAdapter(null);
		this.schedule = adapter;
		calendar.setAdapter(new Adapter());
		calendar.setCurrentItem(currentItem);
	}

}
