package com.ssttevee.tradeninja.app;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.ssttevee.tradeninja.app.fragments.TradeNinjaFragment;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private TradeNinjaFragment mTradeNinjaFragment;
    private CharSequence mTitle;
	private int mCurrentSection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
	    if(position == mCurrentSection) return;
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mTradeNinjaFragment = TradeNinjaFragment.newInstance(position + 1))
                .commit();
	    mCurrentSection = position;
    }

    public void onSectionAttached(int number) {
	    mTitle = getString(getResources().getIdentifier("title_section" + number, "string", "com.ssttevee.tradeninja.app"));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isDrawerOpen()) {
	        if(TradeNinja.signedIn) {
		        menu.add("Credits: " + TradeNinja.creditBalance).setEnabled(false);
		        menu.add("Sign Out");
	        } else {
		        menu.add("Sign In");
	        }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
	    if(item.getTitle() != null) {
		    if(item.getTitle().equals("Sign In")) {
			    TradeNinja.signIn(this, new Runnable() {
				    @Override
				    public void run() {
					    supportInvalidateOptionsMenu();
				    }
			    });
			    return true;
		    } else if(item.getTitle().equals("Sign Out")) {
			    TradeNinja.signOut(this);
			    return true;
		    }
	    }

        return mTradeNinjaFragment.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

	@Override
	protected void onResume() {
		super.onResume();
		supportInvalidateOptionsMenu();
	}

	public boolean isDrawerOpen() {
		return mNavigationDrawerFragment.isDrawerOpen();
	}

}
