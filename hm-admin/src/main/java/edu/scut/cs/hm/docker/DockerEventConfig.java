package edu.scut.cs.hm.docker;

import lombok.Data;

@Data
public class DockerEventConfig {
    private int countOfThreads = 2;
    private int periodInSeconds = 90;
    private int initialDelayInSeconds = 10;
}
