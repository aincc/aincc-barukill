package com.baru.barukill.ui.controls;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.baru.barukill.R;

/**
 * 
 * <h3><b>LoadingDialog</b></h3></br>
 * 
 * @author aincc@barusoft.com
 * @version 1.0.0
 * @since 1.0.0
 */
public class LoadingDialog extends Dialog
{
	/**
	 * 텍스트뷰
	 * 
	 * @since 1.0.0
	 */
	private TextView					textview;

	/**
	 * 레이아웃 파라미터
	 * 
	 * @since 1.0.0
	 */
	private WindowManager.LayoutParams	param;

	/**
	 * 
	 * @since 1.0.0
	 * @param context
	 */
	private LoadingDialog(Context context)
	{
		super(context);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		param = new WindowManager.LayoutParams();
		param.copyFrom(getWindow().getAttributes());
		param.width = WindowManager.LayoutParams.MATCH_PARENT;
		param.height = WindowManager.LayoutParams.MATCH_PARENT;

		View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
		textview = (TextView) view.findViewById(R.id.loading_message);
		setContentView(view, param);
	}

	/**
	 * 
	 * @since 1.0.0
	 * @param context
	 * @param message
	 * @param indeterminate
	 * @param cancelable
	 * @param cancelListener
	 * @param black
	 * @return the LoadingDialog
	 */
	public static LoadingDialog show(Context context, CharSequence message, boolean indeterminate, boolean cancelable, DialogInterface.OnCancelListener cancelListener, boolean black)
	{
		LoadingDialog dialog = new LoadingDialog(context);

		dialog.textview.setText(message);
		dialog.setTitle("");
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.show();

		return dialog;
	}

	/**
	 * 
	 * @since 1.0.0
	 * @param context
	 * @param message
	 * @return the LoadingDialog
	 */
	public static LoadingDialog show(Context context, CharSequence message)
	{
		return show(context, message, false);
	}

	/**
	 * 
	 * @since 1.0.0
	 * @param context
	 * @param message
	 * @param indeterminate
	 * @return the LoadingDialog
	 */
	public static LoadingDialog show(Context context, CharSequence message, boolean indeterminate)
	{
		return show(context, message, indeterminate, false, null, false);
	}

	/**
	 * 
	 * @since 1.0.0
	 * @param context
	 * @param message
	 * @param indeterminate
	 * @param cancelable
	 * @return the LoadingDialog
	 */
	public static LoadingDialog show(Context context, CharSequence message, boolean indeterminate, boolean cancelable)
	{
		return show(context, message, indeterminate, cancelable, null, false);
	}
}
