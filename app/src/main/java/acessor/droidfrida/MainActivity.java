package acessor.droidfrida;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;
import android.widget.CheckBox;

public class MainActivity extends Activity {

	private HashMap<View, View> tabs;
	private AutoCompleteTextView pkgName;
	private TextView scriptInput;
	private TextView scriptOutput;
	private FixedSpinner jsOptionMenu;
	private ListView scriptsList;

	private Shell rootShell;  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tabs = new HashMap<View, View>();
		tabs.put(findViewById(R.id.codeTab), 
				 findViewById(R.id.editorTabPanel));
		tabs.put(findViewById(R.id.outputTab), 
				 findViewById(R.id.ouputTabPanel));
		tabs.put(findViewById(R.id.scriptsTab),
				 findViewById(R.id.scriptsTabPanel));
		tabs.put(findViewById(R.id.settingsTab),
				 findViewById(R.id.settingsTabPanel));
		pkgName = findViewById(R.id.pkgName);
		pkgName.setAdapter(getInstalledAppNames());
		scriptInput = findViewById(R.id.scriptInput);
		scriptInput.setHorizontallyScrolling(true);
		scriptOutput = findViewById(R.id.outputLog);
		scriptOutput.setHorizontallyScrolling(true);
		scriptOutput.setRawInputType(InputType.TYPE_NULL);
		scriptOutput.setTextIsSelectable(true);
		jsOptionMenu = findViewById(R.id.jsOptionMenu);
		jsOptionMenu.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, 
														 getResources().getStringArray(R.array.js_options)));
		jsOptionMenu.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {
					showDialog("Warning", "Code processing features are currently very unstable and shouldn't be used.", false);
					/*String minified = JSMinifier.minify(scriptInput.getText().toString());
					 switch (jsOptionMenu.getItemAtPosition(myPosition).toString()) {
					 case "Format code":
					 scriptInput.setText(JSFormatter.formatJSCode(minified));
					 break;
					 case "Minify code":
					 scriptInput.setText(minified);
					 break;
					 case "Obfuscate code":
					 scriptInput.setText(JSConfuser.obfuscate(minified));
					 break;
					 }*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { }

			});

		scriptsList = findViewById(R.id.scriptsList);
		scriptsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String assetName = scriptsList.getItemAtPosition(position).toString();
					if (assetExists(assetName)) {
						scriptInput.setText(readAsset(assetName));
						showToast(assetName + " loaded!");
					}
				}
			});
		scriptsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, 
														getResources().getStringArray(R.array.offline_scripts)));
		String initScriptPath = "/data/data/" + getPackageName() + "/script.js";
		if (new File(initScriptPath).exists()) {
			try {
				scriptInput.setText(readTextFile(initScriptPath));
			}
			catch (Exception e) {
				showToast(e.getMessage());
			}
		}

		if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
			String basePath = "/data/data/" + getPackageName().toString();
			try {
				File fridaBin = new File(basePath + "/frida64");
				if (!fridaBin.exists())
					extractAsset("frida64.zip", fridaBin.getPath());
				if (!fridaBin.canExecute())
					rootShell.runRootCommand(new Command(0, new String[]{"chmod +x " + fridaBin.getPath()}));
			}
			catch (Exception e) {
				showDialog("Error", e.getMessage(), false);
			}
		}
		else {
			showDialog("Error", "You need root access in order to use DroidFrida", true);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			switch (requestCode) {
				case 1:
					try {
						InputStream is = getContentResolver().openInputStream(uri);
						byte[] buffer = new byte[is.available()];
						is.read(buffer, 0, buffer.length);
						scriptInput.setText(new String(buffer));
					}
					catch (Exception e) {
						showToast("Failed to read script: " + e.getMessage());
					}
					break;
				case 2:
					try {
						OutputStream outputStream = getContentResolver().openOutputStream(uri);
						outputStream.write(scriptInput.getText().toString().getBytes());
						outputStream.close();
					}
					catch (Exception e) {
						showToast("Failed to save script: " + e.getMessage());
					}
					break;
			}
		}
	}

	@Override
	public void onStop() {
		try {
			writeTextFile("/data/data/" + getPackageName() + "/script.js", scriptInput.getText().toString());
		}
		catch (Throwable e) {
			showToast(e.getMessage());
		}
		super.onStop();
	}

	public void onJsOptionClicked(View view) {
		jsOptionMenu.performClick();
	}

	public void onOpenScriptClicked(View view) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/javascript");
		startActivityForResult(
			Intent.createChooser(intent, "Select File"), 1);
	}

	public void onSaveScriptClicked(View view) {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, 2);
	}

	public void tabClicked(View v) {
		for (Map.Entry<View, View> tab : tabs.entrySet()) {
			View value = tab.getValue(), 
				key = tab.getKey();
			if (key == v) {
				value.setVisibility(View.VISIBLE);
				key.setBackground(getResources().getDrawable(R.drawable.border_a));
			}
			else {
				value.setVisibility(View.GONE);
				key.setBackground(null);
			}
		}
	}

	public void launchClicked(View v) {
		try {
			scriptOutput.setText(null);
			if (rootShell == null || !rootShell.isShellOpen())
				rootShell = RootShell.getShell(true);
			String dataFolder = "/data/data/" + getPackageName();
			writeTextFile(dataFolder + "/script.js", scriptInput.getText().toString());
			showToast("Injecting to the target application...");
			rootShell.runRootCommand(new Command(0, new String[]{"killall -v " + pkgName })); 
			rootShell.runRootCommand(new Command(1, new String[]{dataFolder + "/frida64 -s " + dataFolder + 
													 "/script.js -f " + pkgName.getText()}) {
					@Override
					public void commandOutput(int id, String line) {
						scriptOutput.append(line + "\n");
						showToast(line);
					}
				});
		}
		catch (Exception e) {
			showToast(e.getMessage());
		}
	}

	public void onWrapSettingClicked(View v) {
		scriptInput.setHorizontallyScrolling(((CheckBox)v).isChecked());
	}

	public  void showDialog(String title, String msg, final boolean exit) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(msg)
			.setCancelable(false)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					if (exit)
						finishAffinity();
				}
			})
			.show();
	}

	public void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}

	public ArrayAdapter<String> getInstalledAppNames() {
		Context context = getApplicationContext();
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		List<String> appNames = new ArrayList<>();
		for (ApplicationInfo applicationInfo : applicationInfos) {
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				appNames.add(applicationInfo.packageName);
			}
		}
		return new ArrayAdapter<>(
			context, 
			R.layout.item, 
			appNames
		);
	}

	public String readAsset(String assetName) {
		String content = null;
		try {
			InputStream is = getApplicationContext()
				.getAssets().open(assetName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			content = new String(buffer);
		}
		catch (IOException ex) {
			showToast(ex.getMessage());
		}
		return content;
	}

	public boolean assetExists(String assetName) {
		try {
			getApplicationContext().getAssets().open(assetName);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	public void writeTextFile(String path, String text) throws IOException {
		File file = new File(path);
		FileWriter writer = new FileWriter(file);
		try {
			writer.write(text);
		}
		finally {
			writer.close();
		}
	}

	public String readTextFile(String path) throws IOException {
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder stringBuilder = new StringBuilder();
	    try {
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		}
		finally {
			reader.close();
		}
		return stringBuilder.toString();
	}


	public void extractAsset(String assetName, String outputFilePath) throws IOException {
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		InflaterOutputStream infOutput = null;
		try {
			Inflater inflater = new Inflater(true);
			inputStream = getAssets().open(assetName);
			fileOutputStream = new FileOutputStream(outputFilePath);
			infOutput = new InflaterOutputStream(fileOutputStream, inflater);
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) > 0) {
				infOutput.write(buffer, 0, bytesRead);
			}
		}
		catch (IOException e) {
			showToast("Error extracting asset: " + e.getMessage());
		}
		finally {
			if (fileOutputStream != null) 
				fileOutputStream.close();
			if (inputStream != null) 
				inputStream.close();
			if (infOutput != null) {
				infOutput.close();
			}
		}
	}

	/*

	 // Just for testing if everything is decompressed fine

	 public static void decompressFile(String inputFilePath, String outputFilePath) throws IOException {
	 FileInputStream fileInputStream = new FileInputStream(inputFilePath);
	 FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
	 Inflater inflater = new Inflater(true);
	 InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(fileOutputStream, inflater);
	 byte[] buffer = new byte[1024];
	 int bytesRead;
	 while ((bytesRead = fileInputStream.read(buffer)) > 0) {
	 inflaterOutputStream.write(buffer, 0, bytesRead);
	 }
	 fileInputStream.close();
	 inflaterOutputStream.finish();
	 inflaterOutputStream.close();
	 }

	 // Used this only to compress frida and pack inlt inside assets

	 public static void compressFile(String inputFilePath, String outputFilePath) throws IOException {
	 FileInputStream fileInputStream = new FileInputStream(inputFilePath);
	 FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
	 Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
	 DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream, deflater);
	 byte[] buffer = new byte[1024];
	 int bytesRead;
	 while ((bytesRead = fileInputStream.read(buffer)) > 0) {
	 deflaterOutputStream.write(buffer, 0, bytesRead);
	 }
	 fileInputStream.close();
	 deflaterOutputStream.finish();
	 deflaterOutputStream.close();
	 }

	 */
}
	  
	  
	 
