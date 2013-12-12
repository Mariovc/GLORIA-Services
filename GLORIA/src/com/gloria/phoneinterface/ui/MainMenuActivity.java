package com.gloria.phoneinterface.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.structures.GridItem;


public class MainMenuActivity extends SherlockActivity {
	

	private String authorizationToken = ""; 
	
	private GridView gridView;
	private GridViewAdapter customGridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		
		authorizationToken = getIntent().getExtras().getString("authorizationToken");

		gridView = (GridView) findViewById(R.id.gridView);
		customGridAdapter = new GridViewAdapter(this, R.layout.big_item_grid, getData());
		gridView.setAdapter(customGridAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Log.d("DEBUG", "onItemClick");
				newActivity(position);
			}

		});

	}

	private void newActivity(int position){
		Intent intent = null;
		switch (position){
		case 0: 
			intent = new Intent(this, TelescopesActivity.class);
			break;
		case 1:
			intent = new Intent(this, ImagesActivity.class);
			break;
		case 2:
			intent = new Intent(this, SchedulerActivity.class);
			break;
		default:
			Toast.makeText(MainMenuActivity.this, "work in progress", Toast.LENGTH_SHORT).show();
			break;
		}
		if (intent != null){
			intent.putExtra("authorizationToken", authorizationToken);
			startActivity(intent);
		}	
	}

	private ArrayList<GridItem> getData() {
		final ArrayList<GridItem> imageItems = new ArrayList<GridItem>();

		/*for (int i = 0; i < gridItems.size(); i++) {
			GridItem item = gridItems.get(i);
			Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), item.getImageResourceId());
			imageItems.add(new ImageItem(bitmap, item.getName()));
		}*/


		TypedArray imgs = getResources().obtainTypedArray(R.array.section_image_ids);
		TypedArray names = getResources().obtainTypedArray(R.array.section_names);
		for (int i = 0; i<imgs.length(); i++){
			Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), imgs.getResourceId(i, -1));
			names.getString(i);
			imageItems.add(new GridItem(bitmap, names.getString(i)));
		}
		imgs.recycle();
		names.recycle();


		return imageItems;

	}

}
