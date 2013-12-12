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


	private static final String T01_BOOTES1 = "BOOTES1";
	private static final String T02_BOOTES2 = "BOOTES2";
	private static final String T03_BOOTES3 = "BOOTES3";
	private static final String T04_CABEVA1  = "CAB-EVA1";
	private static final String T05_CABCAB  = "CAB-CAB";
	private static final String T06_CABCAHA  = "CAB-CAHA";
	private static final String T07_BART = "BART";
	private static final String T08_FRAM  = "FRAM";
	private static final String T09_PI1 = "PI1";
	private static final String T10_WATCHER = "WATCHER";
	private static final String T11_TOLOLO = "TOLOLO";
	private static final String T12_OM = "OM";
	private static final String T13_TAD = "TAD";
	private static final String T14_D50 = "D50";
	private static final String T15_TAU = "TAU";
	private static final String T16_TORTORA = "TORTORA";
	private static final String T17_PI2 = "PI2";

	
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
			ArrayList<String> telescopesNames = apiOperations.getTelescopesNames();
			ArrayList<Bitmap> telescopesImages = getTelescopesImages(telescopesNames);
			ArrayList<Telescope> telescopesArray = new ArrayList<Telescope>();
			for (int i=0; i < telescopesNames.size(); i++) {
				Telescope telescope = new Telescope(telescopesNames.get(i), telescopesImages.get(i));
				telescopesArray.add(telescope);
			}
			telescopes = telescopesArray;
			Log.d("DEBUG", "array de telescopes: " + telescopesNames.toString());
			
			// TODO call API to get telescopes images
			
			
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

		private ArrayList<Bitmap> getTelescopesImages(ArrayList<String> telescopesNames) {
			ArrayList<Bitmap> images = new ArrayList<Bitmap>();
			Bitmap image;
			for (int i = 0; i < telescopesNames.size(); i++) {
				String name = telescopesNames.get(i);
				// switch
				if (name.compareTo(T01_BOOTES1) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t01_bootes_1);
				else if (name.compareTo(T02_BOOTES2) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t02_bootes_2);
				else if (name.compareTo(T03_BOOTES3) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t03_bootes_3);
				else if (name.compareTo(T04_CABEVA1) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t04_cab_eva_1);
				else if (name.compareTo(T05_CABCAB) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t05_cab_cab);
				else if (name.compareTo(T06_CABCAHA) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t06_cab_caha);
				else if (name.compareTo(T07_BART) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t07_bart);
				else if (name.compareTo(T08_FRAM) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t08_fram);
				else if (name.compareTo(T09_PI1) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t09_piofthesky1);
				else if (name.compareTo(T10_WATCHER) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t10_watcher);
				else if (name.compareTo(T11_TOLOLO) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t11_tololo);
				else if (name.compareTo(T12_OM) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t12_om);
				else if (name.compareTo(T13_TAD) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t13_tad);
				else if (name.compareTo(T14_D50) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t14_d50);
				else if (name.compareTo(T15_TAU) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t15_tau);
				else if (name.compareTo(T16_TORTORA) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t16_tortora);
				else if (name.compareTo(T17_PI2) == 0)
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t17_piofthesky2);
				else 
					image = BitmapFactory.decodeResource(getResources(), R.drawable.t00_no_telescope);
				images.add(image);
			}
			return images;
		}
	}
	

}
