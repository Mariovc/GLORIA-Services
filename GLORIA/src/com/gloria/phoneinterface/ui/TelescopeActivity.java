package com.gloria.phoneinterface.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.communications.GloriaApiOperations;
import com.gloria.phoneinterface.structures.Telescope;

public class TelescopeActivity extends SherlockActivity{

	
	private Telescope telescope = null;
	private String authorizationToken = "";
	private GloriaApiOperations apiOperations = null;

	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.telescope);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		Bundle extras = getIntent().getExtras();
		this.authorizationToken = extras.getString("authorizationToken");
		apiOperations = new GloriaApiOperations(authorizationToken);
		String telescopeName = extras.getString("telescopeName");
		Bitmap telescopeImage = extras.getParcelable("telescopeImage");
		if (telescopeName != null && telescopeImage != null)
			setTelescopeInfo(telescopeName, telescopeImage);
	}

	private void setTelescopeInfo(String telescopeName, Bitmap telescopeImage) {
		this.telescope = apiOperations.getTelescopeInfo(telescopeName, telescopeImage);
		
		if (this.telescope != null)
			setInfoViews();
	}

	
	private void setInfoViews() {
		((TextView) findViewById(R.id.nameField)).setText(telescope.getName());
		getSupportActionBar().setTitle(telescope.getName());
		ImageView imageView = (ImageView) findViewById(R.id.telescopeImage);
		imageView.setImageBitmap(telescope.getImage());
		Drawable imageDrawable = new BitmapDrawable(getResources(),telescope.getImage());
		getSupportActionBar().setIcon(imageDrawable);
		
		if (telescope.getPartner() != null)
			((TextView) findViewById(R.id.partnerValueField)).setText(telescope.getPartner());
		else 
			findViewById(R.id.partnerFieldContainer).setVisibility(View.GONE);

		if (telescope.getLocation() != null)
			((TextView) findViewById(R.id.locationValueField)).setText(telescope.getLocation());
		else 
			findViewById(R.id.locationFieldContainer).setVisibility(View.GONE);

		if (telescope.getCoordinates() != null){
			PointF coordinates = telescope.getCoordinates();
			String coordinatesString = getString(R.string.telescope_longitude) + ": " + coordinates.x + " " + getString(R.string.telescope_latitude) + ": " + coordinates.y;
			((TextView) findViewById(R.id.coordinatesValueField)).setText(coordinatesString);
		}else 
			findViewById(R.id.coordinatesFieldContainer).setVisibility(View.GONE);

		if (telescope.getStartingDate() != null)
			((TextView) findViewById(R.id.startingDateValueField)).setText(telescope.getStartingDate());
		else 
			findViewById(R.id.startingDateFieldContainer).setVisibility(View.GONE);

		if (telescope.getFilters() != null)
			((TextView) findViewById(R.id.filtersValueField)).setText(telescope.getFilters());
		else 
			findViewById(R.id.filtersFieldContainer).setVisibility(View.GONE);

		if (telescope.getAperture() != null)
			((TextView) findViewById(R.id.apertureValueField)).setText(telescope.getAperture());
		else 
			findViewById(R.id.apertureFieldContainer).setVisibility(View.GONE);

		if (telescope.getFocalLength() != null)
			((TextView) findViewById(R.id.focalLengthValueField)).setText(telescope.getFocalLength());
		else 
			findViewById(R.id.focalLengthFieldContainer).setVisibility(View.GONE);

		if (telescope.getWebsite() != null) {
			TextView websiteTextView = (TextView) findViewById(R.id.websiteValueField);
			websiteTextView.setText(telescope.getWebsite());
			websiteTextView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		} else 
			findViewById(R.id.websiteFieldContainer).setVisibility(View.GONE);
		
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return(true);
	}


	


	public void websiteLink(View view) {
		if (telescope.getWebsite() != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(telescope.getWebsite()));
			startActivity(intent);
		} 
	}
}
