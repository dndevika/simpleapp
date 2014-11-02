package result;

/**
 * Created by Kitty on 5/8/2014.
 */

public class ActionResult {
    private boolean success = true;
    private String errorMessage = "";

    public ActionResult() {

    }

    public ActionResult(Exception e) {
        success = false;
        errorMessage = e.getMessage();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

