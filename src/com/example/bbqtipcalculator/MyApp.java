package com.example.bbqtipcalculator;

import android.app.Application;
import android.content.Context;

/**
 * Provides a way to access certain top level application state.
 * 
 * @author Ji jiwpark90
 */
public class MyApp extends Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		MyApp.context = getApplicationContext();	
	}

	/**
	 * Provides a static way to access the application Context.
	 *
	 * @return the application Context.
	 */
	public static Context getContext() {
		return MyApp.context;
	}
}