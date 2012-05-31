package kr.gwangyi.activities;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;

public abstract class FragmentTabActivity extends FragmentActivity
{
	protected class TabManager implements TabHost.OnTabChangeListener
	{
		private final class TabInfo
		{
            private final String tag;
            private final Class<?> klass;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String tag, Class<?> klass, Bundle args) {
                this.tag = tag;
                this.klass = klass;
                this.args = args;
            }
        }
		
		private class DummyTabFactory implements TabHost.TabContentFactory
		{
            @Override
            public View createTabContent(String tag) {
                View v = new View(FragmentTabActivity.this);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }
		
		private final HashMap<String, TabInfo> tabs = new HashMap<String, TabInfo>();
        private TabInfo lastTab;
		
		private TabManager()
		{
			tabHost.setOnTabChangedListener(this);
		}
		
		public void addTab(TabHost.TabSpec tabSpec, Class<?> klass, Bundle args)
		{
			tabSpec.setContent(new DummyTabFactory());
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, klass, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            tabs.put(tag, info);
            tabHost.addTab(tabSpec);
		}

		@Override
		public void onTabChanged(String tabId)
		{
			TabInfo newTab = tabs.get(tabId);
			if (lastTab != newTab)
			{
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				if (lastTab != null && lastTab.fragment != null)
					ft.detach(lastTab.fragment);
				if (newTab != null)
				{
					if (newTab.fragment == null)
					{
						newTab.fragment = Fragment.instantiate(FragmentTabActivity.this,
								newTab.klass.getName(), newTab.args);
						ft.add(android.R.id.tabcontent, newTab.fragment, newTab.tag);
					}
					else
						ft.attach(newTab.fragment);
				}

				lastTab = newTab;
				ft.commit();
				getSupportFragmentManager().executePendingTransactions();
			}
			FragmentTabActivity.this.onTabChanged(tabId);
		}
	}
	
	private TabHost tabHost;
	private TabManager tabManager;

	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(layoutResID);
		init();
	}
	
	@Override
	public void setContentView(View view)
	{
		super.setContentView(view);
		init();
	}
	
	@Override
	public void setContentView(View view, LayoutParams params)
	{
		super.setContentView(view, params);
		init();
	}
	
	private void init()
	{
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();
		tabManager = new TabManager();
	}
	
	protected TabHost getTabHost() { return tabHost; }
	protected TabManager getTabManager() { return tabManager; }
	
	protected void onTabChanged(String tabId)
	{
	}
}
