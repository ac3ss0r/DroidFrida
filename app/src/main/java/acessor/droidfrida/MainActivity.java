package acessor.droidfrida;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;
import java.io.File;
import java.io.FileOutputStream;
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
	private TextView scriptInput;
	private TextView scriptOutput;
	private AutoCompleteTextView pkgName;
	private Shell rootShell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		scriptInput = findViewById(R.id.scriptInput);
		scriptOutput = findViewById(R.id.outputLog);
		tabs.put(findViewById(R.id.codeTab), findViewById(R.id.scriptInput));
		tabs.put(findViewById(R.id.outputTab), findViewById(R.id.outputLog));
		pkgName = findViewById(R.id.pkgName);
		pkgName.setAdapter(getInstalledAppNames());
		if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
			String basePath = "/data/data/" + getPackageName().toString();
			try {
				File fridaBin = new File(basePath + "/frida64");
				if (!fridaBin.exists())
					extractAsset("frida64.zip", fridaBin.getPath());
				if (!fridaBin.canExecute())
					rootShell.runRootCommand(new Command(0, "chmod +x " + fridaBin.getPath()));
			} catch (Exception e) {
				showToast(e.toString());
			}
		} else {
			ShowDialog("Error", "Your device should be rooted in order to use DroidFrida.", true);
		}
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
			scriptOutput.setText(null); 
			if (rootShell == null || !rootShell.isShellOpen())
				rootShell = RootShell.getShell(true);
			String dataFolder = "/data/data/" + getPackageName();
			writeToFile(dataFolder + "/script.js", scriptInput.getText().toString());
			showToast("Injecting to the target application...");
			rootShell.runRootCommand(new Command(0, dataFolder + "/frida64 -s " + dataFolder + 
												 "/script.js -f " + pkgName.getText()) {
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

	public void writeToFile(String path, String text) throws IOException {
		File file = new File(path);
		FileWriter writer = new FileWriter(file);
		writer.write(text);
		writer.close();
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
