package pl.edu.mimuw.chatnfc.tools;

public class OnlineProvider {

    public OnlineProvider() {
    }

    public static void setOnline() {
        if (FirebaseTools.getInstance().getCurrentUser() != null) {
            String uid = FirebaseTools.getInstance().getCurrentUser().getUid();
            FirebaseTools.getInstance().setValueInDB("Users/" + uid + "/online/", true);
        }
    }

    public static void setOffline() {
        if (FirebaseTools.getInstance().getCurrentUser() != null) {
            String uid = FirebaseTools.getInstance().getCurrentUser().getUid();
            FirebaseTools.getInstance().setValueInDB("Users/" + uid + "/online/", false);
            FirebaseTools.getInstance().setValueInDB("Users/" + uid + "/last_seen/", TimeProvider.getCurrentTimeMillisOrLocal(200));
        }
    }
}
