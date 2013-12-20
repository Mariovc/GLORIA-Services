package com.gloria.phoneinterface.structures;

import android.graphics.Bitmap;
import android.graphics.PointF;

public class Telescope {

	
	
	private Bitmap image = null;
	private String name = null;
	private String partner = null;
	private String location = null;
	private PointF coordinates = null;
	private String startingDate = null;
	private String filters = null;
	private String aperture = null;
	private String focalLength = null;
	private String website = null;
	
	
	

	public Telescope(String name, Bitmap image) {
		super();
		this.name = name;
		this.image = image;
	}
	
	
	
	
	
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public PointF getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(Float longitude, Float latitude) {
		this.coordinates.x = longitude;
		this.coordinates.y = latitude;
	}
	public String getStartingDate() {
		return startingDate;
	}
	public void setStartingDate(String startingDate) {
		this.startingDate = startingDate;
	}
	public String getFilters() {
		return filters;
	}
	public void setFilters(String filters) {
		this.filters = filters;
	}
	public String getAperture() {
		return aperture;
	}
	public void setAperture(String aperture) {
		this.aperture = aperture;
	}
	public String getFocalLength() {
		return focalLength;
	}
	public void setFocalLength(String focalLength) {
		this.focalLength = focalLength;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	
	
}
