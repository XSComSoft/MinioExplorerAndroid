package xssoft.club.minio.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;

import xssoft.club.minio.MainActivity;
import xssoft.club.minio.R;
import xssoft.club.minio.misc.IMinioExplore;
import xssoft.club.minio.misc.IOnBackListener;
import xssoft.club.minio.misc.MinioExploreAfterAPI26;
import xssoft.club.minio.misc.State;
import xssoft.club.minio.misc.StateAdapter;

public class TabView extends Fragment implements IOnBackListener {

    List<State> states = new ArrayList();
    RecyclerView recyclerView;
    IMinioExplore FileExploreClass;
    boolean IsLinearLayoutManager;
    LayoutInflater currentInflater;
    StateAdapter mAdapter;
    AppCompatImageButton changeLayoutButton;
    public AppCompatImageButton returnButton;
    TabView currentTabView;
    String Location;
    IOnBackListener onBackListenerObject;
    public BottomAppBar bAppBar;
    public CoordinatorLayout bAppBarLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tab_view, container, false);


        currentTabView = this;

        //InitializeRecycleView(view, inflater);
        InitializeButtons(view);

        Spinner spinner = view.findViewById(R.id.menu_recycle_button);

        try {
            FileExploreClass = new MinioExploreAfterAPI26(this.getContext());
            final List<String> buckets = FileExploreClass.getAdapter();

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, buckets);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view2, int position, long id) {
                    String bucket = buckets.get(position);
                    InitializeRecycleView(view, inflater, bucket);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        onBackListenerObject = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        onBackListenerObject = this;
    }

    private void initAdapter(View view){
        changeLayoutButton = view.findViewById(R.id.change_layout);
        changeLayoutButton.setImageDrawable(IsLinearLayoutManager ?
                MainActivity.mainContext.getDrawable(R.drawable.list_ic) :
                MainActivity.mainContext.getDrawable(R.drawable.grid_ic));

        if(IsLinearLayoutManager){
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        }else{
            GridLayoutManager gridLayoutManager = new GridLayoutManager(recyclerView.getContext(),4);
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        mAdapter = new StateAdapter(currentInflater, states, FileExploreClass, IsLinearLayoutManager, currentTabView);
        recyclerView.setAdapter(mAdapter);
        recyclerView.scrollToPosition(0);
    }

    private synchronized void InitializeRecycleView(View view, LayoutInflater inflater, String bucket){
        final KProgressHUD hub = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDetailsLabel("正在连接到桶:" + bucket)
                .setLabel("正在初始化")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        FileExploreClass.setBucket(bucket);
        new Thread(()->{
            try {
                IsLinearLayoutManager = false;
                currentInflater = inflater;
                recyclerView = view.findViewById(R.id.fileRecycleView);

                states = FileExploreClass.FolderNavigation("");

                ((Activity)getContext()).runOnUiThread(()->{
                    initAdapter(view);
                    recyclerView.setAdapter(mAdapter);

                    changeLayoutButton.setOnClickListener(view1 -> {
                        IsLinearLayoutManager = !IsLinearLayoutManager;
                        initAdapter(view1);
                    });

                    returnButton = view.findViewById(R.id.return_button);
                    returnButton.setVisibility(View.GONE);
                    returnButton.setOnClickListener(view12 -> mAdapter.openPath(FileExploreClass.getParentPath()));

                    bAppBar = view.findViewById(R.id.tabView_bottomAppBar);
                    bAppBar.performHide();

                    hub.dismiss();
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void InitializeButtons(View view){

    }

    @Override
    public void  onHiddenChanged(boolean hidden){
        onBackListenerObject = !hidden ? this : null;
    }


    @Override
    public void onBackPressed() {
        if(bAppBar != null){
            bAppBar.performHide();
        }
        if(FileExploreClass!=null)
            mAdapter.openPath(FileExploreClass.getParentPath());

    }


    @Override
    public boolean getInitialState() {
        return FileExploreClass != null;
    }

    @Override
    public IOnBackListener getOnBackListenerObject() {
        return onBackListenerObject;
    }
}