package controller;

import controller.EmulatorInformation;

/**
 * Created by Kitty on 5/8/2014.
 */
public class Status {
    private long totalMemory;
    private long freeMemory;
    private int emulatorCount;
    private int maxSupported;
    private EmulatorInformation[] emulatorInformation;

    public Status(long totalMemory,
                  long freeMemory,
                  int emulatorCount,
                  int maxSupported,
                  EmulatorInformation[] emulatorInformation) {
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
        this.emulatorCount = emulatorCount;
        this.maxSupported = maxSupported;
        this.emulatorInformation = emulatorInformation;

    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public int getEmulatorCount() {
        return emulatorCount;
    }

    public int getMaxSupported() {
        return maxSupported;
    }

    public EmulatorInformation[] getEmulatorInformation() {
        return emulatorInformation;
    }
}

