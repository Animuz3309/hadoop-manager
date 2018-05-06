package edu.scut.cs.hm.docker.arg;

import edu.scut.cs.hm.model.source.EditableContainerSource;
import lombok.Data;

@Data
public class EditContainerArg {
    private String containerId;
    private EditableContainerSource source;
}
