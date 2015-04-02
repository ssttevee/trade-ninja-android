package com.ssttevee.tradeninja.app.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ssttevee.tradeninja.app.MainActivity;
import com.ssttevee.tradeninja.app.R;

public class TradeNinjaFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section
	 * number.
	 */
	public static TradeNinjaFragment newInstance(int sectionNumber) {
		TradeNinjaFragment fragment;

		switch(sectionNumber) {
			default:
			case 1:
				fragment = new HomeFragment();
				break;
			case 2:
				fragment = new BettingFragment();
				break;
			case 4:
				fragment = new ShopFragment();
				break;
			case 6:
				fragment = new HistoryFragment();
				break;
		}

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public TradeNinjaFragment() {}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

}