package com.gloria.phoneinterface.structures;

import android.graphics.Bitmap;

public class Star {

	private String name = null;
	private String longName = null;
	private String description = null;
	private String ra = null;
	private String dec = null;
	private Bitmap image = null;
	
	
	
	
	
	public Star(String name) {
		super();
		this.name = name;
	}
	public Star(String name, String longName, String description, String ra, String dec,
			Bitmap image) {
		super();
		this.name = name;
		this.longName = longName;
		this.description = description;
		this.ra = ra;
		this.dec = dec;
		this.image = image;
	}
	
	
	
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLongName() {
		return longName;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRa() {
		return ra;
	}
	public void setRa(String ra) {
		this.ra = ra;
	}
	public String getDec() {
		return dec;
	}
	public void setDec(String dec) {
		this.dec = dec;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
}
