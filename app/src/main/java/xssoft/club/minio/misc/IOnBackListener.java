package xssoft.club.minio.misc;

public interface IOnBackListener {
    void onBackPressed();
    boolean getInitialState();
    IOnBackListener getOnBackListenerObject();
}
