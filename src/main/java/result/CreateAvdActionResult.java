package result;

import controller.EmulatorInformation;

/**
 * Created by Kitty on 5/8/2014.
 */
public class CreateAvdActionResult extends ActionResult {
    private EmulatorInformation emulatorInformation;

    public CreateAvdActionResult(EmulatorInformation e) {
        this.emulatorInformation = e;
    }

    public  CreateAvdActionResult(Exception e){
        super(e);
    }

    public EmulatorInformation getEmulatorInformation() {
        return emulatorInformation;
    }
}
