package xssoft.club.minio.misc;

import android.graphics.drawable.Drawable;

public class State {
    public State(String name, String size, String dateChange, Drawable image, String path, String mimeType, boolean check, int checkVisible){
        nameObject = name;
        sizeObject = size;
        dateChangeObject = dateChange;
        imageObject = image;
        buttonPath =path;
        currentMimeType = mimeType;
        checkedObject = check;

    }

    private String nameObject;
    public String getNameObject(){
        return nameObject;
    }
    public void setNameObject(String name){
        nameObject = name;
    }

    private String sizeObject;
    public String getSizeObject(){
        return sizeObject;
    }
    public void setSizeObject(String size){
        sizeObject = size;
    }

    private String dateChangeObject;
    public String getDateChangeObject(){
        return dateChangeObject;
    }
    public void setDateChangeObject(String dateChange){
        dateChangeObject = dateChange;
    }

    private Drawable imageObject;
    public Drawable getImageObject(){
        return imageObject;
    }
    public void setImageObject(Drawable image){
        imageObject = image;
    }

    private boolean checkedObject;
    public boolean getCheckedObject(){return checkedObject;}
    public void setCheckedObject(boolean check){checkedObject = check;}

    private int checkedObjectVisible;
    public int getCheckedObjectVisible(){return checkedObjectVisible;}
    public void setCheckedObjectVisible(int checkVisible){checkedObjectVisible = checkVisible;}

    private String buttonPath;
    public String getButtonPath(){return buttonPath;}
    public void setButtonPath(String path){ buttonPath = path; }

    private String currentMimeType;
    public String getCurrentMimeType(){ return currentMimeType;}
    public void setCurrentMimeType(String mime){currentMimeType = mime;}
}

