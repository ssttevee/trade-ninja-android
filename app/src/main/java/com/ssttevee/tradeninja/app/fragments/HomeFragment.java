package com.ssttevee.tradeninja.app.fragments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.*;
import android.widget.ListView;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.adapters.NewsAdapter;

import java.util.HashMap;

public class HomeFragment extends TradeNinjaFragment {
	private SwipeRefreshLayout mRootView;
	private static NewsAdapter mNewsAdapter;

	public HomeFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ListView listView = new ListView(getActivity());
		listView.setPadding(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
		listView.setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		listView.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
		listView.addHeaderView(new View(getActivity()));
		listView.addFooterView(new View(getActivity()));
		listView.setAdapter(mNewsAdapter);

		mRootView = new SwipeRefreshLayout(getActivity());
		mRootView.setBackgroundColor(getResources().getColor(R.color.background_material_dark));
		mRootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateNews();
			}
		});

		mRootView.addView(listView);
		return mRootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(mNewsAdapter == null) mNewsAdapter = new NewsAdapter(getActivity(), new HashMap[0]);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onStart() {
		super.onStart();

		mRootView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		if(mNewsAdapter.getCount() < 1) updateNews();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void updateNews() {
		new Thread(new Runnable() {
			public void run() {
				final HashMap<String,String>[] articles = TradeNinja.getNews();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						mNewsAdapter.setArticles(articles);
						mRootView.setRefreshing(false);
					}
				});
			}
		}).start();
	}

}
