package edu.scut.cs.hm.docker.arg;

import edu.scut.cs.hm.docker.model.container.EditableContainerSource;
import lombok.Data;

@Data
public class EditContainerArg {
    private String containerId;
    private EditableContainerSource source;
}
