package controller;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.prefs.AndroidLocation;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.repository.descriptors.IdDisplay;
import com.android.utils.StdLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Kitty on 5/7/2014.
 */
public class AndroidEmulatorManager {

	private static SdkManager sdkManager;
	private static AvdManager avdManager;
	private static Object lockObject = new Object();

	private static final int MAX_COUNT = 5;
	private static final int MIN_PORT = 5554;
	private static final int MAX_PORT = 5584;
	private static int count = 0;
	private static HashSet<Integer> usedPorts;
	private static HashMap<String, InternalEmulatorInformation> emulators;
	private static AndroidDebugBridge debugBridge;
	private static Random random = new Random();

	public synchronized static void inititalize()
			throws AndroidLocation.AndroidLocationException {
		try {
			System.out.println(System.getenv("ANDROID_HOME"));
			sdkManager = SdkManager.createManager(System.getenv("ANDROID_HOME"),
					new StdLogger(StdLogger.Level.VERBOSE));
			System.out.println("sdk mgr "+sdkManager);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
		
		//System.out.println("and em init "+ sdkManager);
		avdManager = AvdManager.getInstance(sdkManager.getLocalSdk(),
				new StdLogger(StdLogger.Level.VERBOSE));
		
		//deleteAllAvd();
		
		AndroidDebugBridge.init(false);
		File f = new File(System.getenv("ANDROID_HOME"),
				"platform-tools/adb");
		String fPath = f.getAbsolutePath();
		System.out.println(fPath);
		AndroidDebugBridge.createBridge(fPath, false);

		usedPorts = new HashSet<Integer>();
		emulators = new HashMap<String, InternalEmulatorInformation>();
		
	}

	private static int getNextPort() {
		for (int i = MIN_PORT; i <= MAX_PORT; i += 2) {
			if (usedPorts.contains(new Integer(i))) {
				continue;
			}
			usedPorts.add(new Integer(i));
			return i;
		}
		return 5554;
	}

	private synchronized static void terminateAll() {
		try {
			for(InternalEmulatorInformation i : emulators.values()){
				i.stopEmulator();
			}
			Runtime.getRuntime().exec("taskkill -9 /im emulator.exe /f ");
			Runtime.getRuntime().exec("taskkill -9 /im emulator-arm.exe /f ");
			Runtime.getRuntime().exec("taskkill -9 /im emulator-x86.exe /f ");
			Runtime.getRuntime().exec("taskkill -9 /im emulator-mips.exe /f ");
			usedPorts.clear();
			emulators.clear();
			count = 0;
		} catch (Exception ex) {

		}
	}

	private static long[] getMemoryInfo() throws Exception {
		Process p = Runtime
				.getRuntime()
				.exec("wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /Format:Textvaluelist");
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String s = reader.readLine();
		long max = 0;
		long free = 0;
		while (s != null) {
			if (s.startsWith("FreePhysicalMemory=")) {
				free = Long.parseLong(s.replace("FreePhysicalMemory=", ""));
			} else if (s.startsWith("TotalVisibleMemorySize=")) {
				max = Long.parseLong(s.replace("TotalVisibleMemorySize=", ""));
			}

			if (max != 0 && free != 0) {
				break;
			}
			s = reader.readLine();
		}
		return new long[] { max, free };
	}

	public static Status getStatus() throws Exception {
		ArrayList<EmulatorInformation> emulatorInformation = new ArrayList<EmulatorInformation>();
		for (InternalEmulatorInformation e : emulators.values()) {
			emulatorInformation.add(e.getInformation());
		}
		long[] memInfo = getMemoryInfo();
		return new Status(memInfo[0], memInfo[1], count, MAX_COUNT,
				emulatorInformation.toArray(new EmulatorInformation[] {}));

	}

	public static InternalEmulatorInformation getEmulator(String id)
			throws Exception {
		if (emulators.containsKey(id)) {
			System.out.println("emulator id " +emulators.get(id) );
			return emulators.get(id);
		} else {
			throw new Exception("Invalid emulator id");
		}
	}

	private synchronized static void deleteAllAvd() {
		AvdInfo[] avds = avdManager.getAllAvds();
		for (AvdInfo info : avds) {
			avdManager.deleteAvd(info, new StdLogger(StdLogger.Level.VERBOSE));
		}
	}

	private synchronized static void deleteAvd(String avd) {
		AvdInfo info = avdManager.getAvd(avd, false);
		if (info != null) {
			avdManager.deleteAvd(info, new StdLogger(StdLogger.Level.VERBOSE));
		}
	}

	public static void closeEmulator(String id) {
		InternalEmulatorInformation info = emulators.get(id);
		if (info != null) {
			try {
				info.stopEmulator();
			} catch (Exception ex) {

			}
			synchronized (lockObject) {
				count--;
				emulators.remove(id);
				usedPorts.remove(info.getPort());
			}
		}
	}

//	public synchronized static EmulatorInformation createAvd(int version,
//			int memory) throws Exception {
	public synchronized static String createAvd(int version,
			int memory) throws Exception {
		if (count >= MAX_COUNT) {
			System.out.println("Too many emulators: "+count);
			throw new EmulatorNodeException.InsufficientResourcesException();
		}
//		long[] memInfo = getMemoryInfo();
//		
//		if (memInfo[1] < memory * 1024) {
//			throw new EmulatorNodeException.InsufficientResourcesException();
//		}
		String avdName = Integer.toString(random.nextInt(99999));
		
		AndroidVersion andVersion = null;
		andVersion = new AndroidVersion( Integer.toString(version) );
		String hashString = AndroidTargetHash.getPlatformHashString(andVersion);
		System.out.println(andVersion + "," + hashString +"," + sdkManager);
		//IAndroidTarget target = sdkManager.getTargetFromHashString("android-" + version);
		IAndroidTarget target = sdkManager.getTargetFromHashString(hashString);
		
		System.out.println(target);
		if (target == null) {
			throw new EmulatorNodeException.UnsupportedAndroidVersionException(
					version);
		}
		String id = crateAvdInfo(avdName, target, memory);
		InternalEmulatorInformation information = new InternalEmulatorInformation(
				id, avdName, getNextPort(), version, memory);
//		try {
//			System.out.println("starting emulator");
//			information.startEmulator();
//		} catch (Exception ex) {
//			synchronized (lockObject) {
//				usedPorts.remove(information.getPort());
//			}
//			throw ex;
//		}
		count++;
		emulators.put(id, information);
		return id;
	//	return information.getInformation();
	}
	
	public synchronized static void startEmulatorWithId(String emulatorId) throws Exception {
		InternalEmulatorInformation Emulatorinfo = getEmulator(emulatorId);
		try {
		System.out.println("starting emulator");
		Emulatorinfo.startEmulator();
	} catch (Exception ex) {
		synchronized (lockObject) {
			usedPorts.remove(Emulatorinfo.getPort());
		}
		throw ex;
	}
		
	}

	public static void terminate() {
		terminateAll();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {

		}
		deleteAllAvd();
	}

	private static String crateAvdInfo(String avdName, IAndroidTarget target,
			int memory) throws Exception {
		String name = UUID.randomUUID().toString();
		HashMap<String, String> myConfig = new HashMap<String, String>();
		myConfig.put("hw.camera.back", "none");
		myConfig.put("hw.audioInput", "no");
		myConfig.put("hw.ramSize", Integer.toString(memory));
		String abiType = target.getSystemImages()[0].getAbiType();
		for (ISystemImage s : target.getSystemImages()) {
			if (s.getAbiType().contains("arm")) {
				abiType = s.getAbiType();
				break;
			}
		}
		File path = new File(System.getenv("AVD_PATH"), avdName);
		System.out.println("avdname"+avdName + ",avd_path " + System.getenv("AVD_PATH") + ", " + path + ",abitype " + abiType);
	//	AvdInfo f = avdManager.createAvd(path, avdName, target, null, abiType, null, null, abiType, myConfig, myConfig, false, false, false, new StdLogger(
	//					StdLogger.Level.VERBOSE));
		AvdInfo f = avdManager.createAvd(path, avdName, target, new IdDisplay("default", "Default"), abiType, null, null, memory + "M",
				myConfig, null, false, false, false, new StdLogger(
						StdLogger.Level.VERBOSE));
		if (f == null) {
			throw new Exception("Unable to create AVD");
		}
		return name;
	}
}
