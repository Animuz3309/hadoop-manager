package edu.scut.cs.hm.admin.web.model.images;

import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.image.ImageName;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UiDeployedImage extends UiImageData {

    @Data
    public static class UiContainerShort {
        private String name;
        private String id;
        private String node;
        @ApiParam("Id of service which is own container")
        private String service;

        public static UiContainerShort toUi(DockerContainer dc) {
            UiContainerShort uc = new UiContainerShort();
            uc.setId(dc.getId());
            uc.setName(dc.getName());
            uc.setNode(dc.getNode());
            uc.setService(dc.getService());
            return uc;
        }
    }

    private String name;
    private String currentTag;
    private String registry;
    private final List<UiContainerShort> containers = new ArrayList<>();

    public UiDeployedImage(String id) {
        super(id);
    }

    private void fillFromContainer(DockerContainer dc) {
        String fullImageName = dc.getImage();
        updateName(fullImageName);
    }

    public void updateName(String fullImageName) {
        if(ImageName.isId(fullImageName)) {
            return;
        }
        ImageName in = ImageName.parse(fullImageName);
        setName(in.getName());
        if(currentTag == null || currentTag.equals(ImageName.TAG_LATEST)) {
            // we want any tag that is differ from 'latest', because it can mark any version
            setCurrentTag(in.getTag());
        }
    }

    public void addContainer(DockerContainer dc) {
        fillFromContainer(dc);
        UiContainerShort uc = UiContainerShort.toUi(dc);
        containers.add(uc);
    }
}

