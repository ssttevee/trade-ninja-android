package com.ssttevee.tradeninja.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.ssttevee.tradeninja.app.helpers.Match;
import com.telly.mrvector.MrVector;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class MatchActivity extends ActionBarActivity implements SwipeBackActivityBase {
	public static String ARG_MATCH_ID = "MATCH_ID";

	private SwipeRefreshLayout mRootView;
	private SwipeBackActivityHelper mHelper;
	private SwipeBackLayout mSwipeBackLayout;
	private Match match;
	private int matchId;

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
				refreshMatch();
			}
		});
		setContentView(mRootView);

		if (savedInstanceState == null) {
			if(getIntent().getExtras() == null) {
				Toast.makeText(this, "Missing Match ID...", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				matchId = getIntent().getExtras().getInt(ARG_MATCH_ID);
			}
		} else {
			matchId = savedInstanceState.getInt(ARG_MATCH_ID);
		}

		((EditText) mRootView.findViewById(R.id.match_info_place_bet_credits)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				calculatePotentialReward();
			}
		});
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
		refreshMatch();
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

	public void goToStream(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(match.url));
		startActivity(browserIntent);
	}

	public void onRadioButtonClicked(View v) {
		calculatePotentialReward();
	}

	public void submitPlaceBet(View v) {
		final EditText et = (EditText) mRootView.findViewById(R.id.match_info_place_bet_credits);
		final RadioButton rb1 = (RadioButton) mRootView.findViewById(R.id.match_info_place_bet_team_1);
		final CheckBox cb = (CheckBox) mRootView.findViewById(R.id.match_info_place_bet_rules);

		if(!cb.isChecked()) {
			Toast.makeText(this, "You must agree with the rules.", Toast.LENGTH_SHORT).show();
			return;
		}

		mRootView.setRefreshing(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				match = TradeNinja.placeBet(matchId, rb1.isChecked() ? "a" : "b", Integer.parseInt(et.getText().toString()), cb.isChecked());
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						updateLayout();
					}
				});
			}
		}).start();
	}

	public void removeBet(View v) {
		mRootView.setRefreshing(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				match = TradeNinja.removeBet(matchId);
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						updateLayout();
					}
				});
			}
		}).start();
	}

	private void calculatePotentialReward() {
		EditText et = (EditText) mRootView.findViewById(R.id.match_info_place_bet_credits);
		RadioButton rb1 = (RadioButton) mRootView.findViewById(R.id.match_info_place_bet_team_1);
		RadioButton rb2 = (RadioButton) mRootView.findViewById(R.id.match_info_place_bet_team_2);
		TextView tv = (TextView) mRootView.findViewById(R.id.match_info_place_bet_potential_reward);
		Button btn = (Button) mRootView.findViewById(R.id.match_info_place_bet_submit);

		try {
			int pr = TradeNinja.calculatePotentialReward(Integer.parseInt(et.getText().toString()), rb1.isChecked() ? match.teams.a.credits : match.teams.b.credits, rb2.isChecked() ? match.teams.a.credits : match.teams.b.credits);

			if(!rb1.isChecked() && !rb2.isChecked() || pr == 0) {
				tv.setText("Potential Reward: --");
				btn.setEnabled(false);
			} else {
				tv.setText("Potential Reward: " + pr + " credits + inital bet");
				btn.setEnabled(true);
			}
		} catch(NumberFormatException e) {
			tv.setText("Potential Reward: --");
			btn.setEnabled(false);
		}
	}
	
	private void refreshMatch() {
		mRootView.setRefreshing(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				match = TradeNinja.getMatch(matchId);
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
		System.out.println("Credits: " + TradeNinja.creditBalance);
		getSupportActionBar().setTitle(match.teams.a.longName + " vs " + match.teams.b.longName);

		((TextView) mRootView.findViewById(R.id.match_info_team_1_name)).setText(match.teams.a.longName);
		((TextView) mRootView.findViewById(R.id.match_info_team_2_name)).setText(match.teams.b.longName);

		((TextView) mRootView.findViewById(R.id.match_info_place_bet_team_1)).setText(match.teams.a.longName);
		((TextView) mRootView.findViewById(R.id.match_info_place_bet_team_2)).setText(match.teams.b.longName);

		((TextView) mRootView.findViewById(R.id.match_info_time)).setText(new PrettyTime().format(new Date(match.time)));
		((TextView) mRootView.findViewById(R.id.match_info_host)).setText("Hosted by " + match.host);

		((ImageView) mRootView.findViewById(R.id.match_info_team_1_logo)).setImageDrawable(MrVector.inflate(getResources(), R.drawable.vector_question_circle));
		((ImageView) mRootView.findViewById(R.id.match_info_team_2_logo)).setImageDrawable(MrVector.inflate(getResources(), R.drawable.vector_question_circle));

		TradeNinja.setTeamLogo(this, match.teams.a.urlSlug, (ImageView) mRootView.findViewById(R.id.match_info_team_1_logo));
		TradeNinja.setTeamLogo(this, match.teams.b.urlSlug, (ImageView) mRootView.findViewById(R.id.match_info_team_2_logo));

		mRootView.findViewById(R.id.match_info_team_1_container).setAlpha(1);
		mRootView.findViewById(R.id.match_info_team_2_container).setAlpha(1);

		if(match.winner == 2 || match.winner == 3) mRootView.findViewById(R.id.match_info_team_1_container).setAlpha(0.1F);
		if(match.winner == 1 || match.winner == 3) mRootView.findViewById(R.id.match_info_team_2_container).setAlpha(0.1F);

		if(match.url != null) mRootView.findViewById(R.id.match_info_stream_button).setVisibility(View.VISIBLE);
		else mRootView.findViewById(R.id.match_info_stream_button).setVisibility(View.GONE);

		String[][] stats = new String[][]{new String[]{"Total Credits", (match.teams.a.credits + match.teams.b.credits) + "",}, new String[]{match.teams.a.longName, match.getOddsA() + "%",}, new String[]{match.teams.b.longName, match.getOddsB() + "%",},};

		((ViewGroup) mRootView.findViewById(R.id.match_info_misc)).removeAllViews();
		for(String[] stat : stats) {
			View v = getLayoutInflater().inflate(R.layout.part_match_label_value, null);
			((TextView) v.findViewById(android.R.id.text1)).setText(stat[0]);
			((TextView) v.findViewById(android.R.id.text2)).setText(stat[1]);
			((ViewGroup) mRootView.findViewById(R.id.match_info_misc)).addView(v);
		}

		if(TradeNinja.signedIn) {
			if(match.bet == null) {
				if(System.currentTimeMillis() < match.time - 1000 * 60 * 10) {
					mRootView.findViewById(R.id.match_info_place_bet).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.match_info_your_bet).setVisibility(View.GONE);
				} else {
					mRootView.findViewById(R.id.match_info_place_bet).setVisibility(View.GONE);
					mRootView.findViewById(R.id.match_info_your_bet).setVisibility(View.VISIBLE);

					((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).removeAllViews();
					TextView title = new TextView(this);
					title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
					title.setGravity(Gravity.CENTER);
					title.setText("You didn't place a bet.");
					((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).addView(title);
				}
			} else {
				mRootView.findViewById(R.id.match_info_place_bet).setVisibility(View.GONE);
				mRootView.findViewById(R.id.match_info_your_bet).setVisibility(View.VISIBLE);

				String[][] betInfo = new String[][]{new String[]{"Team", match.bet.team == 1 ? match.teams.a.longName : match.teams.b.longName,}, new String[]{"Amount", match.bet.credits + " credits",}, new String[]{"Potential Reward", match.bet.getPotentialReward() + " credits",},};

				((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).removeAllViews();
				TextView title = new TextView(this);
				title.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				title.setTypeface(null, Typeface.BOLD);
				title.setText("Your Bet");
				((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).addView(title);
				for(String[] info : betInfo) {
					View v = getLayoutInflater().inflate(R.layout.part_match_label_value, null);
					((TextView) v.findViewById(android.R.id.text1)).setText(info[0]);
					((TextView) v.findViewById(android.R.id.text2)).setText(info[1]);
					((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).addView(v);
				}
				if(System.currentTimeMillis() < match.time - 1000 * 60 * 10 || match.bet.credits < 5000) {
					Button btn = new Button(this);
					btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
					((LinearLayout.LayoutParams) btn.getLayoutParams()).setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()), 0, 0);
					btn.setBackgroundColor(getResources().getColor(R.color.trade_ninja_button_green));
					btn.setText("Remove Bet");
					btn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeBet(v);
						}
					});
					((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).addView(btn);
				}
			}
		} else if(System.currentTimeMillis() < match.time - 1000 * 60 * 10) {
			mRootView.findViewById(R.id.match_info_place_bet).setVisibility(View.GONE);
			mRootView.findViewById(R.id.match_info_your_bet).setVisibility(View.VISIBLE);

			((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).removeAllViews();
			TextView title = new TextView(this);
			title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			title.setGravity(Gravity.CENTER);
			title.setText("Sign in to place a bet.");
			title.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TradeNinja.signIn(MatchActivity.this, new Runnable() {
						@Override
						public void run() {
							MatchActivity.this.refreshMatch();
						}
					});
				}
			});
			((ViewGroup) mRootView.findViewById(R.id.match_info_your_bet)).addView(title);
		}

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
