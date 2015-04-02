package com.ssttevee.tradeninja.app.fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ssttevee.tradeninja.app.MatchActivity;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.TradeNinjaApplication;
import com.ssttevee.tradeninja.app.adapters.ShopItemsAdapter;
import com.ssttevee.tradeninja.app.helpers.Game;
import com.ssttevee.tradeninja.app.helpers.ShopItem;
import com.ssttevee.tradeninja.app.listeners.EndlessScrollListener;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends TradeNinjaFragment {

	private SwipeRefreshLayout mRootView;
	private ShopItemsAdapter mShopItemsAdapter;

	private List<Game> games;

	public ShopFragment() {
		games = new ArrayList<>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ListView listView = new ListView(getActivity());
		listView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
		listView.setClipToPadding(false);
		listView.setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		listView.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
		listView.setAdapter(mShopItemsAdapter = new ShopItemsAdapter(getActivity()));
		listView.setOnScrollListener(new EndlessScrollListener(5, 0) {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				getShopItems(page);
			}
		});
		/*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getActivity(), MatchActivity.class);
				intent.putExtra(MatchActivity.ARG_MATCH_ID, mShopItemsAdapter.getItem((int) id).getListingUrl());
				startActivity(intent);
			}
		});*/

		mRootView = new SwipeRefreshLayout(getActivity());
		mRootView.setBackgroundColor(getResources().getColor(R.color.background_material_dark));
		mRootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mShopItemsAdapter.clearShopItems();
				getShopItems(1);
			}
		});

		mRootView.addView(listView);
		return mRootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		for(String game : ((TradeNinjaApplication) getActivity().getApplication()).getPrefGames())
			games.add(Game.fromShortName(game));

		mRootView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		getShopItems(1);
	}

	private void getShopItems(final int page) {
		if(page == 1) mRootView.setRefreshing(true);
		new Thread(new Runnable() {
			public void run() {
				final List<ShopItem> shopItems = TradeNinja.getShopItems("", games, "price", page);

				mShopItemsAdapter.setServerListSize((page - (shopItems.size() < 10 ? 1 : 0)) * 10 + shopItems.size());

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						if(page == 1) {
							mShopItemsAdapter.setShopItems(shopItems);
						} else {
							mShopItemsAdapter.addShopItems(shopItems);
						}
						mRootView.setRefreshing(false);
					}
				});
			}
		}).start();
	}
}
