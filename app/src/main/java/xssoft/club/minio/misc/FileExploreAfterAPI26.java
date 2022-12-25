package xssoft.club.minio.misc;

import static xssoft.club.minio.BuildConfig.APPLICATION_ID;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import xssoft.club.minio.MainActivity;
import xssoft.club.minio.R;

public class FileExploreAfterAPI26 implements IFileExplore {

    private String parentPath;
    @Override
    public String getParentPath(){
        return parentPath;
    }

    private boolean isInitial;
    @Override
    public boolean getIsInitial(){
        return isInitial;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public List<State> FolderNavigation(String path) {
        List<State> newStateArray = new ArrayList<State>();
        File CurrentItem = new File(path);
        if(CurrentItem == null) return null;

        if(CurrentItem.isFile()){
            OpenFile(CurrentItem);
            return null;
        }

        try{
            parentPath = CurrentItem.getParent();
            if(CurrentItem.list() == null){
                return InitializeData();
            }

            isInitial = false;
            for (String file : CurrentItem.list()){
                Path currentItemPath = Paths.get(path + "/"+file);
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                String fileMimeType = myMime.getMimeTypeFromExtension(FileExtensions(currentItemPath.toString()));
                BasicFileAttributes currentItemAttr = Files.readAttributes(currentItemPath,BasicFileAttributes.class);;
                long fileSize = Files.size(currentItemPath);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String currentItemLastModifiedDate = sdf.format(currentItemPath.toFile().lastModified());

                newStateArray.add(new State(file,
                        currentItemAttr.isDirectory() ? "" : FormatBytes(fileSize),
                        currentItemLastModifiedDate != null ? currentItemLastModifiedDate : "",
                        getImageIcon(currentItemAttr,currentItemPath.toFile(),fileMimeType),
                        currentItemPath.toString(),
                        fileMimeType, false, View.INVISIBLE));
            }
        }
        catch (Exception e){
            newStateArray = null;
            Log.i("myLog", e.toString() + "!!!");
        }

        return newStateArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Drawable getImageIcon(BasicFileAttributes attr, File currentFile, String mimeType){
        Drawable newDrawable = MainActivity.mainContext.getDrawable(R.drawable.empty_file_ic);;
        if (attr.isDirectory())
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.folder_ic);
        else if(mimeType == null){}
        else if(mimeType.contains("image")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.image_file_ic);
        }
        else if(mimeType.contains("video")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.video_file_ic);
        }
        else if(mimeType.contains("audio")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.audio_file_ic);
        }
        else if(mimeType.contains("pdf")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.pdf_ic);
        }
        else if(mimeType.contains("package-archive")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.apk_ic);
        }
        else if(mimeType.contains("text")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.file_ic);
        }
        return newDrawable;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public List<State> InitializeData() {
        List<State> arrFiles = new ArrayList<State>();
        arrFiles.clear();
        File[] appsDir = ContextCompat.getExternalFilesDirs(MainActivity.mainContext, null);
        ArrayList<File> extRootPath = new ArrayList<File>();
        for (File file1 : appsDir) {
            extRootPath.add(file1.getParentFile().getParentFile().getParentFile().getParentFile());
        }

        for(File filesToStorage : extRootPath){
            arrFiles.add(new State(filesToStorage.getName(),"","",
                    MainActivity.mainContext.getDrawable(R.drawable.sd_card_ic), filesToStorage.getPath(), null, false, View.INVISIBLE));
        }

        isInitial = true;
        return arrFiles;
    }

    public void OpenFile(File file){
        try {

            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            String mimeType = myMime.getMimeTypeFromExtension(FileExtensions(file.getPath()));

            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Uri myUri = FileProvider.getUriForFile(MainActivity.mainContext, APPLICATION_ID, file);
            newIntent.setDataAndType(myUri,mimeType);

            MainActivity.mainContext.startActivity(newIntent);
        }
        catch (Exception e){
            Log.i("myLog", e.toString());
        }
    }

    public String FormatBytes(long size){
        if(size <= 0) return "0 B";
        String[] units = new String[]{"B","KB", "MB", "GB", "TB"};
        int unitGroup = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, unitGroup)) + " " + units[unitGroup];
    }

    private  String FileExtensions(String url){
        if(url.indexOf("?") > -1){
            url = url.substring(0,url.indexOf("?"));
        }
        if(url.lastIndexOf(".") == -1){
            return null;
        }
        else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if(ext.indexOf("%")>-1){
                ext = ext.substring(0,ext.indexOf("%"));
            }
            if(ext.indexOf("/")>-1){
                ext = ext.substring(0,ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }
}
