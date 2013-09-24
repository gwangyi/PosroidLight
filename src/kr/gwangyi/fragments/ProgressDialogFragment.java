package kr.gwangyi.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment
{
	private String title, msg;
	private Dialog.OnCancelListener onCancel = null;
	
	public static ProgressDialogFragment newInstance(String title, String msg)
	{
		ProgressDialogFragment f = new ProgressDialogFragment();

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
        return ProgressDialog.show(this.getActivity(), title, msg, true);
    }
    
    @Override
    public void onCancel(DialogInterface dialog) {
    	if(onCancel != null) onCancel.onCancel(dialog);
    	super.onCancel(dialog);
    }
    
    public void setOnCancelListener(Dialog.OnCancelListener listener)
    {
    	this.onCancel = listener;
    }
}
