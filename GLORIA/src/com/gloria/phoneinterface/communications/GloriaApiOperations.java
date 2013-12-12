package com.gloria.phoneinterface.communications;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magick.util.MagickBitmap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.gloria.phoneinterface.structures.Image;
import com.gloria.phoneinterface.structures.Plan;
import com.gloria.phoneinterface.structures.Telescope;


public class GloriaApiOperations {

	private static final String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String TEMP_IMAGE_PATH = CACHE_PATH + "/newImage.jpg";

	// private static final String SERVER_URL = "https://venus.datsi.fi.upm.es:8443/GLORIAAPI/";
	private static final String SERVER_URL = "https://ws.users.gloria-project.eu:8443/";
	private static final String USERS = "GLORIAAPI/users/";
	private static final String TELESCOPES = "GLORIAAPI/telescopes/";
	private static final String IMAGES = "GLORIAAPI/images/";
	private static final String SCHEDULER = "GLORIAAPI/scheduler/";

	public static final String OP_TELESCOPES_LIST = SERVER_URL + TELESCOPES + "list";
	public static final String OP_TELESCOPE_INFO = SERVER_URL + TELESCOPES + "%s";
	public static final String OP_IMAGES_LIST = SERVER_URL + IMAGES + "list";
	public static final String OP_IMAGES_LIST_BY_DATE = SERVER_URL + IMAGES + "list/%s/%s/%s";
	public static final String OP_IMAGES_LIST_BY_OBJECT = SERVER_URL + IMAGES + "list/object/%s";
	public static final String OP_IMAGE_INFO = SERVER_URL + IMAGES + "%s";
	public static final String OP_PLANS_ACTIVE = SERVER_URL + SCHEDULER + "plans/active";
	public static final String OP_PLANS_INACTIVE = SERVER_URL + SCHEDULER + "plans/inactive";
	public static final String OP_PLAN_INFO = SERVER_URL + SCHEDULER + "plans/%s";
	public static final String OP_PLAN_REQUEST = SERVER_URL + SCHEDULER + "plans/request";
	public static final String OP_AUTHENTICATION = SERVER_URL + USERS + "authenticate";


	private String authorizationToken = "";
	private GloriaApiProxy apiProxy = new GloriaApiProxy();




	public GloriaApiOperations(String authorizationToken) {
		super();
		this.authorizationToken = authorizationToken;
	}








