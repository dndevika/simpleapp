package controller;

/**
 * Created by Kitty on 5/7/2014.
 */
public class EmulatorInformation {

    private String name;
    private int memorySize;
    private int version;

    public EmulatorInformation(String name, int memorySize, int version){
        this.name = name;
        this.memorySize = memorySize;
        this.version = version;
    }


    public String getName() {
        return name;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public int getVersion() {
        return version;
    }
}
