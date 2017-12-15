package interfaces;

import remm.sharedtrip.MainActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;

/**
 * Created by Mark on 14.12.2017.
 */

public interface UserModelHolder {
    FbGoogleUserModel getLoggedInUser();
    String getSerializedLoggedInUserModel();
    int getLoggedInUserId();
}
