package controller;

/**
 * Created by Kitty on 5/7/2014.
 */
public class EmulatorNodeException {
    public static class InsufficientResourcesException extends Exception {
        public InsufficientResourcesException() {
            super("Not enough resources to start the emulator");
        }
    }

    public static  class UnsupportedAndroidVersionException extends Exception{
        public UnsupportedAndroidVersionException(int version){
            super("Android version " + version + "  not found.");
        }
    }
}
