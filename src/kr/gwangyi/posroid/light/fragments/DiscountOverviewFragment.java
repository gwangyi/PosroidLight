package kr.gwangyi.posroid.light.fragments;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DiscountOverviewFragment extends Fragment {
	private final static String STORE_IMAGE = "http://posroid.gwangyi.kr:8799/img/discount/";
	
	private String start, end, note;
	private String phone, businessDay;
	
	private class DownloadTask extends AsyncTask<String, Void, Bitmap> implements DialogInterface.OnCancelListener
	{
		private ProgressDialogFragment dlg;
		
		@Override
		protected Bitmap doInBackground(String... params)
		{
			String id = params[0];
			InputStream image;
			try {
				image = getActivity().openFileInput(id + ".jpg");
			} catch (FileNotFoundException e) {
				try {
					image = new URL(STORE_IMAGE + id + ".jpg").openStream();
					byte[] buffer = new byte[1024];
					int size;
					FileOutputStream fos = getActivity().openFileOutput(id + ".jpg", Context.MODE_PRIVATE);
					while ((size = image.read(buffer)) >= 0)
					{
						fos.write(buffer, 0, size);
					}
					fos.close();
					image.close();
					image = getActivity().openFileInput(id + ".jpg");
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					return null;
				} catch (IOException e1) {
					e1.printStackTrace();
					return null;
				}
			}
			Bitmap b;
			
			BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inScaled = false;
            options.inSampleSize = 4;
            
            b = BitmapFactory.decodeStream(image, null, options);
			try {
				image.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return b;
		}
		
		@Override
		protected void onPreExecute()
		{
			dlg = ProgressDialogFragment.newInstance(null, getString(R.string.loading));
			dlg.setOnCancelListener(this);
			dlg.show(getFragmentManager(), "dialog");
		}
		
		protected void onPostExecute(Bitmap result)
		{
			if(result != null)
			{
				ImageView photo = (ImageView)getView().findViewById(R.id.photo);
				photo.setImageBitmap(result);
			}
			dlg.dismissAllowingStateLoss();
		}

		@Override
		public void onCancel(DialogInterface dialog)
		{
			this.cancel(true);
			Toast.makeText(getActivity(), R.string.cancelled, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static DiscountOverviewFragment newInstance(String id, String name, String start, String end, String detail, String phone, String businessDay)
	{
		DiscountOverviewFragment frag = new DiscountOverviewFragment();
		Bundle args = new Bundle();
		args.putString("id", id);
		args.putString("name", name);
		args.putString("start", start);
		args.putString("end", end);
		args.putString("phone", phone);
		args.putString("note", detail);
		args.putString("businessDay", businessDay);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		String id;
		
		Bundle args = getArguments();
		start = args.getString("start");
		end = args.getString("end");
		note = args.getString("note");
		phone = args.getString("phone");
		businessDay = args.getString("businessDay");
		id = args.getString("id");

		new DownloadTask().execute(id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.discount_overview, container, false);

		((TextView)view.findViewById(R.id.from)).setText(start);
		((TextView)view.findViewById(R.id.to)).setText(end);
		((TextView)view.findViewById(R.id.detail)).setText(note);
		if(businessDay != null)
			((TextView)view.findViewById(R.id.business_day)).setText(getString(R.string.closed_message, businessDay));
		
		View phoneView = view.findViewById(R.id.phone);
		if(this.phone != null)
		{
			TextView tv = (TextView)phoneView.findViewById(android.R.id.text1);
			tv.setText(this.phone);
			phoneView.setOnClickListener(new View.OnClickListener()
			{
				private String phone = DiscountOverviewFragment.this.phone;

				@Override
				public void onClick(View v) {
					getActivity().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
				}
			});
		}
		else
			phoneView.setVisibility(View.GONE);
		
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
