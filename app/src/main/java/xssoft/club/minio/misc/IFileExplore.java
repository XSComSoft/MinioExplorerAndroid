package xssoft.club.minio.misc;

import java.util.List;

public interface IFileExplore {
    public List<State> FolderNavigation (String path);
    public List<State> InitializeData();
    public String getParentPath();
    public boolean getIsInitial();
}
