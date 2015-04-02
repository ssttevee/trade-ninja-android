package com.ssttevee.tradeninja.app.helpers;

import android.util.TypedValue;
import com.ssttevee.tradeninja.app.TradeNinja;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Bet {
	public int credits;
	public Match match;
	public int team;

	public Bet(int credits, Match match, int team) {
		this.credits = credits;
		this.match = match;
		this.team = team;
	}

	public int getPotentialReward() {
		return TradeNinja.calculatePotentialReward(credits, (team == 1 ? match.teams.a.credits : match.teams.b.credits), (team == 2 ? match.teams.a.credits : match.teams.b.credits));
	}

	public static ArrayList<Bet> fromJSON(JSONObject response) {
		ArrayList<Bet> bets = new ArrayList<>();

		try {
			JSONArray jsonBets = response.getJSONObject("results").getJSONArray("bets");

			for(int i = 0; i < jsonBets.length(); i++) {
				JSONObject bet = jsonBets.getJSONObject(i);
				JSONObject match = bet.getJSONObject("match");
				JSONObject teamA = match.getJSONObject("teams").getJSONObject("a");
				JSONObject teamB = match.getJSONObject("teams").getJSONObject("b");
				bets.add(
						new Bet(
								bet.getInt("credits"),
								new Match(
										match.getInt("id"),
										new Teams(
												new Team(
														teamA.getInt("id"),
														teamA.getString("long_name"),
														teamA.getString("short_name"),
														teamA.getString("url_slug"),
														teamA.getInt("credits")
												),
												new Team(
														teamB.getInt("id"),
														teamB.getString("long_name"),
														teamB.getString("short_name"),
														teamB.getString("url_slug"),
														teamB.getInt("credits")
												)
										),
										match.getInt("winner"),
										match.getString("host"),
										0,
										""
								),
								bet.getInt("team")
						)
				);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}

		return bets;
	}
}
