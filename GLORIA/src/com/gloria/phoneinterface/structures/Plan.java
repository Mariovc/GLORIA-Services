package com.gloria.phoneinterface.structures;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Plan {
	
	private int id = -1;
	private String description = "";
	private String objectName = "";
	private String objectCommonName = "";
	private String status = "";
	private String ra = null;
	private String dec = null;
	private Double moonAltitude = null;
	private Double moonDistance = null;
	private Double targetAltitude = null;
	private Double exposure = null;
	private String filter = null;
	private Bitmap image = null;
	private ArrayList<Bitmap> results = null;
	
	
	

	





	public Plan(String description) {
		super();
		this.description = description;
	}


	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}


	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}


	public String getObjectCommonName() {
		return objectCommonName;
	}
	public void setObjectCommonName(String objectCommonName) {
		this.objectCommonName = objectCommonName;
	}

	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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


	public Double getMoonAltitude() {
		return moonAltitude;
	}
	public void setMoonAltitude(Double moonAltitude) {
		this.moonAltitude = moonAltitude;
	}

	public Double getMoonDistance() {
		return moonDistance;
	}
	public void setMoonDistance(Double moonDistance) {
		this.moonDistance = moonDistance;
	}

	public Double getTargetAltitude() {
		return targetAltitude;
	}
	public void setTargetAltitude(Double targetAltitude) {
		this.targetAltitude = targetAltitude;
	}
	

	public Double getExposure() {
		return exposure;
	}
	public void setExposure(Double exposure) {
		this.exposure = exposure;
	}



	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	

	
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
	
	
	public ArrayList<Bitmap> getResults() {
		return results;
	}
	public void setResults(ArrayList<Bitmap> results) {
		this.results = results;
	}
}
