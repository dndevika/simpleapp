package controller;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.ddmlib.*;
import com.android.sdklib.internal.avd.AvdManager;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
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
    private static HashMap<String, String> installedApps;

    public InternalEmulatorInformation(String name, String avdName, int nextPort, int version, int memory) {
        this.userName = name;
        this.emulatorName = "emulator-" + nextPort;
        this.consolePort = nextPort;
        this.version = version;
        this.memory = memory;
        this.avdName = avdName;
        this.emulatorPath = System.getenv("EMULATOR_PATH");
        this.adbPath  = System.getenv("ADB_PATH");
        this.running = false;
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

   

	public synchronized void installApplication(InputStream input) {
        for (IDevice d : AndroidDebugBridge.getBridge().getDevices()) {
            if (d.getSerialNumber().equals(emulatorName)) {
         
                try {
                    File tempFile = File.createTempFile("install", ".apk");
                    FileOutputStream fs = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[1024]; // Adjust if you want
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        fs.write(buffer, 0, bytesRead);
                    }
                    fs.flush();
                    fs.close();
                    input.close();
                    d.installPackage(tempFile.toString(), true);
                    tempFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InstallException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void launchApplication(String intentName) {

        for (IDevice d : AndroidDebugBridge.getBridge().getDevices()) {
            if (d.getSerialNumber().equals(emulatorName)) {
                try {
                    d.executeShellCommand("am start -n " + intentName, new NullOutputReceiver(), 60, TimeUnit.SECONDS);
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

	public void retreiveAppInfo() throws IOException {
	//	 apps = Runtime.getRuntime().exec(emulatorPath+"adb shell pm list packages -f");
		//process.
//         DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
//         outputStream.writeBytes("screenrecord --time-limit 10 /sdcard/MyVideo.mp4\n");
//         outputStream.flush();
//         System.out.println("info "+outputStream.toString());
//         
		StringBuffer output = new StringBuffer();
		 
		Process p;
		try {
			p = Runtime.getRuntime().exec(adbPath+"adb shell pm list packages -f");
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
				String full_apk = line.split(":")[1];
				String[] apks = full_apk.split(".apk")[0].split("/");
				String apk_name = apks[apks.length-1];
				System.out.println(apk_name + " , " + full_apk);
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		System.out.println(output.toString());
         
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
