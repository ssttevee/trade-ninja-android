package com.ssttevee.tradeninja.app.adapters;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.helpers.Match;
import com.telly.mrvector.MrVector;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;

public class MatchesAdapter extends BaseAdapter {

	private Activity mActivity;
	private Match[] matches;

	public MatchesAdapter(Activity activity, Match[] matches) {
		this.mActivity = activity;
		this.matches = matches;
	}

	@Override
	public int getCount() {
		return matches.length;
	}

	@Override
	public Match getItem(int position) {
		return matches[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView == null ? mActivity.getLayoutInflater().inflate(R.layout.listviewitem_match, null) : convertView;

		((TextView) v.findViewById(R.id.match_info_team_1_name)).setText(getItem(position).teams.a.longName);
		((TextView) v.findViewById(R.id.match_info_team_2_name)).setText(getItem(position).teams.b.longName);

		((TextView) v.findViewById(R.id.match_info_team_1_chance)).setText(getItem(position).getOddsA() + "%");
		((TextView) v.findViewById(R.id.match_info_team_2_chance)).setText(getItem(position).getOddsB() + "%");

		((TextView) v.findViewById(R.id.match_info_time)).setText(new PrettyTime().format(new Date(getItem(position).time)));
		((TextView) v.findViewById(R.id.match_info_host)).setText("Hosted by " + getItem(position).host);

		((ImageView) v.findViewById(R.id.match_info_team_1_logo)).setImageDrawable(MrVector.inflate(mActivity.getResources(), R.drawable.vector_question_circle));
		((ImageView) v.findViewById(R.id.match_info_team_2_logo)).setImageDrawable(MrVector.inflate(mActivity.getResources(), R.drawable.vector_question_circle));

		TradeNinja.setTeamLogo(mActivity, getItem(position).teams.a.urlSlug, (ImageView) v.findViewById(R.id.match_info_team_1_logo));
		TradeNinja.setTeamLogo(mActivity, getItem(position).teams.b.urlSlug, (ImageView) v.findViewById(R.id.match_info_team_2_logo));

		v.findViewById(R.id.match_info_team_1_container).setAlpha(1);
		v.findViewById(R.id.match_info_team_2_container).setAlpha(1);

		if(getItem(position).winner == 2 || getItem(position).winner == 3) v.findViewById(R.id.match_info_team_1_container).setAlpha(0.1F);
		if(getItem(position).winner == 1 || getItem(position).winner == 3) v.findViewById(R.id.match_info_team_2_container).setAlpha(0.1F);

		return v;
	}

	public void setMatches(Match[] matches) {
		this.matches = matches;
		notifyDataSetChanged();
	}
}
