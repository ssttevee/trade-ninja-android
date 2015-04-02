package com.ssttevee.tradeninja.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.ssttevee.tradeninja.app.MatchActivity;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.adapters.BetsAdapter;
import com.ssttevee.tradeninja.app.helpers.Bet;
import com.ssttevee.tradeninja.app.listeners.EndlessScrollListener;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryFragment extends TradeNinjaFragment {
	private SwipeRefreshLayout mRootView;
	private BetsAdapter mBetsAdapter;
	private int pages = 1;
	private int page = 0;
	private boolean loadingMore = false;

	public HistoryFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ListView listView = new ListView(getActivity());
		listView.setPadding(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
		listView.setClipToPadding(false);
		listView.setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		listView.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
		listView.setAdapter(mBetsAdapter = new BetsAdapter(getActivity()));
		listView.setOnScrollListener(new EndlessScrollListener(5, 0) {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if(page <= pages)
					getHistory(true);
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getActivity(), MatchActivity.class);
				intent.putExtra(MatchActivity.ARG_MATCH_ID, mBetsAdapter.getItem((int) id).match.id);
				startActivity(intent);
			}
		});

		mRootView = new SwipeRefreshLayout(getActivity());
		mRootView.setBackgroundColor(getResources().getColor(R.color.background_material_dark));
		mRootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mBetsAdapter.clearBets();
				getHistory(false);
			}
		});

		mRootView.addView(listView);
		return mRootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onStart() {
		super.onStart();

		mRootView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		getHistory(false);
	}

	private void getHistory(final boolean nextPage) {
		if(!nextPage) mRootView.setRefreshing(true);
		new Thread(new Runnable() {
			public void run() {
				final JSONObject response;
				if(nextPage && page < pages) response = TradeNinja.getHistory(page + 1);
				else response = TradeNinja.getHistory(page = 1);

				try {
					if(!response.getBoolean("success") && response.getString("status_message").equals("Not signed in.")) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							public void run() {
								TradeNinja.signIn(getActivity(), new Runnable() {
									@Override
									public void run() {
										getHistory(false);
									}
								});
							}
						});
					}

					pages = response.getJSONObject("results").getInt("num_pages");
					page = response.getJSONObject("results").getInt("page");

					mBetsAdapter.setServerListSize(pages * 30);
					if(page == pages) mBetsAdapter.setServerListSize((pages - 1) * 30 + response.getJSONObject("results").getJSONArray("bets").length());
				} catch(JSONException e) {
					e.printStackTrace();
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						if(nextPage) {
							loadingMore = false;
							mBetsAdapter.addBets(Bet.fromJSON(response));
						} else {
							mBetsAdapter.setBets(Bet.fromJSON(response));
						}
						mRootView.setRefreshing(false);
					}
				});
			}
		}).start();
	}

}
