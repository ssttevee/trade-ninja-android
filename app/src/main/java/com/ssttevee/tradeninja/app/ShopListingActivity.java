package com.ssttevee.tradeninja.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.ssttevee.tradeninja.app.helpers.ShopItem;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class ShopListingActivity extends ActionBarActivity implements SwipeBackActivityBase {
	public static String ARG_LISTINGS_URL = "LISTINGS_URL";

	private SwipeRefreshLayout mRootView;
	private SwipeBackActivityHelper mHelper;
	private SwipeBackLayout mSwipeBackLayout;
	private ShopItem shopItem;
	private String listingsUrl;

	private String side = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SwipeBackActivityHelper(this);
		mHelper.onActivityCreate();

		mRootView = (SwipeRefreshLayout) getLayoutInflater().inflate(R.layout.activity_match, null);
		mRootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshListings();
			}
		});
		setContentView(mRootView);

		if (savedInstanceState == null) {
			if(getIntent().getExtras() == null) {
				Toast.makeText(this, "Missing Listing URL...", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				listingsUrl = getIntent().getExtras().getString(ARG_LISTINGS_URL);
			}
		} else {
			listingsUrl = savedInstanceState.getString(ARG_LISTINGS_URL);
		}

		mRootView.setEnabled(false);
		getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		getSwipeBackLayout().setEdgeSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics()));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate();
	}

	@Override
	public void onStart() {
		super.onStart();

		mRootView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		refreshListings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Credits: " + TradeNinja.creditBalance).setEnabled(false).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v != null)
			return v;
		return mHelper.findViewById(id);
	}

	@Override
	public void onBackPressed() {
		scrollToFinishActivity();
	}
	
	private void refreshListings() {
		mRootView.setRefreshing(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				shopItem = TradeNinja.getShopItemListings(listingsUrl);
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						updateLayout();
					}
				});
			}
		}).start();
	}

	private void updateLayout() {
		invalidateOptionsMenu();

		mRootView.setRefreshing(false);
	}

	@Override
	public SwipeBackLayout getSwipeBackLayout() {
		return mHelper.getSwipeBackLayout();
	}

	@Override
	public void setSwipeBackEnable(boolean enable) {
		getSwipeBackLayout().setEnableGesture(enable);
	}

	@Override
	public void scrollToFinishActivity() {
		getSwipeBackLayout().scrollToFinishActivity();
	}
}
