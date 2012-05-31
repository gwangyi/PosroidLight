package kr.gwangyi.posroid.light.fragments;

import java.util.Calendar;
import java.util.TimeZone;

import kr.gwangyi.posroid.light.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DeliveryOverviewFragment extends Fragment
{
	private String start, end, note;
	private String[] phone;
	
	public static DeliveryOverviewFragment newInstance(String name, String start, String end, String detail, String [] phone)
	{
		DeliveryOverviewFragment frag = new DeliveryOverviewFragment();
		Bundle args = new Bundle();
		args.putString("name", name);
		args.putString("start", start);
		args.putString("end", end);
		args.putStringArray("phone", phone);
		args.putString("note", detail);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		start = args.getString("start");
		end = args.getString("end");
		note = args.getString("note");
		phone = args.getStringArray("phone");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.delivery_overview, container, false);

		((TextView)view.findViewById(R.id.from)).setText(start);
		((TextView)view.findViewById(R.id.to)).setText(end);
		((TextView)view.findViewById(R.id.detail)).setText(note);
		
		if(this.phone != null)
		{
			ListView phone = (ListView)view.findViewById(R.id.phone);
			phone.setAdapter(
					new ArrayAdapter<String>(inflater.getContext(), R.layout.simple_call_list_item, android.R.id.text1, this.phone));
			phone.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					getActivity().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + DeliveryOverviewFragment.this.phone[position])));
				}
			});
		}
		
		String [] hms;
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		
		hms = this.start.split(":");
		start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hms[0]));
		start.set(Calendar.MINUTE, Integer.parseInt(hms[1]));
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		
		hms = this.end.split(":");
		end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hms[0]));
		end.set(Calendar.MINUTE, Integer.parseInt(hms[1]));
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);
		
		if(start.compareTo(end) > 0)
		{
			if(now.compareTo(end) < 0)
				start.add(Calendar.DAY_OF_MONTH, -1);
			else if(now.compareTo(start) > 0)
				end.add(Calendar.DAY_OF_MONTH, + 1);
		}
		
		ProgressBar progress = (ProgressBar)view.findViewById(R.id.working);
		progress.setMax((int)(end.getTimeInMillis() - start.getTimeInMillis()));
		if(now.compareTo(start) < 0)
			progress.setProgress(0);
		else if(now.compareTo(end) < 0)
			progress.setProgress((int)(now.getTimeInMillis() - start.getTimeInMillis()));
		else
			progress.setProgress((int)(end.getTimeInMillis() - start.getTimeInMillis()));
		
		return view;
	}
}
