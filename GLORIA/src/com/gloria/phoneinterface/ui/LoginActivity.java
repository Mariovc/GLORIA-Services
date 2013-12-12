/**
 * @author Mario Velasco Casquero
 * @version 2.00
 */

package com.gloria.phoneinterface.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.gloria.offlineexperiments.Base64;
import com.gloria.phoneinterface.R;
import com.gloria.phoneinterface.communications.GloriaApiOperations;
import com.gloria.phoneinterface.communications.GloriaApiProxy;
import com.gloria.phoneinterface.ui.fonts.TypefaceManager;

public class LoginActivity extends Activity {

	private String username = "";
	private String password = "";

	private Button btnLogin = null;

	private GloriaApiProxy apiProxy = new GloriaApiProxy();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		prepareWidgets();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadUser();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void launchInfo() {
		Intent i = new Intent(this, InfoActivity.class);
		startActivity(i);
	}

	public void authenticate(View view) {
		EditText usernameText = (EditText) findViewById(R.id.username);
		this.username = usernameText.getText().toString();
		EditText passwordText = (EditText) findViewById(R.id.password);
		this.password = passwordText.getText().toString();
		CheckBox checkbox = (CheckBox) findViewById(R.id.rememberMe);
		if (checkbox.isChecked())
			saveUser();
		new Authentication().execute();
	}

	private void prepareWidgets() {
		final Typeface typeface = TypefaceManager.INSTANCE.getTypeface(
				getApplicationContext(), TypefaceManager.VERDANA);
		TypefaceManager.INSTANCE.applyTypefaceToAllViews(this, typeface);

		btnLogin = (Button) findViewById(R.id.loginButton);
		final EditText etUsername = (EditText) findViewById(R.id.username);
		final EditText etPassword = (EditText) findViewById(R.id.password);
		final TextView tvRegister = (TextView) findViewById(R.id.registerTextView);
		tvRegister.setMovementMethod(LinkMovementMethod.getInstance());
		
		etUsername.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				username = (s == null || s.toString() == null) ? "" : s
						.toString();
				updateCompleteness();
			}
		});
		etPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				password = (s == null || s.toString() == null) ? "" : s
						.toString();
				updateCompleteness();
			}
		});
		etPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO && btnLogin.isEnabled()) {
					authenticate(btnLogin);
					return false;
				}
				return true;
			}
		});

		/*ImageView infoButton = (ImageView) findViewById(R.id.infoButton);
		infoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				launchInfo();
			}
		});*/
		
		updateCompleteness();
	}

	private void updateCompleteness() {
		btnLogin.setEnabled(username.trim().length() > 0
				&& password.trim().length() > 0);
	}

	private void saveUser() {
		SharedPreferences settings = getSharedPreferences("LoginGLORIA",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("username", this.username);
		editor.putString("password", this.password);
		editor.commit();
	}

	private void loadUser() {
		SharedPreferences settings = getSharedPreferences("LoginGLORIA",
				MODE_PRIVATE);
		String username = settings.getString("username", "");
		EditText usernameText = (EditText) findViewById(R.id.username);
		usernameText.setText(username);
		String password = settings.getString("password", "");
		EditText passwordText = (EditText) findViewById(R.id.password);
		passwordText.setText(password);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private class Authentication extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progressDialog;
		private String errorResponse = getString(R.string.defaultErrorMsg);
		private String authorizationToken = "";

		// This function is called at the beginning, before doInBackground
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setMessage(getString(R.string.authenticatingMsg));
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			boolean response = false;

			HttpClient httpClient = null;
			try {
				authorizationToken = apiProxy.getAuthorizationTokenFromUserPassword(username, password);
				// create connection
				httpClient = apiProxy.getHttpClient();
				HttpGet getRequest = apiProxy.getHttpGetRequest(
						GloriaApiOperations.OP_AUTHENTICATION, authorizationToken);
				if (getRequest == null) {
					errorResponse = getString(R.string.defaultErrorMsg);
					return false;
				}
				Log.d("DEBUG", "opening connection");
				HttpResponse resp = httpClient.execute(getRequest);
				int statusCode = resp.getStatusLine().getStatusCode();
				Log.d("DEBUG", "status code: " + statusCode);

				// handle issues
				if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					Log.d("DEBUG", "Invalid user or password");
					errorResponse = getString(R.string.wrongLoginMsg);
					return response;
				} else if (statusCode != HttpURLConnection.HTTP_OK) {
					// handle any other errors, like 404, 500,..
					Log.d("DEBUG", "Unknown error");
					errorResponse = getString(R.string.defaultErrorMsg);
					return response;
				} else {
					response = true;
				}

			} catch (MalformedURLException e) {
				Log.d("DEBUG", "URL is invalid");
			} catch (SocketTimeoutException e) {
				Log.d("DEBUG", "data retrieval or connection timed out");
			} catch (IOException e) {
				Log.d("DEBUG", "IO exception");
				errorResponse = getString(R.string.noInternetMsg);
				// could not read response body
				// (could not create input stream)
			} finally {
				if (httpClient != null) {
					httpClient.getConnectionManager().shutdown();
				}
			}

			publishProgress();
			return response;
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Boolean authenticated) {
			if (progressDialog.isShowing()) {
				progressDialog.cancel();
			}

			if (authenticated) {
				Intent intent = new Intent(LoginActivity.this,
						MainMenuActivity.class);
				intent.putExtra("authorizationToken", authorizationToken);

				startActivity(intent);
			} else {
				Toast.makeText(LoginActivity.this, errorResponse,
						Toast.LENGTH_LONG).show();
			}
		}

		

	}

}
