package com.gloria.phoneinterface.ui;

import java.util.ArrayList;

import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.structures.GridItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class GridViewAdapter extends ArrayAdapter<GridItem> {

	private Context context;
	private int layoutResourceId;
	private ArrayList<GridItem> data = new ArrayList<GridItem>();

	public GridViewAdapter(Context context, int layoutResourceId,
			ArrayList<GridItem> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.imageTitle = (TextView) row.findViewById(R.id.text);
			holder.image = (ImageView) row.findViewById(R.id.image);
			holder.statusText = (TextView) row.findViewById(R.id.statusTitle);
			holder.statusImage = (ImageView) row.findViewById(R.id.statusImage);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		GridItem item = data.get(position);
		holder.imageTitle.setText(item.getTitle());
		holder.image.setImageBitmap(item.getImage());
		if (item.getStatusText() != null && 
				item.getStatusImage() != null &&
				holder.statusText != null && 
				holder.statusImage != null) {
			holder.statusText.setText(item.getStatusText());
			holder.statusImage.setImageBitmap(item.getStatusImage());
		}
		return row;
	}

	static class ViewHolder {
		TextView imageTitle;
		ImageView image;
		TextView statusText;
		ImageView statusImage;
	}
	

	public ArrayList<GridItem> getData() {
		return data;
	}
}