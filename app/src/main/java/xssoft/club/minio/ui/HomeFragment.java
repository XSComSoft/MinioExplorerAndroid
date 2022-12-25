package xssoft.club.minio.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.dmoral.toasty.Toasty;
import im.delight.android.webview.AdvancedWebView;
import xssoft.club.minio.R;
import xssoft.club.minio.misc.IOnBackListener;
import xssoft.club.minio.misc.PermissionsInfo;

public class HomeFragment extends Fragment implements IOnBackListener {

    private static HomeFragment _instance;

    public static  HomeFragment Instance(){
        if(_instance == null){
            _instance = new HomeFragment();
        }
        return _instance;
    }

    private HomeFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        Button btnPermissions = view.findViewById(R.id.checkPermissions);
        AdvancedWebView webView = view.findViewById(R.id.webView);

        webView.loadUrl("https://xssoft.club");

        btnPermissions.setOnClickListener(view1 -> {
            PermissionsInfo pInfo = new PermissionsInfo();
            Toasty.info(view1.getContext(),getString(R.string.permission_add) + pInfo.IsOnPermissionsGranted(),Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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