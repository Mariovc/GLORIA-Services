package com.gloria.phoneinterface.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.communications.GloriaApiOperations;
import com.gloria.phoneinterface.structures.GridItem;
import com.gloria.phoneinterface.structures.Telescope;


public class TelescopesActivity extends SherlockActivity{

	
	private GridView gridView;
	private GridViewAdapter customGridAdapter;
	private ArrayList<Telescope> telescopes = new ArrayList<Telescope>();


	private GloriaApiOperations apiOperations = null;
	private String authorizationToken = "";
	private Object taskLock = new Object();
	private GetTelescopesList getTelescopesTask = null;


	/* *****************************************
	 ************ Life cycle  *****************
	 ***************************************** */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.telescopes);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.telescopes);

		this.authorizationToken = getIntent().getExtras().getString("authorizationToken");
		apiOperations = new GloriaApiOperations(authorizationToken);
		
		getTelescopesTask = new GetTelescopesList();
		getTelescopesTask.execute();
		//telescopes = getTelescopesInfo();
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		synchronized (taskLock) {
			if (getTelescopesTask != null) {
				getTelescopesTask.cancel(true);
				getTelescopesTask = null;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Search")
		.setIcon(android.R.drawable.ic_menu_search)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//return super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return(true);
	}
	

	/* *****************************************
	 ************ Grid View  *****************
	 ***************************************** */
	
	
	private void initGrid () {
		gridView = (GridView) findViewById(R.id.gridView);
		customGridAdapter = new GridViewAdapter(this, R.layout.medium_item_grid, getData());
		gridView.setAdapter(customGridAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Log.d("DEBUG", "onItemClick " + position);
				newTelescopeActivity(position);
			}

		});
	}

	private void newTelescopeActivity(int position){
		Intent intent = new Intent(this, TelescopeActivity.class);
		intent.putExtra("authorizationToken", authorizationToken);
		intent.putExtra("telescopeName", telescopes.get(position).getName());
		intent.putExtra("telescopeImage", telescopes.get(position).getImage());
		startActivity(intent);
	}

	private ArrayList<GridItem> getData() {
		final ArrayList<GridItem> imageItems = new ArrayList<GridItem>();
		for (int i = 0; i < telescopes.size(); i++) {
			Telescope telescope = telescopes.get(i);
			imageItems.add(new GridItem(telescope.getImage(), telescope.getName()));
		}

		return imageItems;
	}



	/*private ArrayList<Telescope> getTelescopesInfo() {
		// call API to get information
		ArrayList<Telescope> telescopes = new ArrayList<Telescope>();
		Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.t01_bootes_1);
		Telescope telescope = new Telescope(image, "T01_BOOTES_1");
		telescopes.add(telescope);
		return telescopes;
	}*/




	/* *****************************************
	 ************ Async Tasks ****************
	 ***************************************** */

	private class GetTelescopesList extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(TelescopesActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingTelescopes));
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			ArrayList<String> telescopeNames = apiOperations.getTelescopesNames();
			ArrayList<Bitmap> telescopeImages = new ArrayList<Bitmap>();
			for (int i = 0; i < telescopeNames.size(); i++) {
				Bitmap image = apiOperations.getTelescopeImage(telescopeNames.get(i));
				if (image == null)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t00_no_telescope);
				telescopeImages.add(image);
			}
			ArrayList<Telescope> telescopesArray = new ArrayList<Telescope>();
			for (int i=0; i < telescopeNames.size(); i++) {
				Telescope telescope = new Telescope(telescopeNames.get(i), telescopeImages.get(i));
				telescopesArray.add(telescope);
			}
			telescopes = telescopesArray;
			Log.d("DEBUG", "array de telescopes: " + telescopeNames.toString());
			
			return null;
		}


		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Void v) {
			initGrid();
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {}
				progressDialog = null;
			}	

			if (!isCancelled()) {
				/* what should do if the activity is destroyed*/
			}
			synchronized (taskLock) {
				getTelescopesTask = null;
			}
		}

		
	}
	

}
