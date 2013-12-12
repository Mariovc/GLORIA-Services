package com.gloria.phoneinterface.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.communications.GloriaApiOperations;
import com.gloria.phoneinterface.structures.GridItem;
import com.gloria.phoneinterface.structures.Plan;

public class SchedulerActivity extends SherlockActivity implements
ActionBar.TabListener {

	public static final int ACTIVE_TAB_POSITION = 0;
	public static final int NEW_TAB_POSITION = 1;
	public static final int INACTIVE_TAB_POSITION = 2;

	private GridView gridView;
	private GridViewAdapter customGridAdapter;
	private ArrayList<String> starIds = new ArrayList<String>();
	private ArrayList<Integer> planIds = new ArrayList<Integer>();

	private ArrayList<GridItem> plansGrid = new ArrayList<GridItem>();

	private GloriaApiOperations apiOperations = null;
	private String authorizationToken = "";

	private Object taskLock = new Object();
	private GetActiveData getActiveDataTask = null;
	private GetInactiveData getInactiveDataTask = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock); // Used for theme switching in samples
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.scheduler);
		getSupportActionBar().setIcon(R.drawable.scheduler);
		gridView = (GridView) findViewById(R.id.gridView);

		this.authorizationToken = getIntent().getExtras().getString(
				"authorizationToken");
		apiOperations = new GloriaApiOperations(authorizationToken);

		setTabs();
		gridView.setAdapter(customGridAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Log.d("DEBUG", "onItemClick " + position);
				newActivity(position);
			}

		});
	}
	
	/*@Override
	protected void onResume() {
		Log.d("Authorization", "token: "+authorizationToken);
		apiOperations = new GloriaApiOperations(authorizationToken);
		super.onResume();
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		synchronized (taskLock) {
			if (getInactiveDataTask != null) {
				getInactiveDataTask.cancel(true);
				getInactiveDataTask = null;
			} else if (getActiveDataTask != null) {
				getActiveDataTask.cancel(true);
				getActiveDataTask = null;
			}
		}
	}

	private void setTabs() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 0; i < 3; i++) {
			switch (i) {
			case ACTIVE_TAB_POSITION:
				addTab(getResources().getString(R.string.activeScheduleTab));
				break;
			case NEW_TAB_POSITION:
				addTab(getResources().getString(R.string.newScheduleTab));
				break;
			case INACTIVE_TAB_POSITION:
				addTab(getResources().getString(R.string.inactiveScheduleTab));
				break;
			}
		}
	}

	private void newActivity(int position) {
		int currentTab = getSupportActionBar().getSelectedNavigationIndex();
		Intent intent = new Intent(this, StarActivity.class);
		intent.putExtra("authorizationToken", authorizationToken);
		intent.putExtra("tabType", getSupportActionBar().getSelectedNavigationIndex());
		switch (currentTab) {
		case NEW_TAB_POSITION:
			intent.putExtra("starId", starIds.get(position));
			break;
		case ACTIVE_TAB_POSITION:
		case INACTIVE_TAB_POSITION:
			intent.putExtra("planId", planIds.get(position));
			break;
		default:
			break;
		}


		startActivityForResult(intent, 0);

		// Toast.makeText(SchedulerActivity.this, "id: " + starIds.get(position)
		// + "   tab: " + getSupportActionBar().getSelectedNavigationIndex(),
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Search")
		.setIcon(android.R.drawable.ic_menu_search)
		.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		// return super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return (true);
	}

	private ArrayList<GridItem> getObjectsData() {
		final ArrayList<GridItem> imageItems = new ArrayList<GridItem>();
		InputStream is = getResources().openRawResource(
				R.raw.scheduler_catalogue);
		String text = "";
		String image = "";
		String starId = "";
		String name = "";
		Bitmap bitmap = null;
		XmlPullParser parser = null;
		starIds = new ArrayList<String>();
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(is, "utf-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagname = parser.getName();
				switch (eventType) {

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase("name")) {
						name = text;
					} else if (tagname.equalsIgnoreCase("image")) {
						image = text;
					} else if (tagname.equalsIgnoreCase("id")) {
						starId = text;
					} else if (tagname.equalsIgnoreCase("star")) {
						int imageId = getResources().getIdentifier(image,
								"drawable", getPackageName());
						bitmap = BitmapFactory.decodeResource(
								this.getResources(), imageId);
						starIds.add(starId);
						imageItems.add(new GridItem(bitmap, name));
					}

					break;

				default:
					break;
				}
				eventType = parser.next();
			}

		} catch (XmlPullParserException e) {
			Log.e("ERROR", "XML error");
		} catch (IOException e) {
			Log.e("ERROR", "IO exception");
		}

		return imageItems;

	}

	private class GetActiveData extends AsyncTask<Void, Void, ArrayList<Plan>> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(SchedulerActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingActivePlans));
			progressDialog.show();
		}

		@Override
		protected ArrayList<Plan> doInBackground(Void... v) {
			ArrayList<Plan> plans;
				plans = apiOperations.getPlansActive();
			return plans;
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(ArrayList<Plan> plans) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {
				}
				progressDialog = null;
			}

			if (!isCancelled()) {
					if (plans.size() == 0)
						Toast.makeText(getApplicationContext(), R.string.noPlans,
								Toast.LENGTH_SHORT).show();
					setPlans(plans);
					displayPlans();
			}
			synchronized (taskLock) {
				getActiveDataTask = null;
			}
		}
	}

	private class GetInactiveData extends
	AsyncTask<Void, Void, ArrayList<Plan>> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(SchedulerActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingInactivePlans));
			progressDialog.show();
		}

		@Override
		protected ArrayList<Plan> doInBackground(Void... v) {
			ArrayList<Plan> plans;
			// plans = new ArrayList<Plan>();
			plans = apiOperations.getPlansInactive();
			return plans;
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(ArrayList<Plan> plans) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {
				}
				progressDialog = null;
			}

			if (!isCancelled()) {
				if (plans.size() == 0)
					Toast.makeText(getApplicationContext(), R.string.noPlans,
							Toast.LENGTH_SHORT).show();
				setPlans(plans);
				displayPlans();
			}
			synchronized (taskLock) {
				getInactiveDataTask = null;
			}
		}
	}

	private void setPlans(ArrayList<Plan> plans) {
		ArrayList<GridItem> newPlans = new ArrayList<GridItem>();
		planIds = new ArrayList<Integer>();
		for (int i = 0; i < plans.size(); i++) {
			Plan plan = plans.get(i);
			Bitmap image = getObjectImage(plan.getObjectName());
			String title = plan.getDescription();
			String statusText = plan.getStatus();
			Bitmap statusImage = getStatusImage(plan.getStatus());

			GridItem gridItem = new GridItem(image, title, statusText, statusImage);
			newPlans.add(gridItem);

			planIds.add(plan.getId());
		}
		this.plansGrid = newPlans;
	}

	private void displayPlans() {
		customGridAdapter = new GridViewAdapter(this,
				R.layout.scheduler_item_grid, plansGrid);
		gridView.setAdapter(customGridAdapter);
	}

	/*
	 * private ArrayList<GridItem> getInactiveData() { final ArrayList<GridItem>
	 * imageItems = new ArrayList<GridItem>(); InputStream
	 * is=getResources().openRawResource(R.raw.scheduler_objects); String text =
	 * ""; String image = ""; String starId = ""; String name = ""; String
	 * status = ""; Bitmap objectImage = null; Bitmap statusImage = null;
	 * XmlPullParser parser = null; starIds = new ArrayList<String>(); try {
	 * parser = XmlPullParserFactory.newInstance().newPullParser();
	 * parser.setInput(is,"utf-8"); int eventType = parser.getEventType(); while
	 * (eventType != XmlPullParser.END_DOCUMENT) { String tagname =
	 * parser.getName(); switch (eventType) {
	 * 
	 * case XmlPullParser.TEXT: text = parser.getText(); break;
	 * 
	 * case XmlPullParser.END_TAG: if (tagname.equalsIgnoreCase("name")) { name
	 * = text; } else if (tagname.equalsIgnoreCase("image")) { image = text; }
	 * else if (tagname.equalsIgnoreCase("id")) { starId = text; } else if
	 * (tagname.equalsIgnoreCase("status")) { status = text; } else if
	 * (tagname.equalsIgnoreCase("star")) { if (status.compareTo("aborted") == 0
	 * || status.compareTo("error") == 0 || status.compareTo("impossible") == 0
	 * || status.compareTo("done") == 0 ){ int imageId =
	 * getResources().getIdentifier(image, "drawable", getPackageName());
	 * objectImage = BitmapFactory.decodeResource(this.getResources(), imageId);
	 * starIds.add(starId); statusImage = getStatusImage(status);
	 * imageItems.add(new GridItem(objectImage, name, status, statusImage)); } }
	 * 
	 * break;
	 * 
	 * default: break; } eventType = parser.next(); }
	 * 
	 * } catch (XmlPullParserException e) { Log.e("ERROR", "XML error"); } catch
	 * (IOException e) { Log.e("ERROR", "IO exception"); }
	 * 
	 * return imageItems;
	 * 
	 * }
	 */

	private Bitmap getStatusImage(String status) {
		Bitmap bitmap = null;
		String imageName = "status_" + status.toLowerCase(Locale.getDefault());
		int imageId = getResources().getIdentifier(imageName, "drawable",
				getPackageName());
		if (imageId > 0)
			bitmap = BitmapFactory.decodeResource(this.getResources(), imageId);
		else
			bitmap = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.no_image);
		return bitmap;
	}

	private Bitmap getObjectImage(String object) {
		Bitmap bitmap = null;
		String imageName = "star_" + object.toLowerCase(Locale.getDefault());
		int imageId = getResources().getIdentifier(imageName,
				"drawable", getPackageName());
		if (imageId > 0)
			bitmap = BitmapFactory.decodeResource(this.getResources(), imageId);
		else
			bitmap = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.no_image);
		return bitmap;
	}

	private void addTab(String title) {
		ActionBar.Tab tab = getSupportActionBar().newTab();
		tab.setText(title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction transaction) {
		switch (tab.getPosition()) {
		case ACTIVE_TAB_POSITION:
			getActiveDataTask = new GetActiveData();
			getActiveDataTask.execute();
			break;
		case NEW_TAB_POSITION:
			// Toast.makeText(SchedulerActivity.this, "new",
			// Toast.LENGTH_SHORT).show();
			customGridAdapter = new GridViewAdapter(this,
					R.layout.medium_item_grid, getObjectsData());
			break;
		case INACTIVE_TAB_POSITION:
			getInactiveDataTask = new GetInactiveData();
			getInactiveDataTask.execute();
			break;
		}
		gridView.setAdapter(customGridAdapter);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
	}

	private void displaySendResultsDialog() {
		new AlertDialog.Builder(SchedulerActivity.this)
		.setTitle("")
		.setMessage("")
		.setPositiveButton("Request",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO new activity
			}
		}).setCancelable(true).show();
	}

	/*
	 * @Override public void finishActivityFromChild(Activity child, int
	 * requestCode) { Toast.makeText(SchedulerActivity.this,
	 * "finished from child", Toast.LENGTH_SHORT).show();
	 * getSupportActionBar().setSelectedNavigationItem(ACTIVE_TAB_POSITION);
	 * super.finishActivityFromChild(child, requestCode); }
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			/*Bitmap bitmap = (Bitmap) data.getParcelableExtra("starImage");
			String name = data.getStringExtra("starName");
			getSupportActionBar()
			.setSelectedNavigationItem(ACTIVE_TAB_POSITION);
			customGridAdapter.getData().add(new GridItem(bitmap, name));*/
			Toast.makeText(SchedulerActivity.this, "Plan added", Toast.LENGTH_LONG).show();
		}
	}
}
