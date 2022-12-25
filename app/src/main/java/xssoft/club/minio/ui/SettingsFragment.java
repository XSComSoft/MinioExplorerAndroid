package xssoft.club.minio.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xssoft.club.minio.R;
import xssoft.club.minio.misc.IOnBackListener;


public class SettingsFragment extends Fragment implements IOnBackListener {

    private static SettingsFragment _instance;
    public static SettingsFragment Instance(){
        if(_instance == null){
            _instance = new SettingsFragment();
        }
        return _instance;
    }
    private SettingsFragment(){}
    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onPause() {
        onBackListener = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        onBackListener = this;
    }

    @Override
    public void  onHiddenChanged(boolean hidden){
        onBackListener = !hidden ? this : null;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean getInitialState() {
        return true;
    }

    private IOnBackListener onBackListener;
    @Override
    public IOnBackListener getOnBackListenerObject() {
        return onBackListener;
    }
}