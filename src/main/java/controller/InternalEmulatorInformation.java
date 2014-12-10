package controller;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.ddmlib.*;
import com.android.sdklib.internal.avd.AvdManager;
import com.mongodb.BasicDBObject;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kitty on 5/7/2014.
 */
public class InternalEmulatorInformation {



	private String userName;
	private int version;
	private int memory;
	private String avdName;
	private boolean running;

	private Object lockObject = new Object();

	private Process process = null;
	private Process apps = null;
	private int consolePort;
	private String emulatorPath;
	private String adbPath;
	private String aaptPath;
	private HashMap<String, String> installedApps;
	private static HashMap<String, String> appNames = new HashMap<String, String>();

	public InternalEmulatorInformation(String name, String avdName, int nextPort, int version, int memory) {
		this.userName = name;
		this.emulatorName = "emulator-" + nextPort;
		this.consolePort = nextPort;
		this.version = version;
		this.memory = memory;
		this.avdName = avdName;
		this.emulatorPath = System.getenv("EMULATOR_PATH");
		this.adbPath  = System.getenv("ADB_PATH");
		this.aaptPath = System.getenv("AAPT_PATH");
		this.running = false;
		installedApps = new HashMap<String, String>();
		//appNames = new ArrayList<String>();
	}

	public  HashMap<String, String> getInstalledApps() {
		return installedApps;
	}


	private String emulatorName;


	public void startEmulator() throws IOException {
		synchronized (lockObject) {
			if (!this.running) {
				System.out.println(emulatorPath);

				process = Runtime.getRuntime().exec(emulatorPath+"emulator -avd " + avdName + " -noaudio -port " + consolePort);
				this.running = true;

				//    			final PackageManager pm = getPackageManager();
				//    			//get a list of installed apps.
				//    			List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
				//
				//    			for (ApplicationInfo packageInfo : packages) {
				//    			    Log.d(TAG, "Installed package :" + packageInfo.packageName);
				//    			    Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
				//    			    Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName)); 
				//    			}	
			}
		}
	}

	public void stopEmulator(boolean emulatorStatus) throws IOException {
		if (emulatorStatus) {
			Socket socket = new Socket("localhost", consolePort);
			socket.setKeepAlive(true);
			BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			r.readLine();
			PrintWriter w = new PrintWriter(socket.getOutputStream(), true);
			w.write("kill\n");
			w.flush();
			w.write("kill\n");
			w.flush();
			socket.close();
			this.running = false;
		}
	}



	public synchronized String installApplication(InputStream input,String appname) throws InterruptedException {
		String launAct=null;
		String packageName=null, pack="";
		System.out.println("aaptPath "+aaptPath);
		for (IDevice d : AndroidDebugBridge.getBridge().getDevices()) {
			if (d.getSerialNumber().equals(emulatorName)) {

				try {
					Process pr;
					File tempFile = File.createTempFile("install", ".apk");
					FileOutputStream fs = new FileOutputStream(tempFile);
					byte[] buffer = new byte[1024]; // Adjust if you want
					int bytesRead;
					while ((bytesRead = input.read(buffer)) != -1) {
						fs.write(buffer, 0, bytesRead);
					}
					pr = Runtime.getRuntime().exec(aaptPath+"./aapt dump badging "+tempFile);
					pr.waitFor();
					BufferedReader reader = 
							new BufferedReader(new InputStreamReader(pr.getInputStream()));

					String line = "";			
					while ((line = reader.readLine())!= null) {
						if (line.contains("package")) {
							packageName = line.split("=")[1].split(" ")[0];
							pack = packageName.substring(1, packageName.length()-1);

						}
						if (line.contains("launchable")) {
							String line1 = line.split("=")[1].split(" ")[0];
							launAct = line1.substring(1, line1.length()-1);

						}
					}
					String path = pack+"/"+launAct;
					installedApps.put(appname,path);
					appNames.put(appname,path);
					
					
					fs.flush();
					fs.close();
					input.close();
					d.installPackage(tempFile.toString(), true);
					tempFile.delete();
					return path;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InstallException e) {
					e.printStackTrace();
					
				}
				break;
			}
			
		}
		return null;
	}

