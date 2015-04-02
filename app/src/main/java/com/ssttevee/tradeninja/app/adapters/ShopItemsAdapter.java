package com.ssttevee.tradeninja.app.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ssttevee.tradeninja.app.R;
import com.ssttevee.tradeninja.app.TradeNinja;
import com.ssttevee.tradeninja.app.helpers.ShopItem;

import java.util.List;

public class ShopItemsAdapter extends EndlessScrollAdapter<ShopItem> {

	public ShopItemsAdapter(Activity activity) {
		super(activity);
	}

	@Override
	public View getDataRow(int position, View convertView, ViewGroup parent) {
		View v = mActivity.getLayoutInflater().inflate(R.layout.listviewitem_shopitem, null);
		ShopItem shopItem = getItem(position);

		((TextView) v.findViewById(R.id.shopitem_name)).setText(shopItem.name);
		((TextView) v.findViewById(R.id.shopitem_name)).setTextColor(mActivity.getResources().getColor(shopItem.statTrak ? R.color.trade_ninja_shopitem_orange : R.color.trade_ninja_shopitem_white));
		((TextView) v.findViewById(R.id.shopitem_game)).setText(shopItem.game.getLongName());
		((TextView) v.findViewById(R.id.shopitem_price)).setText(shopItem.price + "");

		v.findViewById(R.id.shopitem_thumbnail).setBackgroundResource(shopItem.statTrak ? R.drawable.trade_ninja_shopitem_orange : R.drawable.trade_ninja_shopitem_white);
		TradeNinja.setShopItemThumbnail(mActivity, shopItem, (ImageView) v.findViewById(R.id.shopitem_thumbnail), 46);

		return v;
	}

	public void clearShopItems() {
		dataList.clear();
		notifyDataSetInvalidated();
	}

	public void setShopItems(List<ShopItem> shopItems) {
		dataList = shopItems;
		notifyDataSetInvalidated();
	}

	public void addShopItems(List<ShopItem> shopItems) {
		dataList.addAll(shopItems);
		notifyDataSetChanged();
	}
}