	public ArrayList<String> getTelescopesNames() {
		ArrayList<String> telescopesArray = new ArrayList<String>();
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(OP_TELESCOPES_LIST, authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET telescopes, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			JSONArray telescopes = new JSONArray(respStr);
			Log.d("DEBUG", "telescopes: " + telescopes.toString());
			for (int i=0; i < telescopes.length(); i++) {
				String telescope = telescopes.optString(i);
				telescopesArray.add(telescope);
			}

		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return telescopesArray;
	}

	public Telescope getTelescopeInfo(String telescopeName, Bitmap telescopeImage) {
		Telescope telescope = new Telescope(telescopeName, telescopeImage);
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(String.format(OP_TELESCOPE_INFO, telescopeName), authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET telescope info, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONObject jsonTelescope = new JSONObject(respStr);
			telescope.setPartner(jsonTelescope.optString("owner"));


		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return telescope;
	}

	public ArrayList<Integer> getImagesList(int year, int monthOfYear, int dayOfMonth) {
		Log.d("DEBUG", "URL images: " + String.format(OP_IMAGES_LIST_BY_DATE, year, monthOfYear, dayOfMonth));
		ArrayList<Integer> imageIds = new ArrayList<Integer>();
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(
					String.format(OP_IMAGES_LIST_BY_DATE, year, monthOfYear, dayOfMonth), 
					authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET image ids, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONArray imageIdsJsonArray = new JSONArray(respStr);
			for (int i=0; i < imageIdsJsonArray.length(); i++){
				Integer id = Integer.valueOf(imageIdsJsonArray.getInt(i));
				imageIds.add(id);
			}


		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return imageIds;
	}


	public ArrayList<Integer> getImagesList(String object) {
		ArrayList<Integer> imageIds = new ArrayList<Integer>();
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(
					String.format(OP_IMAGES_LIST_BY_OBJECT, object), 
					authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET image ids, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONArray imageIdsJsonArray = new JSONArray(respStr);
			for (int i=0; i < imageIdsJsonArray.length(); i++){
				Integer id = Integer.valueOf(imageIdsJsonArray.getInt(i));
				imageIds.add(id);
			}


		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return imageIds;
	}


	public ArrayList<Integer> getImagesList() {
		ArrayList<Integer> imageIds = new ArrayList<Integer>();
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(String.format(OP_IMAGES_LIST), authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET image ids, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONArray imageIdsJsonArray = new JSONArray(respStr);
			for (int i=0; i < imageIdsJsonArray.length(); i++){
				Integer id = Integer.valueOf(imageIdsJsonArray.getInt(i));
				imageIds.add(id);
			}


		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return imageIds;
	}



	public Image getImage(int imageId) {
		Image image = null;
		Log.d("DEBUG", "image id: " + imageId);
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(String.format(OP_IMAGE_INFO, imageId), authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET image, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			//Log.d("DEBUG", "response: " + respStr);
			JSONObject imageJson = new JSONObject(respStr);

			String url = imageJson.optString("jpg");
			long dateMs = imageJson.optLong("creationDate");
			Log.d("DEBUG", "URL: " + url + "\nms: " + dateMs);

			Bitmap bitmap = loadImageFromUrl(url, TEMP_IMAGE_PATH);
			Date date = new Date(dateMs);
			image = new Image(bitmap, date);

		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return image;
	}


	/*private Bitmap loadImageFromUrl (String url){
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false; // ask the bitmap factory not to scale the loaded bitmaps
		//opts.inJustDecodeBounds = true;
		Log.d("DEBUG", "width: " + opts.outWidth + "\theight" + opts.outHeight);
		try {
			URL urlObject = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			bitmap = BitmapFactory.decodeStream(input,null,opts);
			Log.d("DEBUG", "width: " + opts.outWidth + "\theight" + opts.outHeight);
		} 
		catch (IOException e) {}
		return bitmap;
	}*/


	private Bitmap loadImageFromUrl (String url, String cachePath){
		Bitmap bitmap = null;
		boolean okResult = downloadFile(url, cachePath); 
		if (okResult){
			try {
				ImageInfo info = new ImageInfo(cachePath); // where the image is
				MagickImage image = new MagickImage(info);
				if (image.getColorspace() == ColorspaceType.CMYKColorspace) {
					Log.d("DEBUG", "ColorSpace BEFORE => " + image.getColorspace());
					boolean status = image.transformRgbImage(ColorspaceType.CMYKColorspace);
					Log.d("DEBUG", "ColorSpace AFTER => " + image.getColorspace() + ", success = " + status);
					bitmap = MagickBitmap.ToBitmap(image);
				}
				else {
					bitmap = BitmapFactory.decodeFile(cachePath);
				}
			} catch (MagickException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}


	private boolean downloadFile (String urlString, String savingPath) {
		boolean okResult = false;
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			File file = new File(savingPath); // save file here
			FileOutputStream fileOutput = new FileOutputStream(file);
			InputStream inputStream = urlConnection.getInputStream();
			byte[] buffer = new byte[1024];
			int bufferLength = 0; //used to store a temporary size of the buffer
			while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
				fileOutput.write(buffer, 0, bufferLength);
			}
			fileOutput.close();
			okResult = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		return okResult;
	}



	public ArrayList<Plan> getPlansInactive() {
		ArrayList<Plan> inactivePlans = new ArrayList<Plan>();
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(OP_PLANS_INACTIVE, authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET inactive plans, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONArray plansJson = new JSONArray(respStr);

			for (int i = 0; i < plansJson.length(); i++) {
				JSONObject planJson = plansJson.optJSONObject(i);
				JSONObject opInfoJson = planJson.optJSONObject("opInfo");
				String description = opInfoJson.optString("description");
				Plan plan = new Plan(description);
				plan.setObjectName(opInfoJson.optString("object"));
				plan.setStatus(planJson.optString("state"));
				plan.setId(planJson.optInt("id"));
				inactivePlans.add(plan);
			}


		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return inactivePlans;
	}


	public Plan getPlanInfo(int planId){
		Plan plan = null; 
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(String.format(OP_PLAN_INFO, planId), authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET plan info, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONObject planJson = new JSONObject(respStr);
			JSONObject opInfoJson = planJson.optJSONObject("opInfo");

			String description = opInfoJson.optString("description");
			plan = new Plan(description);
			plan.setId(planJson.optInt("id"));
			plan.setObjectName(opInfoJson.optString("object"));
			plan.setStatus(planJson.optString("state"));
			plan.setRa(opInfoJson.optString("ra"));
			plan.setDec(opInfoJson.optString("dec"));
			plan.setExposure(opInfoJson.optDouble("exposure"));
			plan.setFilter(opInfoJson.optString("filter"));

			ArrayList<Bitmap> imagesArray = new ArrayList<Bitmap>();
			JSONArray resultsJson = planJson.optJSONArray("results");
			if (resultsJson != null) {
				for (int i = 0; i < resultsJson.length(); i++) {
					JSONObject resultJson = resultsJson.optJSONObject(i);
					JSONArray imagesJSON = resultJson.optJSONArray("images");
					for (int j = 0; j < imagesJSON.length(); j++) {
						JSONObject imageJSON = imagesJSON.optJSONObject(j);
						String format = imageJSON.optString("format");
						if (format != null && format.compareTo("JPG") == 0){
							String url = imageJSON.optString("url");
							Log.d("RESULTS", url);
							Bitmap bm = loadImageFromUrl(url, TEMP_IMAGE_PATH);
							if (bm != null)
								imagesArray.add(bm);
							else 
								Log.d("RESULTS", "Image could not be loaded");
						}
					}
				}
			}
			plan.setResults(imagesArray);

		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return plan;
	}





	public ArrayList<Plan> getPlansActive() {
		ArrayList<Plan> activePlans = new ArrayList<Plan>();
		HttpClient httpClient = null;
		try {
			httpClient = apiProxy.getHttpClient();
			HttpGet getRequest = apiProxy.getHttpGetRequest(OP_PLANS_ACTIVE, authorizationToken); 
			HttpResponse resp = httpClient.execute(getRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "GET active plans, status code: " + statusCode);
			if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
				Log.d("DEBUG", "Unauthorized, token: " + authorizationToken);
			} else if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

			String respStr = EntityUtils.toString(resp.getEntity());
			Log.d("DEBUG", "response: " + respStr);
			JSONArray plansJson = new JSONArray(respStr);

			for (int i = 0; i < plansJson.length(); i++) {
				JSONObject planJson = plansJson.optJSONObject(i);
				JSONObject opInfoJson = planJson.optJSONObject("opInfo");
				String description = opInfoJson.optString("description");
				Plan plan = new Plan(description);
				plan.setObjectName(opInfoJson.optString("object"));
				plan.setStatus(planJson.optString("state"));
				plan.setId(planJson.optInt("id"));
				activePlans.add(plan);
			}

		} catch (IllegalStateException e) {
			Log.d("DEBUG", "Connection is not open");
			Log.d("DEBUG", httpClient.toString());
			Log.d("DEBUG", httpClient.getConnectionManager().toString());
		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
		return activePlans;
	}




	public void requestPlan(Plan plan) {
		try {
			HttpClient httpClient = apiProxy.getHttpClient();
			JSONObject planResquestJson = new JSONObject();
			planResquestJson.put("description", plan.getDescription());
			planResquestJson.put("moonAltitude", plan.getMoonAltitude());
			planResquestJson.put("moonDistance", plan.getMoonDistance());
			planResquestJson.put("targetAltitude", plan.getTargetAltitude());
			planResquestJson.put("object", plan.getObjectName());
			planResquestJson.put("filter", plan.getFilter());
			planResquestJson.put("exposure", plan.getExposure());

			Log.d("DEBUG", "Send results: " + planResquestJson.toString());
			StringEntity entity = new StringEntity(planResquestJson.toString());
			HttpPost postRequest = apiProxy.getHttpPostRequest(OP_PLAN_REQUEST, authorizationToken, entity); 
			HttpResponse resp = httpClient.execute(postRequest);

			int statusCode = resp.getStatusLine().getStatusCode();
			Log.d("DEBUG", "POST plan request, status code: " + statusCode);
			if (statusCode != HttpURLConnection.HTTP_OK) { // handle any errors, like 404, 500,..
				Log.d("DEBUG", "Unknown error");
			}

		} catch (ClientProtocolException e) {
			Log.d("DEBUG", "Client protocol exception");
		} catch (IOException e) {
			Log.d("DEBUG", "IO exception");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.d("DEBUG", "JSON exception");
		}  finally {
			apiProxy.shutdown();
		}
	}
}


