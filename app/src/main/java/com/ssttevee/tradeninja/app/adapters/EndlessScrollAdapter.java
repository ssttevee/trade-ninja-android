package com.ssttevee.tradeninja.app.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 *  A child class shall subclass this Adapter and
 *  implement method getDataRow(int position, View convertView, ViewGroup parent),
 *  which supplies a View present data in a ListRow.
 *
 *  This parent Adapter takes care of displaying ProgressBar in a row or
 *  indicating that it has reached the last row.
 *
 */
public abstract class EndlessScrollAdapter<T> extends BaseAdapter {

	// the main data list to save loaded data
	protected List<T> dataList;

	protected Activity mActivity;

	// the serverListSize is the total number of items on the server side,
	// which should be returned from the web request results
	protected int serverListSize = -1;

	// Two view types which will be used to determine whether a row should be displaying
	// data or a Progressbar
	public static final int VIEW_TYPE_LOADING = 0;
	public static final int VIEW_TYPE_ACTIVITY = 1;


	public EndlessScrollAdapter(Activity activity) {
		this(activity, new ArrayList<T>());
	}

	public EndlessScrollAdapter(Activity activity, List<T> list) {
		mActivity = activity;
		dataList = list;
	}

	public void setServerListSize(int serverListSize) {
		this.serverListSize = serverListSize;
	}

	/**
	 * disable click events on indicating rows
	 */
	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == VIEW_TYPE_ACTIVITY;
	}

	/**
	 * One type is normal data row, the other type is Progressbar
	 */
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/**
	 * the size of the List plus one, the one is the last row, which displays a Progressbar
	 */
	@Override
	public int getCount() {
		return dataList.size() + (dataList.size() > 5 ? 1 : 0);
	}

	/**
	 * return the type of the row,
	 * the last row indicates the user that the ListView is loading more data
	 */
	@Override
	public int getItemViewType(int position) {
		return position >= dataList.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_ACTIVITY;
	}

	@Override
	public T getItem(int position) {
		return getItemViewType(position) == VIEW_TYPE_ACTIVITY ? dataList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return getItemViewType(position) == VIEW_TYPE_ACTIVITY ? position : -1;
	}

	/**
	 *  returns the correct view
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		if (getItemViewType(position) == VIEW_TYPE_LOADING) {
			// display the last row
			return getFooterView(position, convertView, parent);
		}

		return getDataRow(position, convertView, parent);
	}

	/**
	 * A subclass should override this method to supply the data row.
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public abstract View getDataRow(int position, View convertView, ViewGroup parent);

	/**
	 * returns a View to be displayed in the last row.
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public View getFooterView(int position, View convertView, ViewGroup parent) {
		if (position >= serverListSize && serverListSize > 0) {
			// the ListView has reached the last row
			return new View(mActivity);
		}

		View row = convertView;
		if (row == null || !(row instanceof ProgressBar)) {
			row = new ProgressBar(mActivity);
		}

		return row;
	}

}