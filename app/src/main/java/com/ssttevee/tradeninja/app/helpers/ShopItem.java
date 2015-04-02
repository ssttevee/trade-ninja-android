package com.ssttevee.tradeninja.app.helpers;

import android.net.Uri;
import android.util.SparseArray;

import java.util.regex.Pattern;

public class ShopItem {
	public String name;
	public boolean statTrak;
	public int price;
	public Game game;
	public String thumbnail;
	public int currentStock;
	public int maxStock;
	public SparseArray<String> listings;

	public ShopItem(String name, boolean statTrak, int price, Game game, String thumbnail) {
		this.name = name.replace("StatTrak™ ", "");
		this.statTrak = statTrak;
		this.price = price;
		this.game = game;
		this.thumbnail = thumbnail.split("/")[5];
	}

	public ShopItem(String name, int price, Game game, String thumbnail) {
		this(name, name.contains("StatTrak"), price, game, thumbnail);
	}

	public ShopItem(String name, int price, Game game, String thumbnail, int currentStock, int maxStock, SparseArray<String> listings) {
		this(name, price, game, thumbnail);
		this.currentStock = currentStock;
		this.maxStock = maxStock;
		this.listings = listings;
	}

	public ShopItem(String name, int price, Game game, String thumbnail, String stock, SparseArray<String> listings) {
		this(name, price, game, thumbnail);
		String[] stocks = stock.split(Pattern.quote("/"));
		this.currentStock = Integer.parseInt(stocks[0]);
		this.maxStock = Integer.parseInt(stocks[1]);
		this.listings = listings;
	}

	public String getLongName() {
		return (statTrak ? "StatTrak™ " : "") + name;
	}

	public Uri getListingUrl() {
		return Uri.parse("https://www.trade.ninja/shop/listings/" + game + "/" + (statTrak ? "StatTrak™ " : "") + name + "/");
	}

	public Uri getThumbnailUrl(int size) {
		return Uri.parse("https://steamcommunity-a.akamaihd.net/economy/image/" + this.thumbnail + "/" + size + "fx" + size + "f");
	}
}
