package kr.gwangyi.posroid.light.fragments;

import java.net.URLEncoder;

import kr.gwangyi.posroid.light.R;
import kr.gwangyi.posroid.light.utilities.PasswordManager;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsletterFragment extends Fragment implements View.OnClickListener, View.OnKeyListener
{
	private static final int ANNOUNCEMENT = 79, NOTICE = 98;
	private int boardId;
	private WebView webview;
	
	private Handler handler = new Handler(Looper.getMainLooper());
	private Runnable goBack = new Runnable() {
		@Override
		public void run() {
			webview.goBack();
		}
	};
	
	private static String getBoardUrl(int boardId) {
		return "http://mpovis.postech.ac.kr/mpovis/logincheck.do?url="
			+ URLEncoder.encode(
				String.format("http://mbdap.postech.ac.kr/board/viewBoard.do?boardNo=%d", boardId));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		try
		{
			boardId = Integer.parseInt(pref.getString("newsletter_default_board", Integer.toString(ANNOUNCEMENT)));
		}
		catch(NumberFormatException e)
		{
			boardId = ANNOUNCEMENT;
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View ret = inflater.inflate(R.layout.newsletter, container, false);
		webview = (WebView)ret.findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if(url.contains("login.do?")) // 로그인 창이 뜨면 저장된 id/pw로 로그인 시도
				{
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
					String id = pref.getString("povis_id", ""), pass = pref.getString("povis_pwd", "");
					if(!id.equals("") && !pass.equals(""))
					{
						int pos = url.indexOf("url=", 0) + 4;
						url = url.substring(pos, url.indexOf("&", pos) == -1 ? url.length() : url.indexOf("&", pos));
						pass = PasswordManager.decrypt(id, pass);
						view.clearCache(false);
						view.postUrl("https://mpovis.postech.ac.kr/mpovis/login.do", ("j_username=" + URLEncoder.encode(id) + "&j_password=" + URLEncoder.encode(pass) + "&last_login_id_save=1&url=" + url).getBytes());
						return true;
					}
				}
				else if(!url.contains("/viewBoard.do") && !url.contains("/viewPost.do") && !url.contains("/logincheck.do"))// 게시판도, 로그인·로그아웃도 아니면 게시판 첫 화면으로 이동시켜준다
				{
					view.loadUrl(getBoardUrl(boardId));
					return true;
				}
				view.loadUrl(url);
				return true;
			}
		});
		ret.findViewById(R.id.announcement).setOnClickListener(this);
		ret.findViewById(R.id.notice).setOnClickListener(this);
		webview.loadUrl(getBoardUrl(boardId));
		webview.setOnKeyListener(this);
		return ret;
	}

	@Override
	public void onClick(View v)
	{
		int nextBoardId;
		switch(v.getId())
		{
		case R.id.announcement:
			nextBoardId = ANNOUNCEMENT;
			break;
		case R.id.notice:
			nextBoardId = NOTICE;
			break;
		default:
			return;
		}
		if(nextBoardId != boardId)
		{
			boardId = nextBoardId;
			webview.loadUrl(getBoardUrl(boardId));
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(webview.canGoBack())
			{
				handler.post(goBack);
				return true;
			}
		}
		return false;
	}
}
