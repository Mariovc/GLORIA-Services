package com.gloria.phoneinterface.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.communications.GloriaApiOperations;
import com.gloria.phoneinterface.structures.Plan;

public class PlanActivity extends SherlockActivity{

	private Plan planObject = null;
	private int planType = -1;

	private GloriaApiOperations apiOperations = null;
	private String authorizationToken = "";

	private Object taskLock = new Object();
	private GetStarInfoFromPlan getStarInfoFromPlanTask = null;
	private RequestPlan requestPlanTask = null;

	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scheduler_object);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		this.authorizationToken = getIntent().getExtras().getString("authorizationToken");
		apiOperations = new GloriaApiOperations(authorizationToken);

		setStarInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		synchronized (taskLock) {
			if (getStarInfoFromPlanTask != null) {
				getStarInfoFromPlanTask.cancel(true);
				getStarInfoFromPlanTask = null;
			}
			else if (requestPlanTask != null) {
				requestPlanTask.cancel(true);
				requestPlanTask = null;
			}
		}
	}

	private void setStarInfo() {
		Bundle extras = getIntent().getExtras();
		planType = extras.getInt("tabType", -1);
		setContent();
		switch(planType){
		case SchedulerActivity.NEW_TAB_POSITION:
			String starId = extras.getString("starId");
			this.planObject = getStarInfoFromCatalogue(starId);
			
			break;
		case SchedulerActivity.ACTIVE_TAB_POSITION:
		case SchedulerActivity.INACTIVE_TAB_POSITION:
			int planId = extras.getInt("planId", -1);
			getStarInfoFromPlanTask = new GetStarInfoFromPlan();
			getStarInfoFromPlanTask.execute(planId);
			break;
		default:
			break;
		}

		if (this.planObject != null)
			setInfoViews();
	}

	private void setContent() {
		if (planType == SchedulerActivity.NEW_TAB_POSITION){
			findViewById(R.id.objectFieldContainer).setVisibility(View.GONE);
			findViewById(R.id.statusFieldContainer).setVisibility(View.GONE);
			findViewById(R.id.resultsFieldContainer).setVisibility(View.GONE);
		} else {
			findViewById(R.id.requestFieldContainer).setVisibility(View.GONE);
		}
	}

	private class RequestPlan extends AsyncTask<Plan, Void, Void> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(PlanActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.sendingPlanRequest));
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Plan... plans) {
			apiOperations.requestPlan(plans[0]);
			return null;
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Void v) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {
				}
				progressDialog = null;
			}

			if (!isCancelled()) {
				Intent returnIntent = new Intent();
				setResult(RESULT_OK,returnIntent);        
				finish();
			}
			synchronized (taskLock) {
				getStarInfoFromPlanTask = null;
			}
		}
	}
	
	
	private class GetStarInfoFromPlan extends AsyncTask<Integer, Void, Plan> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(PlanActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingPlanInfo));
			progressDialog.show();
		}

		@Override
		protected Plan doInBackground(Integer... ids) {
			Plan plan = apiOperations.getPlanInfo(ids[0]);
			return plan;
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Plan plan) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {
				}
				progressDialog = null;
			}

			if (!isCancelled()) {
				planObject = plan;
				setObjectImage();
				setInfoViews();
			}
			synchronized (taskLock) {
				getStarInfoFromPlanTask = null;
			}
		}
	}

	private void setObjectImage() {
		Bitmap bitmap = null;
		String imageName = "star_" + planObject.getObjectName().toLowerCase(Locale.getDefault());
		int imageId = getResources().getIdentifier(imageName,
				"drawable", getPackageName());
		if (imageId > 0)
			bitmap = BitmapFactory.decodeResource(this.getResources(), imageId);
		else
			bitmap = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.no_image);
		planObject.setImage(bitmap);
	}

	private Plan getStarInfoFromCatalogue(String starId) {
		Plan plan = null;

		InputStream is=getResources().openRawResource(R.raw.scheduler_catalogue);
		boolean endSearch = false;
		String text = "";
		String id = "";
		String name = "";
		String longName = "";
		String description = "";
		String ra = "";
		String dec = "";
		Double moonAltitude = -1d;
		Double moonDistance = -1d;
		Double targetAltitude = -1d;
		String filter = "";
		Double exposure = -1d;
		String imageName = "";
		XmlPullParser parser = null;
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(is,"utf-8");

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT && !endSearch) {
				String tagname = parser.getName();
				switch (eventType) {
				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase("id")) {
						id = text;
					} else if (tagname.equalsIgnoreCase("name")) {
						name = text;
					} else if (tagname.equalsIgnoreCase("longname")) {
						longName = text;
					} else if (tagname.equalsIgnoreCase("description")) {
						description = text;
					} else if (tagname.equalsIgnoreCase("ra")) {
						ra = text;
					} else if (tagname.equalsIgnoreCase("dec")) {
						dec = text;
					} else if (tagname.equalsIgnoreCase("moonAltitude")) {
						moonAltitude = Double.parseDouble(text);
					} else if (tagname.equalsIgnoreCase("moonDistance")) {
						moonDistance = Double.parseDouble(text);
					} else if (tagname.equalsIgnoreCase("targetAltitude")) {
						targetAltitude = Double.parseDouble(text);
					} else if (tagname.equalsIgnoreCase("filter")) {
						filter = text;
					} else if (tagname.equalsIgnoreCase("exposure")) {
						exposure = Double.parseDouble(text);
					} else if (tagname.equalsIgnoreCase("image")) {
						imageName = text;
					} else if (tagname.equalsIgnoreCase("star")) {
						if (id.compareTo(starId) == 0) {
							plan = new Plan(description);
							plan.setObjectName(name);
							plan.setObjectCommonName(longName);
							plan.setDescription(description);
							plan.setRa(ra);
							plan.setDec(dec);
							plan.setMoonAltitude(moonAltitude);
							plan.setMoonDistance(moonDistance);
							plan.setTargetAltitude(targetAltitude);
							plan.setFilter(filter);
							plan.setExposure(exposure);
							int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
							Bitmap imageBitmap = BitmapFactory.decodeResource(this.getResources(), imageId);
							plan.setImage(imageBitmap);

							endSearch = true;
							Log.d("DEBUG", "end search");
						}
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

		if (!endSearch)
			return null;
		return plan;
	}

	private void setInfoViews() {
		if (planType == SchedulerActivity.NEW_TAB_POSITION && planObject.getObjectName() != null){
			String planName = planObject.getObjectName() + " " + planObject.getObjectCommonName();
			((TextView) findViewById(R.id.starNameField)).setText(planName);
			getSupportActionBar().setTitle(planName);
		} else {
			((TextView) findViewById(R.id.starNameField)).setText(planObject.getDescription());
			getSupportActionBar().setTitle(planObject.getDescription());
		}

		if (planObject.getImage() != null) {
			ImageView imageView = (ImageView) findViewById(R.id.starImage);
			imageView.setImageBitmap(planObject.getImage());
			Drawable imageDrawable = new BitmapDrawable(getResources(),planObject.getImage());
			getSupportActionBar().setIcon(imageDrawable);
		}

		if (planObject.getStatus() != null)
			((TextView) findViewById(R.id.statusValueField)).setText(planObject.getStatus());
		else 
			findViewById(R.id.statusFieldContainer).setVisibility(View.GONE);

		if (planObject.getObjectName() != null)
			((TextView) findViewById(R.id.objectValueField)).setText(planObject.getObjectName());
		else 
			findViewById(R.id.objectFieldContainer).setVisibility(View.GONE);

		if (planObject.getRa() != null)
			((TextView) findViewById(R.id.raValueField)).setText(planObject.getRa());
		else 
			findViewById(R.id.raFieldContainer).setVisibility(View.GONE);

		if (planObject.getDec() != null)
			((TextView) findViewById(R.id.decValueField)).setText(planObject.getDec());
		else 
			findViewById(R.id.decFieldContainer).setVisibility(View.GONE);

/*		if (planObject.getObjectType() != null)
			((TextView) findViewById(R.id.objectTypeValueField)).setText(planObject.getExposure().toString());
		else 
			findViewById(R.id.exposureFieldContainer).setVisibility(View.GONE);
*/
		if (planObject.getFilter() != null)
			((TextView) findViewById(R.id.filterValueField)).setText(planObject.getFilter());
		else 
			findViewById(R.id.filterFieldContainer).setVisibility(View.GONE);

		if(planObject.getResults() != null && planObject.getResults().size() > 0){
			findViewById(R.id.imageResult).setVisibility(View.GONE);
			LinearLayout imagesContainer = (LinearLayout) findViewById(R.id.resultImages);
			for (int j = 0; j < planObject.getResults().size(); j++) {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setImageBitmap(planObject.getResults().get(j));
				imagesContainer.addView(imageView);
			}
		}
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return(true);
	}

	public void doPlan (View view){
		EditText descriptionView = (EditText) findViewById(R.id.descriptionEditText);
		String descriptionText = descriptionView.getText().toString();
		if (descriptionText.compareTo("") == 0) {
			Toast.makeText(getApplicationContext(), getString(R.string.noDescription), Toast.LENGTH_SHORT).show();
		} else {
			requestPlanTask = new RequestPlan();
			requestPlanTask.execute(this.planObject);
			/*Intent returnIntent = new Intent();
			returnIntent.putExtra("starName",planObject.getObjectName());
			returnIntent.putExtra("starImage", planObject.getImage());
			setResult(RESULT_OK,returnIntent);        
			finish();*/
		}
	}
}
