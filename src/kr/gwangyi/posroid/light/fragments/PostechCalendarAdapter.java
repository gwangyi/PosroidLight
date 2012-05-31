package kr.gwangyi.posroid.light.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import kr.gwangyi.fragments.CalendarAdapter;
import kr.gwangyi.fragments.SimpleTextDialogFragment;
import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.utilities.XmlUtility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class PostechCalendarAdapter implements CalendarAdapter
{
	private static final String URL = "http://posroid.gwangyi.kr:8799/cal/";
	private FragmentActivity context;
	
	private class Event
	{
		public Calendar start, end;
		public String summary;
	}
	
	private Map<Calendar, Event> events;
	
	public PostechCalendarAdapter(FragmentActivity context)
	{
		this.context = context;
		String url;
		if(Locale.getDefault().getLanguage().equals("ko"))
			url = URL + "korean.xml";
		else
			url = URL + "english.xml";
		
		events = new TreeMap<Calendar, Event>();
		try
		{
			XmlPullParser parser;
			parser = XmlUtility.makeInstanceFromUrl(context, new URL(url));
			
			Event e = null;
			String text = "";
			int parserEvent = parser.getEventType();
			while(parserEvent != XmlPullParser.END_DOCUMENT)
			{
				switch(parserEvent)
				{
				case XmlPullParser.START_TAG:
					if(parser.getName().equals("event"))
						e = new Event();
					text = "";
					break;
				case XmlPullParser.END_TAG:
					if(parser.getName().equals("event"))
						events.put(e.start, e);
					else if(parser.getName().equals("dtstart") || parser.getName().equals("dtend"))
					{
						String items[] = text.split("/");
						Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
						c.set(Integer.parseInt(items[2]), Integer.parseInt(items[0]) - 1, Integer.parseInt(items[1]));
						if(parser.getName().equals("dtstart"))
							e.start = c;
						else
							e.end = c;
					}
					else if(parser.getName().equals("summary"))
						e.summary = text;
					text = "";
					break;
				case XmlPullParser.TEXT:
					text += parser.getText();
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
	}

	@Override
	public String scheduleOf(int year, int month, int day)
	{
		String schedule = "";
		for(Map.Entry<Calendar, Event> e : events.entrySet())
		{
			if(String.format("%tF", e.getValue().start).compareTo(String.format("%04d-%02d-%02d", year, month + 1, day)) <= 0 &&
					String.format("%tF", e.getValue().end).compareTo(String.format("%04d-%02d-%02d", year, month + 1, day)) >= 0 )
			{
				schedule = "\n" + e.getValue().summary + schedule;
			}
		}
		if(schedule.length() == 0) return "";
		else return schedule.substring(1);
	}

	@Override
	public void onItemClick(int year, int month, int day)
	{
		String schedule = scheduleOf(year, month, day);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		cal.set(year, month, day);
		
		DialogFragment newFragment = SimpleTextDialogFragment.newInstance(
                context.getString(R.string.calendar_item, cal), schedule);
        newFragment.show(context.getSupportFragmentManager(), "dialog");
	}

}
