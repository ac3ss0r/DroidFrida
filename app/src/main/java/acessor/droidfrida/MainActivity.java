package acessor.droidfrida;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class MainActivity extends Activity {

  private HashMap<View, View> tabs = new HashMap<View, View>();
  private AutoCompleteTextView pkgName;
  private TextView scriptInput;
  private TextView scriptOutput;
  private JSpinner jsOptionMenu;
  private Shell rootShell;  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	scriptInput = findViewById(R.id.scriptInput);
	String initScriptPath = "/data/data/" + getPackageName() + "/script.js";
	if (new File(initScriptPath).exists()) {
	  try {
		scriptInput.setText(readTextFile(initScriptPath));
	  } catch (Exception e) {
		showToast(e.getMessage());
	  }
	}
	scriptOutput = findViewById(R.id.outputLog);
	jsOptionMenu = findViewById(R.id.jsOptionMenu);
	jsOptionMenu.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, 
													 getResources().getStringArray(R.array.js_options)));
	jsOptionMenu.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {
		  String minified = JSMinifier.minify(scriptInput.getText().toString());

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
		  }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {

        }

	  });
	scriptInput.setHorizontallyScrolling(true);
	scriptInput.addTextChangedListener(new TextWatcher() {

	    @Override
		public void afterTextChanged(Editable s) {} 

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override    
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		  //showToast(s.toString() + " " + count);
		}
	  });
	scriptOutput.setHorizontallyScrolling(true);
	scriptOutput.setRawInputType(InputType.TYPE_NULL);
	scriptOutput.setTextIsSelectable(true);

	ListView scripts = findViewById(R.id.scriptsList);
	scripts.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, 
												getResources().getStringArray(R.array.offline_scripts)));

	tabs.put(findViewById(R.id.codeTab), findViewById(R.id.editorTabPanel));
	tabs.put(findViewById(R.id.outputTab), findViewById(R.id.ouputTabPanel));
	tabs.put(findViewById(R.id.scriptsTab), findViewById(R.id.scriptsTabPanel));
	tabs.put(findViewById(R.id.settingsTab), findViewById(R.id.settingsTabPanel));

	pkgName = findViewById(R.id.pkgName);
	pkgName.setAdapter(getInstalledAppNames());
	if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
	  String basePath = "/data/data/" + getPackageName().toString();
	  try {
		File fridaBin = new File(basePath + "/frida64");
		if (!fridaBin.exists())
		  extractAsset("frida64.zip", fridaBin.getPath());
		if (!fridaBin.canExecute())
		  rootShell.runRootCommand(new Command(0, new String[]{"chmod +x " + fridaBin.getPath()}));
	  } catch (Exception e) {
		ShowDialog("Error", e.getMessage(), false);
	  }
	} else {
	  ShowDialog("Error", "You need root access in order to use DroidFrida", true);
	}
  }

  @Override
  public void onStop() {
	try {
	  writeTextFile("/data/data/" + getPackageName() + "/script.js", scriptInput.getText().toString());
	} catch (Throwable e) {
	  showToast(e.getMessage());
	}
	super.onStop();
  }

  public void tabClicked(View v) {
	for (Map.Entry<View, View> tab : tabs.entrySet()) {
	  View value = tab.getValue(), 
		key = tab.getKey();
	  if (key == v) {
		value.setVisibility(View.VISIBLE);
		key.setBackground(getResources().getDrawable(R.drawable.bordered_view));
	  } else {
		value.setVisibility(View.GONE);
		key.setBackground(null);
	  }
	}
  }

  public void launchClicked(View v) {
	try {
	  scriptOutput.setText("");
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


	} catch (Exception e) {
	  showToast(e.getMessage());
	}
  }

  public void jsOptsClicked(View view) {
	this.jsOptionMenu.performClick();
  }

  public  void ShowDialog(String title, String msg, final boolean exit) {
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

  public void writeTextFile(String path, String text) throws IOException {
	File file = new File(path);
	FileWriter writer = new FileWriter(file);
	writer.write(text);
	writer.close();
  }

  public String readTextFile(String path) throws IOException {
	File file = new File(path);
	BufferedReader reader = new BufferedReader(new FileReader(file));
	StringBuilder stringBuilder = new StringBuilder();
	String line;
	while ((line = reader.readLine()) != null) {
	  stringBuilder.append(line + "\n");
	}
	reader.close();
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
	} catch (IOException e) {
	  showToast("Error extracting asset: " + e.getMessage());
	} finally {
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
	  
	  
	 
