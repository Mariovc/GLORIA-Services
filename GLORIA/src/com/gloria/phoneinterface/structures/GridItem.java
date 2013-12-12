package com.gloria.phoneinterface.structures;

import android.graphics.Bitmap;


public class GridItem {

	private Bitmap image;
	private String title;
	private String statusText = null;
	private Bitmap statusImage = null;



	public GridItem(Bitmap image, String title) {
		this.image = image;
		this.title = title;
	}

	public GridItem(Bitmap image, String title, String statusText,
			Bitmap statusImage) {
		super();
		this.image = image;
		this.title = title;
		this.statusText = statusText;
		this.statusImage = statusImage;
	}
	
	

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public Bitmap getStatusImage() {
		return statusImage;
	}

	public void setStatusImage(Bitmap statusImage) {
		this.statusImage = statusImage;
	}
}