	public void launchApplication(String appName) {

		for (IDevice d : AndroidDebugBridge.getBridge().getDevices()) {
			if (d.getSerialNumber().equals(emulatorName)) {
				try {
					if(appNames.containsKey(appName)){ 
						String intentName = appNames.get(appName);
						d.executeShellCommand("am start -n " + intentName, new NullOutputReceiver(), 60, TimeUnit.SECONDS);
					}
				} catch (TimeoutException e) {
					e.printStackTrace();
				} catch (AdbCommandRejectedException e) {
					e.printStackTrace();
				} catch (ShellCommandUnresponsiveException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}

	}

	public void launchUrl(String url) {
		for (IDevice d : AndroidDebugBridge.getBridge().getDevices()) {
			if (d.getSerialNumber().equals(emulatorName)) {

				try {
					d.executeShellCommand("am start -a android.intent.action.VIEW -d '" + url.replace("&", "\\&") + "'", new NullOutputReceiver(), 60, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					e.printStackTrace();
				} catch (AdbCommandRejectedException e) {
					e.printStackTrace();
				} catch (ShellCommandUnresponsiveException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	public byte[] getScreenShot() {
		for (IDevice d : AndroidDebugBridge.getBridge().getDevices()) {
			System.out.println("device serial number "+ d.getSerialNumber() );
			if (d.getSerialNumber().equals(emulatorName)) {

				RawImage rawImage = null;
				try {
					rawImage = d.getScreenshot();
					System.out.println("raw image width"+ rawImage.width);
					BufferedImage image = new BufferedImage(rawImage.width, rawImage.height,
							BufferedImage.TYPE_INT_ARGB);
					int index = 0;
					int IndexInc = rawImage.bpp >> 3;
					for (int y = 0; y < rawImage.height; y++) {
						for (int x = 0; x < rawImage.width; x++) {
							int value = rawImage.getARGB(index);
							index += IndexInc;
							image.setRGB(x, y, value);
						}
					}
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(image, "png", os);
					return os.toByteArray();
				} catch (TimeoutException e) {
					e.printStackTrace();
				} catch (AdbCommandRejectedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return new byte[]{};
	}


	public String getEmulatorName() {
		return emulatorName;
	}

	public String getUserName() {
		return userName;
	}

	public int getVersion() {
		return version;
	}

	public int getMemory() {
		return memory;
	}

	public EmulatorInformation getInformation() {
		return new EmulatorInformation(userName, memory, version);
	}

	public Integer getPort() {
		return new Integer(consolePort);
	}

	public String getAvdName() {
		return avdName;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void retreiveAppInfo(String appName) throws IOException {
		//	 apps = Runtime.getRuntime().exec(emulatorPath+"adb shell pm list packages -f");
		//process.
		//         DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
		//         outputStream.writeBytes("screenrecord --time-limit 10 /sdcard/MyVideo.mp4\n");
		//         outputStream.flush();
		//         System.out.println("info "+outputStream.toString());
		//         
		//installedApps = new HashMap<String, String>();
		installedApps.put("QuickSearchBox","com.android.quicksearchbox/com.android.quicksearchbox.SearchActivity");
		installedApps.put("LatinIME","com.android.inputmethod.latin/com.android.inputmethod.latin.setup.SetupActivity");
		installedApps.put("Calculator","com.android.calculator2/com.android.calculator2.Calculator");
		installedApps.put("CustomLocale","com.android.customlocale2/com.android.customlocale2.CustomLocaleActivity");
		installedApps.put("Calendar","com.android.calendar/com.android.calendar.AllInOneActivity");
		installedApps.put("Browser","com.android.browser/com.android.browser.BrowserActivity");
		installedApps.put("Music","com.android.music/com.android.music.MusicBrowserActivity");
		installedApps.put("Gallery","com.android.gallery/com.android.camera.GalleryPicker");
		installedApps.put("SpeechRecorder","com.android.speechrecorder/com.android.speechrecorder.SpeechRecorderActivity");
		installedApps.put("LegacyCamera","com.android.camera/com.android.camera.Camera");
		installedApps.put("DeskClock","com.android.deskclock/com.android.deskclock.DeskClock");

		//StringBuffer output = new StringBuffer();
		if(installedApps.containsKey(appName)){ 
			String path = installedApps.get(appName);
			Process p;
			try {
				p = Runtime.getRuntime().exec(adbPath+"adb shell am start -n "+path);
				//			p.waitFor();
				//			BufferedReader reader = 
				//                            new BufferedReader(new InputStreamReader(p.getInputStream()));
				// 
				//                        String line = "";			
				//			while ((line = reader.readLine())!= null) {
				//				output.append(line + "\n");
				//				String full_apk = line.split(":")[1];
				//				String[] apks = full_apk.split(".apk")[0].split("/");
				//				String apk_name = apks[apks.length-1];
				//				System.out.println(apk_name + " , " + full_apk);
				//				

				//			}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}   
		//	final PackageManager pm;
		// 		//get a list of installed apps.
		// 		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		//
		// 		for (ApplicationInfo packageInfo : packages) {
		// 		    System.out.println("Installed package :" + packageInfo.packageName);
		// 		    System.out.println("Source dir : " + packageInfo.sourceDir);
		// 		    System.out.println("Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName)); 
		// 		}


	}
}
