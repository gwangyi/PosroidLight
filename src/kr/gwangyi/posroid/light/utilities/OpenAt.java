package kr.gwangyi.posroid.light.utilities;

import java.util.Calendar;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;

public final class OpenAt {
	private boolean mCondOk = false, mOpened;
	private String mStart, mEnd;
	
	public OpenAt(XmlPullParser parser)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		boolean cond_ok = false, cond_ok_2nd = false;
		String cond = parser.getAttributeValue(null, "cond");
		if(cond != null)
		{
			cal.add(Calendar.DAY_OF_MONTH, -1);
			for(int i = 0; i < 2; i++)
			{
				String w_cond = cond;
				cond_ok = false;
				if(w_cond.startsWith("!"))
				{
					cond_ok = true;
					w_cond = w_cond.substring(1);
				}
				if(w_cond.equals("weekday"))
				{
					w_cond = "weekend";
					cond_ok = !cond_ok;
				}
				if(w_cond.equals("weekend"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
							cal.get(Calendar.DAY_OF_WEEK)== Calendar.SATURDAY)
						cond_ok = !cond_ok;
				}
				else if(w_cond.equals("firstday"))
				{
					if(cal.get(Calendar.DAY_OF_MONTH) == 1)
						cond_ok = !cond_ok;
				}
				else if(w_cond.equals("lastday"))
				{
					if(cal.getActualMaximum(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH))
						cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Sun"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Mon"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Tue"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Wed"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Thu"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Fri"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) cond_ok = !cond_ok;
				}
				else if(w_cond.equals("Sat"))
				{
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) cond_ok = !cond_ok;
				}
				if(i == 0)
				{
					cond_ok_2nd = cond_ok;
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
		}
		else
		{
			cond_ok = true;
			cond_ok_2nd = true;
		}
		mStart = parser.getAttributeValue(null, "start");
		mEnd = parser.getAttributeValue(null, "end");
		if(cond_ok_2nd && mEnd != null && mStart != null && mEnd.compareTo(mStart) < 0)
		{
			String now = String.format("%tR", cal);
			mOpened = mEnd.compareTo(now) > 0;
			mCondOk = mOpened;
		}
		if(!mCondOk && cond_ok)
		{
			String end;
			if(mStart == null) mStart = "00:00";
			if(mEnd == null) mEnd = "24:00";
			end = mEnd;
			if(mEnd.compareTo(mStart) < 0) end = "24:00";

			String now = String.format("%tR", cal);
			mCondOk = true;
			mOpened = (mStart.compareTo(now) <= 0 && end.compareTo(now) >= 0);
		}
	}
	
	public boolean isConditionOk() { return mCondOk; }
	public boolean isOpened() { return mOpened; }
	public String getStart() { return mStart; }
	public String getEnd() { return mEnd; }
}
