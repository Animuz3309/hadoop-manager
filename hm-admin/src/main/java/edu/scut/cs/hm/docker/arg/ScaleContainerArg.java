package edu.scut.cs.hm.docker.arg;

import lombok.Data;

@Data
public class ScaleContainerArg {
    private String containerId;
    private int scale;
}
