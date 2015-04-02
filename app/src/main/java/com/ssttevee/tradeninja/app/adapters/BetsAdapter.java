package com.ssttevee.tradeninja.app.adapters;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.helpers.Bet;
import com.telly.mrvector.MrVector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class BetsAdapter extends EndlessScrollAdapter<Bet> {

	public BetsAdapter(Activity activity) {
		super(activity);
	}

	@Override
	public View getDataRow(int position, View convertView, ViewGroup parent) {
		View v = convertView == null ? mActivity.getLayoutInflater().inflate(R.layout.listviewitem_bet, null) : convertView;
		Bet bet = getItem(position);

		if(bet.match.winner == 0) v.setBackgroundResource(R.drawable.trade_ninja_bets_gray);
		else if(bet.match.winner == 3) v.setBackgroundResource(R.drawable.trade_ninja_bets_orange);
		else if(bet.match.winner == bet.team) v.setBackgroundResource(R.drawable.trade_ninja_bets_green);
		else v.setBackgroundResource(R.drawable.trade_ninja_bets_red);

		((TextView) v.findViewById(R.id.bet_info_team_1_name)).setText(Html.fromHtml(((bet.team == 1 ? "<u>" : "") + bet.match.teams.a.shortName + (bet.team == 1 ? "</u>" : ""))));
		((TextView) v.findViewById(R.id.bet_info_team_2_name)).setText(Html.fromHtml(((bet.team == 2 ? "<u>" : "") + bet.match.teams.b.shortName + (bet.team == 2 ? "</u>" : ""))));

		((TextView) v.findViewById(R.id.bet_info_team_1_chance)).setText("" + bet.match.getOddsA() + "%");
		((TextView) v.findViewById(R.id.bet_info_team_2_chance)).setText("" + bet.match.getOddsB() + "%");

		((TextView) v.findViewById(R.id.bet_info_amount)).setText("Bet: " + bet.credits);
		((TextView) v.findViewById(R.id.bet_info_reward)).setText("[PR:" + bet.getPotentialReward() + "]");

		((ImageView) v.findViewById(R.id.bet_info_team_1_icon)).setImageDrawable(MrVector.inflate(mActivity.getResources(), R.drawable.vector_question_circle));
		((ImageView) v.findViewById(R.id.bet_info_team_2_icon)).setImageDrawable(MrVector.inflate(mActivity.getResources(), R.drawable.vector_question_circle));

		TradeNinja.setTeamLogo(mActivity, bet.match.teams.a.urlSlug, (ImageView) v.findViewById(R.id.bet_info_team_1_icon));
		TradeNinja.setTeamLogo(mActivity, bet.match.teams.b.urlSlug, (ImageView) v.findViewById(R.id.bet_info_team_2_icon));

		v.findViewById(R.id.bet_info_team_1_container).setAlpha(1F);
		v.findViewById(R.id.bet_info_team_2_container).setAlpha(1F);

		if(bet.match.winner == 2 || bet.match.winner == 3) v.findViewById(R.id.bet_info_team_1_container).setAlpha(0.1F);
		if(bet.match.winner == 1 || bet.match.winner == 3) v.findViewById(R.id.bet_info_team_2_container).setAlpha(0.1F);

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
