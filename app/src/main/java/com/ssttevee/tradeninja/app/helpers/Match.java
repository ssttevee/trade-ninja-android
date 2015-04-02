package com.ssttevee.tradeninja.app.helpers;

public class Match {
	public int id;
	public String host;
	public int winner;
	public Teams teams;
	public long time;
	public String extra;
	public String url;
	public Bet bet;

	public Match(String team1, int t1percent, String team2, int t2percent, String time, String host, String extra, int winner) {
		this.id = -1;
		this.host = host.replace("Hosted by ", "");
		this.winner = winner;
		this.teams = new Teams(new Team(team1, t1percent), new Team(team2, t2percent));
		this.time = prettyTimeToMillis(time.replace("LIVE since ", "").replace("LIVE in ", ""));
		this.extra = extra;
	}

	public Match(int id, Teams teams, int winner, String host, long time, String extra) {
		this.id = id;
		this.host = host.replace("Hosted by ", "");
		this.winner = winner;
		this.teams = teams;
		this.time = time;
		this.extra = extra;
	}

	public Match(int id, Teams teams, int winner, String host, String time, String extra) {
		this(id, teams, winner, host, prettyTimeToMillis(time.replace("LIVE since ", "").replace("LIVE in ", "")), extra);
	}

	public int getOddsA() {
		return (int) Math.round(((double) teams.a.credits / (double) (teams.a.credits + teams.b.credits)) * 100D);
	}

	public int getOddsB() {
		return (int) Math.round(((double) teams.b.credits / (double) (teams.a.credits + teams.b.credits)) * 100D);
	}

	private static long prettyTimeToMillis(String prettytime) {
		long milliseconds = System.currentTimeMillis();
		long multiplier = 1;
		int timeDiff;
		String timeUnit = " ";

		if(prettytime.contains("ago")) {
			multiplier *= -1;
			prettytime = prettytime.replace(" ago", "");
		}

		if(prettytime.contains("sec")) {
			multiplier *= 1000;
			timeUnit += "sec";
		} else if(prettytime.contains("min")) {
			multiplier *= 1000*60;
			timeUnit += "min";
		} else if(prettytime.contains("hour")) {
			multiplier *= 1000*60*60;
			timeUnit += "hour";
		} else if(prettytime.contains("day")) {
			multiplier *= 1000*60*60*24;
			timeUnit += "day";
		} else if(prettytime.contains("week")) {
			multiplier *= 1000*60*60*24*7;
			timeUnit += "week";
		} else if(prettytime.contains("month")) {
			multiplier *= 1000*60*60*24*7*30;
			timeUnit += "month";
		} else if(prettytime.contains("year")) {
			multiplier *= 1000*60*60*24*7*365;
			timeUnit += "year";
		}

		int index = prettytime.indexOf(timeUnit);
		prettytime = prettytime.substring(0, index);
		timeDiff = Integer.parseInt(prettytime);
		milliseconds += multiplier*timeDiff;

		return milliseconds;
	}
}