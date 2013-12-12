package com.gloria.phoneinterface.structures;

import java.util.Date;

import android.graphics.Bitmap;

public class Image {

	private Bitmap bitmap = null;
	private Date creationDate = null;
	
	

	public Image(Bitmap bitmap, Date creationDate) {
		super();
		this.bitmap = bitmap;
		this.creationDate = creationDate;
	}
	
	
	
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	
}
