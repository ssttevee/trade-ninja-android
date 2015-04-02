package com.ssttevee.tradeninja.app.adapters;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ssttevee.tradeninja.app.R;

import java.util.HashMap;

public class NewsAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater;
	private HashMap<String,String>[] articles;

	public NewsAdapter(Activity activity, HashMap<String,String>[] articles) {
		mLayoutInflater = activity.getLayoutInflater();
		this.articles = articles;
	}

	@Override
	public int getCount() {
		return articles.length;
	}

	@Override
	public HashMap<String,String> getItem(int position) {
		return articles[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView == null ? mLayoutInflater.inflate(R.layout.listviewitem_news, null) : convertView;
		((TextView) v.findViewById(R.id.news_article_title)).setText(getItem(position).get("title"));
		((TextView) v.findViewById(R.id.news_article_time)).setText(getItem(position).get("time"));
		((TextView) v.findViewById(R.id.news_article_body)).setText(Html.fromHtml(getItem(position).get("body")));
		return v;
	}

	public void setArticles(HashMap<String,String>[] articles) {
		this.articles = articles;
		notifyDataSetChanged();
	}

}
