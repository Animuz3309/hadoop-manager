package edu.scut.cs.hm.common.fc;

import java.util.List;

public interface IndexFile extends StorageConfig {
    boolean isExists();
    List<String> getList();
    void setList(List<String> list);
    String getId();
    void setId(String id);

    default void init(String id, FbStorage fb) {
        setId(id);
        setMaxFilesSize(fb.getMaxFileSize());
        setMaxFiles(fb.getMaxFiles());
    }

}
