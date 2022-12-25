package xssoft.club.minio.misc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.List;

import xssoft.club.minio.MainActivity;
import xssoft.club.minio.R;
import xssoft.club.minio.ui.TabView;

public class StateAdapter extends RecyclerView.Adapter<StateAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<State> states;
    private final IMinioExplore FileExploreClass;
    private final boolean IsLayoutManagerLinear;
    private TabView currentTabView;
    public Thread setupScetchTh;


    public StateAdapter(LayoutInflater inflaterParent, List<State> states, IMinioExplore fileExploreClass, boolean isLayoutManagerLinear, TabView tabView) {
        this.states = states;
        this.inflater = inflaterParent;
        FileExploreClass = fileExploreClass;
        IsLayoutManagerLinear = isLayoutManagerLinear;
        currentTabView = tabView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(IsLayoutManagerLinear ? R.layout.list_item_template : R.layout.grid_item_template, parent, false);
        return new ViewHolder(view, IsLayoutManagerLinear);
    }

    @Override
    public void onBindViewHolder(ViewHolder holderList, int position) {
        try {
            State state = states.get(position);
            holderList.iconView.setImageDrawable(state.getImageObject());

            holderList.nameView.setText(state.getNameObject());
            holderList.checkedView.setChecked(state.getCheckedObject());
            holderList.dateChangeView.setText(state.getDateChangeObject());
            holderList.sizeView.setText(state.getSizeObject());
            holderList.getCurrentView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPath(state.getButtonPath());
                }
            });

            holderList.getCurrentView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(currentTabView.bAppBar != null){
                        currentTabView.bAppBar.performShow();
                    }
                    setCheckBoxesVisible(true);

                    return true;
                }
            });
        }catch (Exception e){
            Log.i("myLog", e.toString());
        }
    }

    public synchronized void openPath(String path){
        final KProgressHUD hub = KProgressHUD.create(this.currentTabView.getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDetailsLabel(path == null || path.length() == 0? "/":path)
                .setLabel("处理")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        ((Activity)currentTabView.getContext()).runOnUiThread(()->{
            List<State> newStates = null;
            try {
                newStates = FileExploreClass.FolderNavigation(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(newStates !=null){
                if(setupScetchTh != null)
                    setupScetchTh.interrupt();
                states.clear();
                states.addAll(newStates);
                notifyDataSetChanged();
                currentTabView.returnButton.setVisibility(path == null || path.length() == 0 ? View.GONE :View.VISIBLE);
                setupScetchTh = new Thread(this::setScetchImage);
                setupScetchTh.start();
            }
            hub.dismiss();
        });
    }

    private void setScetchImage(){
        try {
            for (State currentItem:states) {
                if(Thread.currentThread().isInterrupted()) return;
                if(currentItem.getCurrentMimeType() != null && currentItem.getCurrentMimeType().contains("image")){
                    Bitmap bmp = BitmapFactory.decodeFile(currentItem.getButtonPath());
                    currentItem.setImageObject(new BitmapDrawable(MainActivity.mainContext.getResources(), Bitmap.createScaledBitmap(bmp, 45, 45, false)));
                    if(bmp != null && !bmp.isRecycled()){
                        bmp.recycle();
                        bmp = null;
                    }
                }
            }
        }catch (Exception  ex){

        }
    }

    public void setCheckBoxesVisible(boolean isVisible){
        for (State state:states) {
            state.setCheckedObjectVisible(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return states == null? 0 : states.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        AppCompatTextView nameView, dateChangeView, sizeView;
        String buttonPath;
        CheckBox checkedView;
        private final View currentView;
        int checkedViewVisibleState;

        ViewHolder(View view, boolean isLinearLayoutManager) {
            super(view);
            iconView = isLinearLayoutManager ? view.findViewById(R.id.icon_template) : view.findViewById(R.id.icon_template_grid);
            nameView = isLinearLayoutManager ? view.findViewById(R.id.name_template) : view.findViewById(R.id.name_template_grid);
            checkedView = isLinearLayoutManager ? view.findViewById(R.id.checkbox_template) : view.findViewById(R.id.checkbox_template_grid);
            dateChangeView = isLinearLayoutManager ? view.findViewById(R.id.dateChange_template) : view.findViewById(R.id.dateChange_template_grid);
            sizeView = isLinearLayoutManager ? view.findViewById(R.id.size_template) : view.findViewById(R.id.size_template_grid);
            buttonPath = "";
            currentView = view;
            checkedViewVisibleState= View.INVISIBLE;
        }

        public View getCurrentView(){return currentView;}
    }
}
