package com.ssttevee.tradeninja.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ssttevee.tradeninja.app.*;
import com.ssttevee.tradeninja.app.adapters.MatchesAdapter;
import com.ssttevee.tradeninja.app.helpers.Match;
import com.telly.mrvector.MrVector;

public class BettingFragment extends TradeNinjaFragment {
	private SwipeRefreshLayout mRootView;
	private MatchesPagerAdapter mMatchesPagerAdapter;

	public BettingFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_betting, null);
		mRootView.setEnabled(false);
		mRootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateMatches();
			}
		});

		((ViewPager) mRootView.findViewById(R.id.view_pager)).setAdapter(mMatchesPagerAdapter = new MatchesPagerAdapter(getActivity()));

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
		updateMatches();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(!((MainActivity) getActivity()).isDrawerOpen()) {
			menu.add("Refresh")
					.setIcon(MrVector.inflate(getResources(), R.drawable.vector_refresh))
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle() != null) {
			if(item.getTitle().equals("Refresh")) {
				updateMatches();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateMatches() {
		mRootView.setRefreshing(true);
		new Thread(new Runnable() {
			public void run() {
				final Match[][] matches = TradeNinja.getMatches();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						mMatchesPagerAdapter.setMatches(matches);
						mRootView.setRefreshing(false);
					}
				});
			}
		}).start();
	}

	public class MatchesPagerAdapter extends PagerAdapter {
		private SparseArray<ListView> matchesViews = new SparseArray<>(3);
		private String[] categories = new String[] {"Live", "Upcoming", "Played"};

		public MatchesPagerAdapter(Activity activity) {
			for(int i = 0; i < 3; i++) {
				ListView lv = new ListView(activity);
				lv.setDivider(getResources().getDrawable(android.R.color.transparent));
				lv.setDividerHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics()));
				lv.setAdapter(new MatchesAdapter(activity, new Match[0]));
				lv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Intent intent = new Intent(getActivity(), MatchActivity.class);
						intent.putExtra(MatchActivity.ARG_MATCH_ID, ((MatchesAdapter) parent.getAdapter()).getItem((int) id).id);
						startActivity(intent);
					}
				});
				matchesViews.put(i, lv);
			}
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ListView lv = matchesViews.get(position);
			container.addView(lv);
			return lv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((ListView) object);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return categories[position];
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		public void setMatches(Match[][] matches) {
			for(int i = 0; i < matches.length; i++) {
				if(matchesViews.get(i) == null) return;
				((MatchesAdapter) matchesViews.get(i).getAdapter()).setMatches(matches[i]);
			}
		}
	}

}
