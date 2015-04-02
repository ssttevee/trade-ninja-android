package com.ssttevee.tradeninja.app.helpers;

public class Team {
	public int id;
	public int credits;
	public String longName;
	public String shortName;
	public String urlSlug;
	public int score;

	public Team(String name, int credits) {
		this.id = -1;
		this.longName = name;
		this.shortName = name;
		this.urlSlug = name.toLowerCase().replaceAll(" ", "-").replace(".","-");
		this.credits = credits;
	}

	public Team(int id, String longName, String shortName, String urlSlug, int credits) {
		this.id = id;
		this.longName = longName;
		this.shortName = shortName;
		this.urlSlug = urlSlug;
		this.credits = credits;
	}
}