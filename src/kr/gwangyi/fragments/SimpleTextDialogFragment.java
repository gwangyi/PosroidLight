package kr.gwangyi.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SimpleTextDialogFragment extends DialogFragment
{
	private String title, msg;
	
	public static SimpleTextDialogFragment newInstance(String title, String msg)
	{
		SimpleTextDialogFragment f = new SimpleTextDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", msg);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        title = getArguments().getString("title");
        msg = getArguments().getString("msg");
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
