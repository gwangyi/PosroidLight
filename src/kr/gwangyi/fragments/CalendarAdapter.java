package kr.gwangyi.fragments;

public interface CalendarAdapter
{
	public String scheduleOf(int year, int month, int day);
	public void onItemClick(int year, int month, int day);
}
