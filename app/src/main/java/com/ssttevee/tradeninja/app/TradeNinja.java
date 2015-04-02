package com.ssttevee.tradeninja.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import com.ssttevee.tradeninja.app.helpers.*;
import com.ssttevee.tradeninja.app.ssl.CustomSSLSocketFactory;
import com.ssttevee.tradeninja.app.ssl.CustomX509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class TradeNinja {

	private HttpsURLConnection conn;
	private HttpClient httpclient = AndroidHttpClient.newInstance("Trade Ninja Android/InDev");
	private BasicHttpContext localContext = new BasicHttpContext();
	private CookieStore cookieJar = new BasicCookieStore();
	private String actionKey = "";
	private int userId = -1;

	private HashMap<String, Bitmap> teamIconBitmaps = new HashMap<>();
	private HashMap<String, Bitmap> shopItemBitmaps = new HashMap<>();

	public static boolean signedIn = false;
	public static int creditBalance = -1;

	private static TradeNinja instance;
	private static TradeNinja instance() {
		return instance == null ? instance = new TradeNinja() : instance;
	}

	public TradeNinja() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[]{new CustomX509TrustManager()}, new SecureRandom());

			SSLSocketFactory socketFactory = new CustomSSLSocketFactory(ctx);
			socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", socketFactory, 443));
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieJar);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private String getBody(String url) throws IOException {
		HttpGet get = new HttpGet(url);
		String body = "";

		HttpResponse response = httpclient.execute(get, localContext);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		while ((line = rd.readLine()) != null)
			body += line;

		return body;
	}

	private String postBody(String url, List<NameValuePair> nvps) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		String body = "";

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));

		HttpResponse response = httpclient.execute(httpPost, localContext);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		while ((line = rd.readLine()) != null)
			body += line;

		return body;
	}

	private Document getDocument(String url) throws IOException {
		Document doc = Jsoup.parse(getBody(url));
		onDocumentParse(doc);
		return doc;
	}

	private Document getDocument(String url, List<NameValuePair> nvps) throws IOException {
		Document doc = Jsoup.parse(postBody(url, nvps));
		onDocumentParse(doc);
		return doc;
	}

	private void onDocumentParse(Document doc) {
		signedIn = doc.select("#user-dropdown").size() > 0;
		if(!signedIn) {
			signOut(null);
		} else {
			actionKey = doc.select("#user-dropdown a").get(2).attr("href").substring(21);
			String strBalance = doc.select(".credit-balance").get(0).text();
			creditBalance = Integer.parseInt(strBalance.substring("Credits: ".length(), strBalance.indexOf(" [+]")));
		}
	}

	private void setCookie(String cookie){
		cookieJar.clear();
		if(!cookie.equals("")){
			String[] cookies = cookie.split(";");
			for(int i=0; i< cookies.length; i++){
				String[] nvp = cookies[i].split("=");
				if(nvp[0].equals("user")) {
					signedIn = true;
					TradeNinjaApplication.setSession(cookie);
				}

				BasicClientCookie c = new BasicClientCookie(nvp[0], nvp[1]);
				//c.setVersion(1);
				c.setDomain(".trade.ninja");
				cookieJar.addCookie(c);
			}
		}
	}

	public static void signOut(@Nullable Activity activity) {
		signedIn = false;
		creditBalance = -1;
		instance().userId = -1;
		instance().cookieJar.clear();
		TradeNinjaApplication.clearSession();
		if(activity != null) ((MainActivity) activity).supportInvalidateOptionsMenu();
	}

	public static void signIn(Activity activity) {
		signIn(activity, null);
	}

	public static void signIn(Activity activity, @Nullable final Runnable callback) {
		final WebView wv = new WebView(activity) {
			@Override
			public boolean onCheckIsTextEditor() {
				return true;
			}
		};
		final AlertDialog dialog = new AlertDialog.Builder(activity)
				.setView(wv).create();

		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl("https://www.trade.ninja/sign-in/");
		wv.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(WebView view, String url) {
				if(url.contains("profiles")) {
					CookieManager cm = CookieManager.getInstance();
					String c = cm.getCookie(url);
					instance().setCookie(c);
					view.addJavascriptInterface(new Object() {
						@JavascriptInterface
						public void setUserId(int userId) {
							instance().userId = userId;
							TradeNinjaApplication.setUserId(userId);
							dialog.cancel();
							signedIn = true;
							if(callback != null) {
								new Handler(Looper.getMainLooper()).post(callback);
							}
						}
					}, "injectedObject");
					view.loadUrl("javascript:injectedObject.setUserId(userId);");
				}
			}
		});

		dialog.show();
	}

	public static void restoreSession(int userId, @Nullable String cookie) {
		if(cookie != null) {
			instance().userId = userId;
			instance().setCookie(cookie);
		}
	}

	public static HashMap<String,String>[] getNews() {
		try {
			Document doc = instance().getDocument("https://www.trade.ninja/");
			Elements newsArticles = doc.select(".news-article");
			HashMap<String,String>[] parsedArticles = new HashMap[newsArticles.size()];
			for(int i = 0; i < newsArticles.size(); i++) {
				parsedArticles[i] = new HashMap<>();
				Element article = newsArticles.get(i);

				parsedArticles[i].put("title", article.getElementsByClass("news-article-title").text());
				parsedArticles[i].put("url", article.getElementsByClass("news-article-title").get(0).getElementsByTag("a").get(0).attributes().get("href"));
				parsedArticles[i].put("time", article.getElementsByClass("news-article-time").text());
				parsedArticles[i].put("body", article.getElementsByClass("news-article-body").html());
			}
			return parsedArticles;
		} catch(IOException e) {
			e.printStackTrace();
			return new HashMap[0];
		}
	}

	public static Match[][] getMatches() {
		try {
			Document doc = instance().getDocument("https://www.trade.ninja/betting/");
			Elements categoryContainers = doc.select(".main-column .container");
			Match[][] matches = new Match[3][];
			for(int i = 0; i < categoryContainers.size(); i++) {
				Elements matchContainers = categoryContainers.get(i).getElementsByClass("match-container");
				matches[i] = new Match[matchContainers.size()];

				for(int j = 0; j < matchContainers.size(); j++) {
					Element match = matchContainers.get(j);

					String matchLink = match.getElementsByClass("match-link").attr("href");
					int matchId = Integer.parseInt(matchLink.substring(9, matchLink.length() - 1));

					String[] t1 = match.getElementsByClass("match-team-left").text().split(" ");
					String t1name = "";
					for(int k = 0; k < t1.length - 1; k++) {
						if(!t1name.isEmpty()) t1name += " ";
						t1name += t1[k];
					}
					int t1chance = Integer.parseInt(t1[t1.length - 1].substring(1, t1[t1.length - 1].length() - 2));

					String[] t2 = match.getElementsByClass("match-team-right").text().split(" ");
					String t2name = "";
					for(int k = 0; k < t2.length - 1; k++) {
						if(!t2name.isEmpty()) t2name += " ";
						t2name += t2[k];
					}
					int t2chance = Integer.parseInt(t2[t2.length-1].substring(1,t2[t2.length-1].length()-3));

					int winner = 0;
					if(match.getElementsByClass("match-team-left").get(0).hasClass("loser")) winner += 2;
					if(match.getElementsByClass("match-team-right").get(0).hasClass("loser")) winner += 1;

					matches[i][j] = new Match(
							t1name.replace("\u00A0", ""),
							t1chance,
							t2name.replace("\u00A0", ""),
							t2chance,
							match.getElementsByClass("match-info-left").text().trim(),
							match.getElementsByClass("match-info-right").text().trim(),
							match.getElementsByClass("extra-info").text(),
							winner
					);

					matches[i][j].id = matchId;
				}
			}
			return matches;
		} catch(IOException e) {
			e.printStackTrace();
			return new Match[3][];
		}
	}

	public static Match getMatch(int matchId) {
		try {
			Document doc = instance().getDocument("https://www.trade.ninja/betting/" + matchId + "/");
			doc.setBaseUri(matchId + "");
			return getMatch(doc);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Match getMatch(Document doc) {
		Element matchContainer = doc.select(".main-column .match-container").get(0);
		Elements sideColumn = doc.select(".side-column .container");
		Element scriptTag = doc.getElementsByTag("script").get(1);
		Element quicklinks = sideColumn.get(0);
		Element outcome = sideColumn.size() == 4 ? sideColumn.get(2) : null;
		Element userBet = sideColumn.size() == 4 ? sideColumn.get(3) : sideColumn.size() == 2 ? null : sideColumn.get(2);

		String[] t1 = matchContainer.getElementsByClass("match-team-left").text().split(" ");
		String t1name = "";
		for(int k = 0; k < t1.length - 1; k++) {
			if(!t1name.isEmpty()) t1name += " ";
			t1name += t1[k];
		}

		String[] t2 = matchContainer.getElementsByClass("match-team-right").text().split(" ");
		String t2name = "";
		for(int k = 0; k < t2.length - 1; k++) {
			if(!t2name.isEmpty()) t2name += " ";
			t2name += t2[k];
		}

		int winner = 0;
		if(matchContainer.getElementsByClass("match-team-left").get(0).hasClass("loser")) winner += 2;
		if(matchContainer.getElementsByClass("match-team-right").get(0).hasClass("loser")) winner += 1;

		String[] variables = scriptTag.html().split(Pattern.quote(";"));
		int creditsA = Integer.parseInt(variables[0].split(Pattern.quote("= "))[1]);
		int creditsB = Integer.parseInt(variables[1].split(Pattern.quote("= "))[1]);

		Match match = new Match(
				Integer.parseInt(doc.baseUri()),
				new Teams(
						new Team(
								t1name.replace("\u00A0", ""),
								creditsA
						),
						new Team(
								t2name.replace("\u00A0", ""),
								creditsB
						)
				),
				winner,
				matchContainer.getElementsByClass("match-info-right").text().trim().substring(10),
				matchContainer.getElementsByClass("match-info-left").text().trim(),
				matchContainer.getElementsByClass("extra-info").text()
		);

		Elements links = quicklinks.getElementsByTag("a");
		if(links.size() > 0) {
			match.url = links.get(0).attr("href");
		}

		if(outcome != null) {
			Elements values = outcome.getElementsByClass("right-align");
			match.teams.a.score = Integer.parseInt(values.get(0).text());
			match.teams.b.score = Integer.parseInt(values.get(1).text());
		}

		if(userBet != null && userBet.getElementsByClass("container-title").text().equals("Your bet")) {
			Elements values = userBet.getElementsByClass("right-align");
			if(values.size() > 0)
				match.bet = new Bet(
						Integer.parseInt(values.get(1).text().split(" ")[0]),
						match,
						values.get(0).text().equals(match.teams.a.longName) ? 1 : 2
				);
		}

		return match;
	}

	public static List<ShopItem> getShopItems(String searchQuery, List<Game> games, String sort, int page) {
		List<ShopItem> shopItems = new ArrayList<>();
		try {
			String gamesquerystr = "games[]=" + TextUtils.join("&games[]=", games);
			Document doc = instance().getDocument("https://www.trade.ninja/shop/?query=" + searchQuery + "&" + gamesquerystr + "&sort_column=" + sort + "&sort_order=desc&page=" + page);

			Elements items = doc.select(".main-column .container-content .shop-search-result");

			for(int i = 0; i < items.size(); i++) {
				Element item = items.get(i);

				shopItems.add(new ShopItem(
						item.select(".item-info-a span").get(0).text(),
						Integer.parseInt(item.select(".item-info-b .price").get(0).text().replaceAll(Pattern.quote(","), "")),
						Game.fromLongName(item.select(".item-info-a .label").get(0).text()),
						item.getElementsByTag("img").get(0).attr("src")
				));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}

		return shopItems;
	}

	public static ShopItem getShopItemListings(String shopItem) {
		SparseArray<String> listings = new SparseArray<>();
		try {
			Document doc = instance().getDocument(shopItem);

			Elements containers = doc.select(".full-column .container");
			Element item = containers.get(0);

			for(Element listing : containers.get(1).select(".dynamic-data tbody tr")) {
				String[] listingComps = listing.getElementsByTag("a").get(0).attr("href").split(Pattern.quote("/"));

				listings.put(Integer.parseInt(listingComps[listingComps.length - 2]), listing.getElementsByTag("td").get(0).text());
			}

			Elements bolded = item.getElementsByTag("b");

			return new ShopItem(
					item.getElementsByTag("h1").get(0).text(),
					Integer.parseInt(bolded.get(0).text().split(" ")[0].replaceAll(Pattern.quote(","), "")),
					Game.fromLongName(item.getElementsByTag("h2").get(0).text()),
					item.getElementsByTag("img").get(0).attr("src"),
					bolded.get(1).text(),
					listings
			);
		} catch(IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static JSONObject getHistory(int page) {
		try {
			String json = instance().getBody("https://www.trade.ninja/ajax/?action=get_betting_history&context=full&page=" + page);
			return new JSONObject(json);
		} catch(Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}

	public static Match placeBet(int matchId, String team, int credits, boolean rules) {
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("actionkey", instance().actionKey));
		nvps.add(new BasicNameValuePair("team", team));
		nvps.add(new BasicNameValuePair("amount", credits + ""));
		if(rules) nvps.add(new BasicNameValuePair("rules", "on"));
		nvps.add(new BasicNameValuePair("submit-place", "Place bet"));

		try {
			Document doc = instance().getDocument("https://www.trade.ninja/betting/" + matchId + "/", nvps);
			doc.setBaseUri(matchId + "");
			return getMatch(doc);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Match removeBet(int matchId) {
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("actionkey", instance().actionKey));
		nvps.add(new BasicNameValuePair("submit-remove", "Remove bet"));

		try {
			Document doc = instance().getDocument("https://www.trade.ninja/betting/" + matchId + "/", nvps);
			doc.setBaseUri(matchId + "");
			return getMatch(doc);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setTeamLogo(final Context context, final String urlSlug, final ImageView placeholder) {
		setImageViewBitmapFromCache(context, placeholder, urlSlug, "team_logos", "https://cdn.trade.ninja/team-icons/" + urlSlug + ".png");
	}

	public static void setShopItemThumbnail(final Context context, final ShopItem shopItem, final ImageView placeholder, int dipsize) {
		int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipsize, context.getResources().getDisplayMetrics());
		setImageViewBitmapFromCache(context, placeholder, shopItem.thumbnail + "@" + size, "item_thumbnails", shopItem.getThumbnailUrl(size).toString());
	}

	private static void setImageViewBitmapFromCache(final Context context, final ImageView imageView, final String fileName, final String directory, final String url) {
		imageView.setTag(fileName);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Bitmap bm;
				if(instance().teamIconBitmaps.containsKey(fileName)) {
					bm = instance().teamIconBitmaps.get(fileName);
				} else {
					File iconsDir = new File(context.getCacheDir().getAbsolutePath() + "/" + directory);
					final File icon = new File(iconsDir.getAbsolutePath() + "/" + fileName + ".png");
					if(!iconsDir.exists()) iconsDir.mkdirs();

					if(!icon.exists()) {
						try {
							InputStream is = new URL(url).openStream();
							FileOutputStream fos = new FileOutputStream(icon);
							int inByte;
							while((inByte = is.read()) != -1) fos.write(inByte);
							is.close();
							fos.close();
						} catch(Exception e) {
							// The team doesn't have an icon or user has no internet access
							// e.printStackTrace();
						}
					}

					if(!icon.exists()) return;
					if(!imageView.getTag().equals(fileName)) return;

					bm = BitmapFactory.decodeFile(icon.getAbsolutePath());
					instance().teamIconBitmaps.put(fileName, bm);
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						imageView.setImageBitmap(bm);
					}
				});
			}
		}).start();
	}

	public static int calculatePotentialReward(double credits, double forTeamCredits, double otherTeamCredits) {
		return (int) Math.max(Math.floor((credits/forTeamCredits)*(forTeamCredits + otherTeamCredits) * .98D - credits), 0);
	}

}
