package xssoft.club.minio.misc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import es.dmoral.toasty.Toasty;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import xssoft.club.minio.BuildConfig;
import xssoft.club.minio.MainActivity;
import xssoft.club.minio.R;

public class MinioExploreAfterAPI26 implements IMinioExplore {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final MimeTypeMap myMime = MimeTypeMap.getSingleton();
    private MinioClient minioClient;
    private String bucket;
    private File folder;
    private Context context;
    private static final BigDecimal NORMAL_PERCENT = BigDecimal.valueOf(100);

    public MinioExploreAfterAPI26(Context context) throws Exception{
        SharedPreferences p = context.getSharedPreferences("xssoft", Context.MODE_PRIVATE);
        this.minioClient = MinioClient.builder()
                .endpoint(p.getString("address", ""))
                .credentials(p.getString("key", ""),
                        p.getString("token", ""))
                .build();
        folder = android.os.Environment.getExternalStorageDirectory();
        folder = new File(folder, "XS_SOFT");
        if(!folder.exists() || folder.isFile()){
            folder.mkdir();
        }
        this.context = context;
    }

    private String parentPath;
    @Override
    public String getParentPath(){
        return parentPath;
    }

    @Override
    public List<String> getAdapter() {
        try {
            return minioClient.listBuckets().stream().map(e->e.name()).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public List<State> FolderNavigation(String path) throws Exception{
        List<State> newStateArray = new ArrayList<>();
        Iterator<Result<Item>> _items = minioClient.listObjects(ListObjectsArgs.builder().prefix(path).bucket(bucket).build()).iterator();
        List<Item> items = new ArrayList<>();

        while (_items.hasNext()){
            items.add(_items.next().get());
        }
        items.sort((a,b)-> {
            String s1 = a.objectName().toLowerCase();
            String s2 = b.objectName().toLowerCase();
            final int s1Dot = s1.lastIndexOf('.');
            final int s2Dot = s2.lastIndexOf('.');
            //
            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither
                s1 = s1.substring(s1Dot + 1);
                s2 = s2.substring(s2Dot + 1);
                return s1.compareTo(s2);
            } else if (s1Dot == -1) {
                return -1;
            } else {
                return 1;
            }
        });

        if(items.size() == 0){
            return null;
        }

        if(items.size() == 1 && !items.get(0).isDir()){
            OpenFile(items.get(0));
            return null;
        }

        try{
            for (Item file : items){
                Item item = file;
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                String fileMimeType = getMime(item.objectName());
                long fileSize = item.size();
                String[] names = item.objectName().split("/");
                newStateArray.add(new State(names[names.length-1],
                        item.isDir() ? "" : FormatBytes(fileSize),
                        !item.isDir()? formatter.format(item.lastModified()) : "",
                        getImageIcon(fileMimeType, item.isDir()),
                        item.objectName(),
                        fileMimeType, false, View.INVISIBLE));
            }
            if(path!=null) {
                StringBuilder sb = new StringBuilder();
                String[] folders = path.split("/");
                for (int i = 0; i < folders.length - 1; i++) {
                    sb.append(folders[i]);
                    sb.append("/");
                }
                this.parentPath = sb.toString();
            }

        }
        catch (Exception e){
            newStateArray = null;
            Log.i("myLog", e.toString() + "!!!");
        }

        return newStateArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Drawable getImageIcon(String mimeType, Boolean dir) throws Exception{
        Drawable newDrawable = MainActivity.mainContext.getDrawable(R.drawable.empty_file_ic);;
        if (dir)
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.folder_ic);
        else if(mimeType == null){}
        else if(mimeType.contains("zip")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.zip);
        }
        else if(mimeType.contains("program")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.exe);
        }
        else if(mimeType.equals("url")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.url);
        }
        else if(mimeType.equals("vbs")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.vbs);
        }
        else if(mimeType.contains("image")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.image_file_ic);
        }
        else if(mimeType.contains("video")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.video_file_ic);
        }
        else if(mimeType.contains("audio")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.audio_file_ic);
        }
        else if(mimeType.contains("java")){
            newDrawable = MainActivity.mainContext.getDrawable(R.drawable.jar);
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

    private String getMime(String name){
        String mimeType = myMime.getMimeTypeFromExtension(FileExtensions(name));
        if(name!=null && mimeType == null){
            if(name.endsWith(".url")||name.endsWith(".lnk")){
                mimeType = "url";
            }else if(name.endsWith(".vbs")){
                mimeType = "vbs";
            }
        }
        return mimeType;
    }

    public synchronized void OpenFile(Item item) {
        String[] names = item.objectName().split("/");
        final String name = names[names.length - 1];
        final KProgressHUD hub = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.BAR_DETERMINATE)
                .setMaxProgress(100)
                .setLabel("正在下载")
                .setDetailsLabel(name)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        final boolean[] cancel = {false};
        hub.setCancellable(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface dialog) {
                cancel[0] = true;
            }
        });
            new Thread(()->{
                Looper.prepare();

                try{
                    File file = new File(folder, name);
                    if(!file.exists()){
                        file.createNewFile();
                    }

                    String mimeType = myMime.getMimeTypeFromExtension(FileExtensions(file.getPath()));

                    FileOutputStream fo = new FileOutputStream(file);
                    GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(item.objectName()).build());
                    byte[] bytes = new byte[1024];
                    int total = 0;
                    int bytesRead;
                    long size = item.size();
                    while ((bytesRead = response.read(bytes)) != -1) {
                        if(cancel[0]){
                            fo.close();
                            response.close();
                            ((Activity)context).runOnUiThread(() -> {

                                Toasty.error(context,context.getString(R.string.download_cancel), Toast.LENGTH_LONG).show();
                            });
                            return;
                        }
                        fo.write(bytes, 0, bytesRead);
                        total += bytesRead;

                        int finalTotal = total;
                        ((Activity)context).runOnUiThread(() -> {
                            hub.setProgress(BigDecimal.valueOf(Double.valueOf(finalTotal) / Double.valueOf(size)).multiply(BigDecimal.valueOf(100)).intValue());
                        });
                    }
                    response.close();
                    fo.close();

                    String text = String.format(MainActivity.mainContext.getString(R.string.already_save), file.getPath());
                    int duration = Toast.LENGTH_SHORT;

                    Toasty.success(context, text, duration).show();

                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Uri myUri = FileProvider.getUriForFile(MainActivity.mainContext, BuildConfig.APPLICATION_ID, file);
                    newIntent.setDataAndType(myUri, mimeType);

                    MainActivity.mainContext.startActivity(newIntent);
                    hub.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Looper.loop();
            }).start();
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
