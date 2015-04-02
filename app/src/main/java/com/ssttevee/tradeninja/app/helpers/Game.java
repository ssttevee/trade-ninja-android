package com.ssttevee.tradeninja.app.helpers;

public enum Game {
	CSGO("Counter-Strike: Global Offensive"),
	TF2("Team Fortress 2"),
	DOTA2("Dota 2"),
	STEAM("Steam");

	String longName;

	Game(String longName) {
		this.longName = longName;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public String getLongName() {
		return longName;
	}

	public static Game fromLongName(String longName) {
		for (Game game : Game.values()) {
			if(game.getLongName().equals(longName)) {
				return game;
			}
		}
		return null;
	}

	public static Game fromShortName(String shortName) {
		for (Game game : Game.values()) {
			if(game.toString().equals(shortName)) {
				return game;
			}
		}
		return null;
	}
}
