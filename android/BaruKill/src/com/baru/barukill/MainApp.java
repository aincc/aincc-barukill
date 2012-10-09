package com.baru.barukill;

import android.app.Application;

import com.baru.barukill.util.CLogger;

/**
 * 
 * <h3><b>MainApp</b></h3></br>
 * 
 * @author aincc@barusoft.com
 * @version 1.0.0
 * @since 1.0.0
 */
public class MainApp extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		CLogger.i("MainApp::onCreate()");
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		CLogger.i("MainApp::onLowMemory()");
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		CLogger.i("MainApp::onConfigurationChanged()");
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		CLogger.i("MainApp::onTerminate()");
	}
}
