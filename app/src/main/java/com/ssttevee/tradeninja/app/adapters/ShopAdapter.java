package com.ssttevee.tradeninja.app.adapters;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.helpers.Bet;
import com.telly.mrvector.MrVector;

import java.util.ArrayList;

public class ShopAdapter extends EndlessScrollAdapter<Bet> {

	public ShopAdapter(Activity activity) {
		super(activity);
	}

	@Override
	public View getDataRow(int position, View convertView, ViewGroup parent) {
		View v = convertView == null ? mActivity.getLayoutInflater().inflate(R.layout.listviewitem_bet, null) : convertView;
		Bet bet = getItem(position);

		return v;
	}

	public void clearBets() {
		dataList.clear();
		notifyDataSetInvalidated();
	}

	public void setBets(ArrayList<Bet> bets) {
		dataList = bets;
		notifyDataSetInvalidated();
	}

	public void addBets(ArrayList<Bet> bets) {
		dataList.addAll(bets);
		notifyDataSetChanged();
	}
}
