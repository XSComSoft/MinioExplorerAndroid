package xssoft.club.minio;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import es.dmoral.toasty.Toasty;
import xssoft.club.minio.misc.PermissionsInfo;
import xssoft.club.minio.ui.AboutFragment;
import xssoft.club.minio.ui.DevInfoFragment;
import xssoft.club.minio.ui.HomeFragment;
import xssoft.club.minio.ui.SettingsFragment;
import xssoft.club.minio.ui.TabView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private AlertDialog.Builder alertDialogBuider;

    public BottomAppBar bottomAppBar;
    public BottomNavigationView bottomNavigationView;
    public NavigationView appNavigationView;
    public AppCompatImageButton appMenuButton;
    public DrawerLayout mainLayout;
    public FloatingActionButton floatingActionButton;

    private AnimationObject CurrentFragment;
    private AnimationDirection currentDirection;
    private Thread memoryCheckThread;

    public TabView tabLeft;
    public TabView tabRight;
    public static Context mainContext;

    public String[] RequestedPermissions(){
        try {
            PackageInfo pInfo =  getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            return pInfo != null ? pInfo.requestedPermissions : null;
        }
        catch (Exception e){
            return null;
        }
    }

    private enum AnimationDirection{
        NO_ANIMATION,
        FROM_LEFT_TO_RIGHT,
        FROM_RIGHT_TO_LEFT
    }

    private enum AnimationObject{
        NOT_ANIMATED,
        TAB_LEFT,
        HOME,
        TAB_RIGHT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            SetupObjects();
            BottomNavigationBarSetup();
            LeftNavigationViewSetup();
            TopBarSetup();
            SetupFloatButton();
        }
        catch (Exception e){
            Log.i("myLog", e.toString());
        }

        Toasty.success(this,this.getString(R.string.login_success), Toast.LENGTH_LONG).show();

    }

    private void SetupObjects(){
        mainContext = this;
        tabLeft = new TabView();
        tabRight = new TabView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        CheckPermissions();
    }

    private void CheckPermissions() {
        String[] reqPerm = RequestedPermissions();
        if(reqPerm != null) {
            ActivityCompat.requestPermissions(this, reqPerm, 1);
        }
    }

    private void EnviromentExit(){
        this.finish();
        System.exit(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestsCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestsCode, permissions, grantResults);

        PermissionsInfo pInfo = new PermissionsInfo();
        if(!pInfo.IsOnPermissionsGranted()){
            alertDialogBuider = new AlertDialog.Builder(this);
            alertDialogBuider.setMessage(getString(R.string.permission_not_found)).setTitle(R.string.permission_deny).
                    setCancelable(false).setPositiveButton(getString(R.string.grant_permission), (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }).
                    setNegativeButton(getString(R.string.exit), (dialogInterface, i) -> EnviromentExit());

            AlertDialog alert = alertDialogBuider.create();
            alert.setTitle(getString(R.string.permission_deny));
            alert.show();
        
        }
    }

    private void GoHome(){
        IsAnimationDirection(AnimationObject.HOME);
        SkipSelectedBottomBar();
        CommitFragment(HomeFragment.Instance());
        ShowBottomBar();
    }

    @Override
    public  boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.tabLeft:
                IsAnimationDirection(AnimationObject.TAB_LEFT);
                CommitFragment(tabLeft);
                break;
            case R.id.tabRight:
                IsAnimationDirection(AnimationObject.TAB_RIGHT);
                CommitFragment(tabRight);
                break;
            case R.id.home_button:
                GoHome();
                break;
            case R.id.settings_button:
                IsAnimationDirection(AnimationObject.NOT_ANIMATED);
                CommitFragment(SettingsFragment.Instance());
                SkipSelectedBottomBar();
                HideBottomBar();
                return true;
            case R.id.info_button:
                IsAnimationDirection(AnimationObject.NOT_ANIMATED);
                CommitFragment(AboutFragment.Instance());
                SkipSelectedBottomBar();
                HideBottomBar();
                return true;
            case R.id.dev_button:
                IsAnimationDirection(AnimationObject.NOT_ANIMATED);
                CommitFragment(DevInfoFragment.Instance());
                SkipSelectedBottomBar();
                HideBottomBar();
                return true;
            case R.id.where_file:
                IsAnimationDirection(AnimationObject.NOT_ANIMATED);
                Toasty.success(mainContext, getString(R.string.where_file_detail), Toast.LENGTH_LONG).show();
                return true;
        }
        ShowBottomBar();
        return true;
    }

    protected  void CommitFragment(Fragment fragment){
        if(mainLayout != null){
            mainLayout.closeDrawer(GravityCompat.START);
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragmentTransaction != null && fragment != null) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();

            if(currentDirection == AnimationDirection.FROM_LEFT_TO_RIGHT){
                fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
            }
            else if(currentDirection == AnimationDirection.FROM_RIGHT_TO_LEFT){
                fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
            }

            for (Fragment itemFragment : fragments) {
                if (itemFragment.getClass().getSimpleName() != fragment.getClass().getSimpleName()) {
                    fragmentTransaction.hide(itemFragment);
                }
            }

            if(fragment.isAdded()){
                fragmentTransaction.show(fragment);
                fragmentTransaction.commit();
                return;
            }

            for (Fragment itemFragment : fragments) {
                if (itemFragment.getClass().getSimpleName() == fragment.getClass().getSimpleName()) {
                    fragmentTransaction.show(itemFragment);
                    fragmentTransaction.commit();
                    return;
                }
            }
            fragmentTransaction.add(R.id.FragmentContainer, fragment);
            fragmentTransaction.commit();
        }
    }

    protected void IsAnimationDirection(AnimationObject newObject){
        CurrentFragment = CurrentFragment != null ? CurrentFragment : AnimationObject.NOT_ANIMATED;

        if(newObject.ordinal() > CurrentFragment.ordinal()){
            currentDirection = AnimationDirection.FROM_RIGHT_TO_LEFT;
        }
        else {
            currentDirection = newObject.ordinal() < CurrentFragment.ordinal() ? AnimationDirection.FROM_LEFT_TO_RIGHT : AnimationDirection.NO_ANIMATION;
        }

        CurrentFragment = newObject;
    }

    protected void BottomNavigationBarSetup(){
        bottomAppBar = findViewById(R.id.BottomAppBar);
        bottomNavigationView = findViewById(R.id.bottomNavView);
        if(bottomNavigationView != null){
            bottomNavigationView.setOnItemSelectedListener(this);
        }
    }

    protected void LeftNavigationViewSetup(){
        appNavigationView = findViewById(R.id.navigationView);
        if(appNavigationView != null){
            appNavigationView.inflateHeaderView(R.layout.navigation_head);
            appNavigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        }
    }

    protected void TopBarSetup(){
        appMenuButton = findViewById(R.id.topMenuButton);
        mainLayout = findViewById(R.id.mainContainer);

        appMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    protected void SetupFloatButton(){
        floatingActionButton = findViewById(R.id.homeFloatingButton);
        if (floatingActionButton!=null){
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoHome();
                }
            });
        }
        floatingActionButton.performClick();
    }

    protected void SkipSelectedBottomBar(){
        if(bottomNavigationView != null){
            bottomNavigationView.findViewById(R.id.Placeholder).performClick();
        }
    }

    protected  void HideBottomBar(){
        if(bottomAppBar != null && floatingActionButton != null){
            bottomAppBar.performHide();
            floatingActionButton.hide();
        }
    }
     protected void ShowBottomBar(){
         if(bottomAppBar != null && floatingActionButton != null){
            bottomAppBar.performShow();
            floatingActionButton.show();
         }
     }

     private boolean doubleBackPress = false;
     @Override
     public void onBackPressed() {
         if (doubleBackPress) {
             super.onBackPressed();
             doubleBackPress = false;
             return;
         }

         doubleBackPress = true;

         if(tabLeft.getOnBackListenerObject() != null){
            if(tabLeft.getInitialState()) {
                GoHome();
            }
            else{
                tabLeft.onBackPressed();
            }
         }else if(tabRight.getOnBackListenerObject() != null){
             if(tabRight.getInitialState()){
                 GoHome();
             }
             else{
                 tabRight.onBackPressed();
             }
         }
         else if(AboutFragment.Instance().getOnBackListenerObject() != null){
             GoHome();
         }
         else if(DevInfoFragment.Instance().getOnBackListenerObject() != null){
             GoHome();
         }
         else if(SettingsFragment.Instance().getOnBackListenerObject() != null){
             GoHome();
         }
         new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
             @Override
             public void run() {
                 doubleBackPress = false;
             }
         }, 200);
     }
}

