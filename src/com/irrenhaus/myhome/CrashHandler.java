package com.irrenhaus.myhome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

// (C) by http://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	String VersionName;
	String PackageName;
	String FilePath;
	String PhoneModel;
	String AndroidVersion;
	String Board;
	String Brand;
	// String CPU_ABI;
	String Device;
	String Display;
	String FingerPrint;
	String Host;
	String ID;
	String Manufacturer;
	String Model;
	String Product;
	String Tags;
	long Time;
	String Type;
	String User;

	private Thread.UncaughtExceptionHandler PreviousHandler;
	private static CrashHandler S_mInstance;
	private Context CurContext;

	public void Init(Context context) {
		PreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		RecoltInformations(context);
		CurContext = context;
	}

	public long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	void RecoltInformations(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi;
			// Version
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			VersionName = pi.versionName;
			// Package name
			PackageName = pi.packageName;
			// Files dir for storing the stack traces
			FilePath = context.getFilesDir().getAbsolutePath();
			// Device model
			PhoneModel = android.os.Build.MODEL;
			// Android version
			AndroidVersion = android.os.Build.VERSION.RELEASE;

			Board = android.os.Build.BOARD;
			Brand = android.os.Build.BRAND;
			// CPU_ABI = android.os.Build.;
			Device = android.os.Build.DEVICE;
			Display = android.os.Build.DISPLAY;
			FingerPrint = android.os.Build.FINGERPRINT;
			Host = android.os.Build.HOST;
			ID = android.os.Build.ID;
			// Manufacturer = android.os.Build.;
			Model = android.os.Build.MODEL;
			Product = android.os.Build.PRODUCT;
			Tags = android.os.Build.TAGS;
			Time = android.os.Build.TIME;
			Type = android.os.Build.TYPE;
			User = android.os.Build.USER;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String CreateInformationString() {
		String ReturnVal = "";
		ReturnVal += "Version : " + VersionName;
		ReturnVal += "\n";
		ReturnVal += "Package : " + PackageName;
		ReturnVal += "\n";
		ReturnVal += "FilePath : " + FilePath;
		ReturnVal += "\n";
		ReturnVal += "Phone Model" + PhoneModel;
		ReturnVal += "\n";
		ReturnVal += "Android Version : " + AndroidVersion;
		ReturnVal += "\n";
		ReturnVal += "Board : " + Board;
		ReturnVal += "\n";
		ReturnVal += "Brand : " + Brand;
		ReturnVal += "\n";
		ReturnVal += "Device : " + Device;
		ReturnVal += "\n";
		ReturnVal += "Display : " + Display;
		ReturnVal += "\n";
		ReturnVal += "Finger Print : " + FingerPrint;
		ReturnVal += "\n";
		ReturnVal += "Host : " + Host;
		ReturnVal += "\n";
		ReturnVal += "ID : " + ID;
		ReturnVal += "\n";
		ReturnVal += "Model : " + Model;
		ReturnVal += "\n";
		ReturnVal += "Product : " + Product;
		ReturnVal += "\n";
		ReturnVal += "Tags : " + Tags;
		ReturnVal += "\n";
		ReturnVal += "Time : " + Time;
		ReturnVal += "\n";
		ReturnVal += "Type : " + Type;
		ReturnVal += "\n";
		ReturnVal += "User : " + User;
		ReturnVal += "\n";
		ReturnVal += "Total Internal memory : " + getTotalInternalMemorySize();
		ReturnVal += "\n";
		ReturnVal += "Available Internal memory : "
				+ getAvailableInternalMemorySize();
		ReturnVal += "\n";

		return ReturnVal;
	}

	public void uncaughtException(Thread t, Throwable e) {
		String Report = "";
		Date CurDate = new Date();
		Report += "Error Report collected on : " + CurDate.toString();
		Report += "\n";
		Report += "\n";
		Report += "Informations :";
		Report += "\n";
		Report += "==============";
		Report += "\n";
		Report += "\n";
		Report += CreateInformationString();

		Report += "\n\n";
		Report += "Stack : \n";
		Report += "======= \n";
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		Report += stacktrace;

		Report += "\n";
		Report += "Cause : \n";
		Report += "======= \n";

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable cause = e.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			Report += result.toString();
			cause = cause.getCause();
		}
		printWriter.close();
		Report += "****  End of current Report ***";
		SaveAsFile(Report);
		// SendErrorMail( Report );
		PreviousHandler.uncaughtException(t, e);
	}

	static CrashHandler getInstance() {
		if (S_mInstance == null)
			S_mInstance = new CrashHandler();
		return S_mInstance;
	}

	private void SendErrorMail(Context _context, String ErrorContent) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		String subject = _context.getResources().getString(
				R.string.CrashReport_MailSubject);
		String body = _context.getResources().getString(
				R.string.CrashReport_MailBody)
				+ "\n\n" + ErrorContent + "\n\n";
		sendIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "nphesse+myhome@gmail.com" });
		sendIntent.putExtra(Intent.EXTRA_TEXT, body);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		sendIntent.setType("message/rfc822");
		_context.startActivity(Intent.createChooser(sendIntent, "Title:"));
	}

	private void SaveAsFile(String ErrorContent) {
		try {
			Random generator = new Random();
			int random = generator.nextInt(99999);
			String FileName = "stack-" + random + ".stacktrace";
			FileOutputStream trace = CurContext.openFileOutput(FileName,
					Context.MODE_PRIVATE);
			trace.write(ErrorContent.getBytes());
			trace.close();
		} catch (IOException ioe) {
			// ...
		}
	}

	private String[] GetErrorFileList() {
		File dir = new File(FilePath + "/");
		// Try to create the files folder if it doesn't exist
		dir.mkdir();
		// Filter for ".stacktrace" files
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".stacktrace");
			}
		};
		return dir.list(filter);
	}

	private boolean bIsThereAnyErrorFile() {
		return GetErrorFileList().length > 0;
	}

	public void CheckErrorAndSendMail(final Context _context) {
		try {
			if (bIsThereAnyErrorFile()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(_context);
				
				builder.setTitle(R.string.fatal_crash_title);
				builder.setMessage(R.string.fatal_crash_message);
				
				builder.setPositiveButton(R.string.dialog_button_ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String WholeErrorText = "";
						try {
							String[] ErrorFileList = GetErrorFileList();
							int curIndex = 0;
							// We limit the number of crash reports to send ( in order not
							// to be too slow )
							final int MaxSendMail = 5;
							for (String curString : ErrorFileList) {
								if (curIndex++ <= MaxSendMail) {
									WholeErrorText += "New Trace collected :\n";
									WholeErrorText += "=====================\n ";
									String filePath = FilePath + "/" + curString;
									BufferedReader input;
									input = new BufferedReader(new FileReader(filePath));
									String line;
									while ((line = input.readLine()) != null) {
										WholeErrorText += line + "\n";
									}
									input.close();
								}
	
								// DELETE FILES !!!!
								File curFile = new File(FilePath + "/" + curString);
								curFile.delete();
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						SendErrorMail(_context, WholeErrorText);
						dialog.cancel();
					}
				});
				
				builder.setNegativeButton(R.string.dialog_button_cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] ErrorFileList = GetErrorFileList();
						for (String curString : ErrorFileList) {
							File curFile = new File(FilePath + "/" + curString);
							curFile.delete();
						}
						dialog.cancel();
					}
				});
				
				builder.create().show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}