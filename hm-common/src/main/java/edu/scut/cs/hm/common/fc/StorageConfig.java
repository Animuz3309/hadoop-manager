package edu.scut.cs.hm.common.fc;

public interface StorageConfig {
    long getMaxFilesSize();
    void setMaxFilesSize(long maxFilesSize);
    int getMaxFiles();
    void setMaxFiles(int maxFiles);
}
