package kr.gwangyi.posroid.light.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import kr.gwangyi.fragments.CalendarFragment;
import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;

public class PostechCalendarFragment extends CalendarFragment
{
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		final DialogFragment dlg = ProgressDialogFragment.newInstance(null, getString(R.string.loading));
		dlg.show(getFragmentManager(), "dialog");
		new AsyncTask<Void, Void, PostechCalendarAdapter>()
		{
			@Override
			protected PostechCalendarAdapter doInBackground(Void... params)
			{
				return new PostechCalendarAdapter(getActivity());
			}
			
			@Override
			protected void onPostExecute(PostechCalendarAdapter result)
			{
			    ((CalendarFragment)getFragmentManager().findFragmentByTag("calendar")).setAdapter(result);
			    dlg.dismiss();
			}
		}.execute();
	}
}
