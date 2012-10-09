package com.baru.barukill.common;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;

import com.baru.barukill.common.anno.InjectView;
import com.baru.barukill.ui.controls.LoadingDialog;
import com.baru.barukill.util.CLogger;

/**
 * 
 * <h3><b>BaseActivity</b></h3></br>
 * 
 * @author aincc@barusoft.com
 * @version 1.0.0
 * @since 1.0.0
 */
abstract public class BaseActivity extends Activity
{
	/**
	 * invalid identifier
	 * 
	 * @since 1.0.0
	 */
	private static final int	INVALID_IDENTIFIER	= 0;

	/**
	 * Common Loading Dialog
	 * 
	 * @since 1.0.0
	 */
	private LoadingDialog		loadingDialog;

	/**
	 * UI Identifier Mapping
	 * 
	 * @since 1.0.0
	 * @param object
	 */
	private static void mappingViews(Object object)
	{
		if (!(object instanceof Activity))
		{
			return;
		}

		Activity activity = (Activity) object;

		// get member fields
		Field[] fields = activity.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			// get InjectView annotation information
			InjectView injectView = field.getAnnotation(InjectView.class);
			if (null == injectView)
			{
				continue;
			}

			// get identifier
			int identifier = injectView.id();
			if (INVALID_IDENTIFIER == identifier)
			{
				String identifierString = field.getName();
				identifier = activity.getResources().getIdentifier(identifierString, "id", activity.getPackageName());
			}

			if (INVALID_IDENTIFIER == identifier)
			{
				continue;
			}

			View view = activity.findViewById(identifier);
			if (null == view)
			{
				continue;
			}

			// bind view
			if (field.getType() == view.getClass())
			{
				try
				{
					field.setAccessible(true);
					field.set(object, view);
				}
				catch (IllegalArgumentException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}

			CLogger.i(field.getName() + "," + identifier + "," + view);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(layoutResID);

		// mapping view using by reflection and annotation
		mappingViews(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	/**
	 * 로딩다이얼로그 표시하기
	 * 
	 * @since 1.0.0
	 */
	public void startProgress(final String message, final boolean cancelable, final OnCancelListener l)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				loadingDialog = LoadingDialog.show(BaseActivity.this, (null != message) ? message : "", false, cancelable, l, false);
			}
		});
	}

	/**
	 * 로딩다이얼로그 취소하기
	 * 
	 * @since 1.0.0
	 */
	public void stopProgress()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					loadingDialog.cancel();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					loadingDialog = null;
				}
			}
		});
	}
}
