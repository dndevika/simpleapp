package controller;

import com.android.ddmlib.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kitty on 5/7/2014.
 */
public class InternalEmulatorInformation {

    private String emulatorName;
    private String userName;
    private int version;
    private int memory;
    private String avdName;

    private Object lockObject = new Object();
    private boolean running = false;
    private Process process = null;
    private int consolePort;
    private String emulatorPath;

    public InternalEmulatorInformation(String name, String avdName, int nextPort, int version, int memory) {
        this.userName = name;
        this.emulatorName = "emulator-" + nextPort;
        this.consolePort = nextPort;
        this.version = version;
        this.memory = memory;
        this.avdName = avdName;
        this.emulatorPath = System.getenv("EMULATOR_PATH");
    }

    public void startEmulator() throws IOException {
        synchronized (lockObject) {
            if (!running) {
            	System.out.println(emulatorPath);
                process = Runtime.getRuntime().exec(emulatorPath+"emulator -avd " + avdName + " -noaudio -port " + consolePort);
                running = true;
            }
        }
    }

    public void stopEmulator() throws IOException {
        if (running) {
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
            if (d.getSerialNumber().equals(emulatorName)) {
                RawImage rawImage = null;
                try {
                    rawImage = d.getScreenshot();
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
}
