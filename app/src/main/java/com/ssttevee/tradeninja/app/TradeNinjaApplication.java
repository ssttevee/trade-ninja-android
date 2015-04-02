package com.ssttevee.tradeninja.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.ssttevee.tradeninja.app.helpers.Game;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TradeNinjaApplication extends Application {

	private static Context context;
	private static SharedPreferences.Editor editor;

	private Set<String> prefGames;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();

		SharedPreferences settings = getSharedPreferences("NinjaSession", 0);
		editor = settings.edit();
		TradeNinja.restoreSession(settings.getInt("userId", 0), settings.getString("sessionKey", null));
		prefGames = settings.getStringSet("pref_games", new HashSet<>(Arrays.asList(new String[]{"csgo", "steam"})));
	}

	public static InputStream loadCertAsInputStream() {
		return context.getResources().openRawResource(R.raw.trade_ninja_cert);
	}

	public void setPrefGames(List<Game> games) {
		Set<String> prefGames = new HashSet<>();
		for(Game game : games) {
			prefGames.add(game.toString());
		}
		editor.putStringSet("pref_games", this.prefGames = prefGames);
		editor.commit();
	}

	public Set<String> getPrefGames() {
		return this.prefGames;
	}

	public static void setSession(String key) {
		editor.putString("sessionKey", key);
		editor.commit();
	}

	public static void setUserId(int userId) {
		editor.putInt("userId", userId);
		editor.commit();
	}

	public static void clearSession() {
		editor.remove("userId");
		editor.remove("sessionKey");
		editor.commit();
	}

}
