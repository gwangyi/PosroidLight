package kr.gwangyi.posroid.light.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import kr.gwangyi.fragments.CalendarFragment;
import kr.gwangyi.fragments.ProgressDialogFragment;
import kr.gwangyi.posroid.light.R;

public class PostechCalendarFragment extends CalendarFragment
{
	private class CalendarTask extends AsyncTask<Void, Void, PostechCalendarAdapter> implements DialogInterface.OnCancelListener
	{
		private ProgressDialogFragment dlg;
		
		@Override
		protected PostechCalendarAdapter doInBackground(Void... params)
		{
			return new PostechCalendarAdapter(getActivity());
		}
		
		@Override
		protected void onPreExecute()
		{
			dlg = ProgressDialogFragment.newInstance(null, getString(R.string.loading));
			dlg.show(getFragmentManager(), "dialog");
			dlg.setOnCancelListener(this);
		}
		
		@Override
		protected void onPostExecute(PostechCalendarAdapter result)
		{
		    ((CalendarFragment)getFragmentManager().findFragmentByTag("calendar")).setAdapter(result);
		    dlg.dismissAllowingStateLoss();
		}
		
		@Override
		public void onCancel(DialogInterface dialog)
		{
			this.cancel(true);
			Toast.makeText(getActivity(), R.string.cancelled, Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		new CalendarTask().execute();
	}
}
