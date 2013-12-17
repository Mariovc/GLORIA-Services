package com.gloria.phoneinterface.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.touch.TouchImageView;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.communications.GloriaApiOperations;
import com.gloria.phoneinterface.structures.Image;

public class ImagesActivity extends SherlockActivity implements ActionBar.OnNavigationListener{


	private static final String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/newImage.jpg";
	private static final int DATE_DIALOG_ID = 0;
	private boolean datePickerCancelled = false;
	private boolean loadImages = true;
	private int dateYear;
	private int dateMonth;
	private int dateDay;

	private ArrayList<Integer> imageIdsList = new ArrayList<Integer>();
	private int imagesListCursor = -1;
	private Image image;
	private TouchImageView imageTouchView = null;
	private boolean displayedElements = true;

	private GloriaApiOperations apiOperations = null;
	private String authorizationToken = "";
	private Object taskLock = new Object();
	private GetImagesList getImagesListTask = null;
	private GetImagesListByObject getImagesListByObjectTask = null;
	private GetNewImage getImageTask = null;

	private String[] categories;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock); //Used for theme switching in samples
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.images);
		getSupportActionBar().setIcon(R.drawable.images);

		this.authorizationToken = getIntent().getExtras().getString("authorizationToken");
		apiOperations = new GloriaApiOperations(authorizationToken);

		imageTouchView = (TouchImageView) findViewById(R.id.touchableImage);
		setNavigationList();
		initCalendarSettings();
		// showDialog(DATE_DIALOG_ID);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		synchronized (taskLock) {
			if (getImagesListTask != null) {
				getImagesListTask.cancel(true);
				getImagesListTask = null;
			} else if (getImageTask != null) {
				getImageTask.cancel(true);
				getImageTask = null;
			} else if (getImagesListByObjectTask != null) {
				getImagesListByObjectTask.cancel(true);
				getImagesListByObjectTask = null;
			}
		}
	}




	public void showHideElements (View view) {
		if (displayedElements) {
			getSupportActionBar().hide();
			findViewById(R.id.previousButton).setVisibility(View.INVISIBLE);
			findViewById(R.id.nextButton).setVisibility(View.INVISIBLE);
			displayedElements = false;
		} else {
			getSupportActionBar().show();
			findViewById(R.id.previousButton).setVisibility(View.VISIBLE);
			findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
			displayedElements = true;
		}
	}




	/* *****************************************
	 ************ Calendar *******************
	 ***************************************** */

	private void initCalendarSettings() {
		final Calendar calendar = Calendar.getInstance();
		dateYear = calendar.get(Calendar.YEAR);
		dateMonth = calendar.get(Calendar.MONTH);
		dateDay = calendar.get(Calendar.DAY_OF_MONTH);
	}

	private void updateDateDisplay() {       
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
		Calendar calendar = new GregorianCalendar(dateYear, dateMonth, dateDay);
		Date date = calendar.getTime();

		setTitle(formatter.format(date));
	}

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Log.d("DEBUG", "onSetDate: " + year + monthOfYear + dayOfMonth);
			if (!datePickerCancelled) {
				datePickerCancelled = true; // known bug - onDateSet called twice sometimes. This will avoid it
				dateYear = year;                   
				dateMonth = monthOfYear;                   
				dateDay = dayOfMonth; 
				getImagesListTask = new GetImagesList();
				getImagesListTask.execute();
			}
		}



	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id)
		{
		case DATE_DIALOG_ID:
			final DatePickerDialog datePicker = new DatePickerDialog(this, mDateSetListener, dateYear, dateMonth,dateDay);
			datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancelBtn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					datePickerCancelled = true;
					/*if (which == DialogInterface.BUTTON_NEGATIVE) {
						finish();
					}*/
				}
			});
			datePicker.setTitle(R.string.selectDateMsg);
			datePicker.setCancelable(false);
			datePicker.setCanceledOnTouchOutside(false);
			return datePicker;
		}
		return null;
	}





	/* *****************************************
	 **************** Menu *******************
	 ***************************************** */

	private void setNavigationList() {
		//mSelected = (TextView)findViewById(R.id.navigation);
		categories = getResources().getStringArray(R.array.image_categories);
		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.image_categories, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*MenuItem setDateItem = menu.add("Set date");
		setDateItem.setIcon(android.R.drawable.ic_menu_today);
		setDateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		setDateId = setDateItem.getItemId();*/
		getSupportMenuInflater().inflate(R.menu.images_menu, menu);

		return super.onCreateOptionsMenu(menu);
		//return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.setDate:
			datePickerCancelled = false;
			//Toast.makeText(ImagesActivity.this, "datePicker", Toast.LENGTH_LONG).show();
			showDialog(DATE_DIALOG_ID);
			break;
		}
		Log.d("DEBUG", "id: " + item.getItemId());
		return(true);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (loadImages) {
			String objectRequested = categories[itemPosition];
			getImagesListByObjectTask = new GetImagesListByObject();
			getImagesListByObjectTask.execute(objectRequested);
		} else {
			loadImages = true;
		}
		return true;
	}



	/* *****************************************
	 ************ Async Tasks ****************
	 ***************************************** */

	private class GetImagesListByObject extends AsyncTask<String, Void, ArrayList<Integer>> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ImagesActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingImagesList));
			progressDialog.show();
		}

		@Override
		protected ArrayList<Integer> doInBackground(String... objects) {
			ArrayList<Integer> auxImageIdsList;
			String objectRequested = objects[0];
			if (objectRequested.compareTo("All") == 0) {
				auxImageIdsList = apiOperations.getImagesList();
			} else {
				auxImageIdsList = apiOperations.getImagesList(objectRequested);
			}

			return auxImageIdsList;
		}


		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(ArrayList<Integer> auxImageIdsList) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {}
				progressDialog = null;
			}	

			if (!isCancelled()) {
				resetValues();
				if (auxImageIdsList.size() > 0){
					imageIdsList = auxImageIdsList;
					nextImage(null);
				} else {
					Toast.makeText(getApplicationContext(), R.string.noImagesMessage, Toast.LENGTH_SHORT).show();
					findViewById(R.id.noImageLayout).setVisibility(View.VISIBLE);
				}
			}
			synchronized (taskLock) {
				getImagesListByObjectTask = null;
			}
		}
	}


	private class GetImagesList extends AsyncTask<Void, Void, ArrayList<Integer>> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ImagesActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingImagesList));
			progressDialog.show();
		}

		@Override
		protected ArrayList<Integer> doInBackground(Void... voids) {
			ArrayList<Integer> auxImageIdsList = apiOperations.getImagesList(dateYear, dateMonth +1, dateDay);

			return auxImageIdsList;
		}


		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(ArrayList<Integer> auxImageIdsList) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {}
				progressDialog = null;
			}	

			if (!isCancelled()) {
				resetValues();
				getSupportActionBar().setSelectedNavigationItem(0);
				loadImages = false;
				if (auxImageIdsList.size() > 0){
					imageIdsList = auxImageIdsList;
					nextImage(null);
				} else {
					Toast.makeText(getApplicationContext(), R.string.noImagesMessage, Toast.LENGTH_SHORT).show();
					findViewById(R.id.noImageLayout).setVisibility(View.VISIBLE);
				}
			}
			synchronized (taskLock) {
				getImagesListTask = null;
			}
		}
	}


	private class GetNewImage extends AsyncTask<Void, Void, Image> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ImagesActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.gettingImage));
			progressDialog.show();
		}

		@Override
		protected Image doInBackground(Void... voids) {
			Image image = apiOperations.getImage(imageIdsList.get(imagesListCursor));

			return image;
		}


		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Image imageGot) {
			if (progressDialog != null && progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
				} catch (Exception ex) {}
				progressDialog = null;
			}	

			if (!isCancelled()) {
				if (imageGot != null && imageGot.getBitmap() != null) {
					image = imageGot;
				} else {
					image = getImageWithError(imageGot.getCreationDate());
					Toast.makeText(ImagesActivity.this, R.string.errorGettingImage, Toast.LENGTH_LONG).show();
				}
				applyImageChanges();
			}
			synchronized (taskLock) {
				getImageTask = null;
			}
		}

	}


	private Image getImageWithError(Date creationDate) {
		Image imageWithError;
		Bitmap errorImage = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image_big);
		imageWithError = new Image(errorImage, creationDate);
		return imageWithError;
	}

	public void nextImage (View view){
		if (imagesListCursor+1 < imageIdsList.size()) {
			imagesListCursor++;
			getImageTask = new GetNewImage();
			getImageTask.execute();
		} else {
			Toast.makeText(getApplicationContext(), R.string.noMoreImagesMessage, Toast.LENGTH_SHORT).show();
		}
	}

	public void previousImage (View view){
		if (imagesListCursor > 0) {
			imagesListCursor--;
			getImageTask = new GetNewImage();
			getImageTask.execute();
		} else {
			Toast.makeText(getApplicationContext(), R.string.noPreviousImagesMessage, Toast.LENGTH_SHORT).show();
		}
	}


	private void applyImageChanges() {
		Date date = this.image.getCreationDate();
		Calendar calendar = Calendar.getInstance();  
		calendar.setTime(date);  
		dateYear = calendar.get(Calendar.YEAR);
		dateMonth = calendar.get(Calendar.MONTH);
		dateDay = calendar.get(Calendar.DAY_OF_MONTH);
		updateDateDisplay();
		// TODO navigation list
		recycleCurrentImage();
		imageTouchView.setImageBitmap(this.image.getBitmap());
		imageTouchView.requestLayout();
		imageTouchView.invalidate();
		findViewById(R.id.noImageLayout).setVisibility(View.INVISIBLE);
	}


	private void resetValues(){
		//recycleCurrentImage();
		setTitle(getString(R.string.images_activity_label));
		// TODO navigationList
		imagesListCursor = -1;
	}

	private void recycleCurrentImage() {
		BitmapDrawable oldDrawable = (BitmapDrawable)imageTouchView.getDrawable();
		if (oldDrawable != null && oldDrawable.getBitmap() != null)
			oldDrawable.getBitmap().recycle();
		imageTouchView.requestLayout();
	}
}
