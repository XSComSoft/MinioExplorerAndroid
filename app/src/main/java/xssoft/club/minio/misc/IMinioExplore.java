package xssoft.club.minio.misc;

import java.util.List;

public interface IMinioExplore {
    public List<State> FolderNavigation (String path) throws Exception;
    public String getParentPath();

    List<String> getAdapter();
    void setBucket(String bucket);

}
