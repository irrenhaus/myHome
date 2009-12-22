package com.irrenhaus.myhome;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler {
	private myHome context;
	
	private String	stackTrace;
	
	public CrashHandler(myHome context)
	{
		this.context = context;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        stackTrace = result.toString();
        printWriter.close();
        
        showDialog();
	}
	
	private void showDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		String title = context.getResources().getString(R.string.fatal_crash_title);
		String msg = context.getResources().getString(R.string.fatal_crash_message);
		String btn = context.getResources().getString(R.string.dialog_button_ok);
		
		builder.setTitle(title);
		builder.setMessage(msg+"\n\n"+stackTrace);
		builder.setPositiveButton(btn, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				context.finish();
			}
		});
		
		Log.d("myHome", msg+"\n\n"+stackTrace);
		
		builder.create().show();
		context.finish();
	}
}
