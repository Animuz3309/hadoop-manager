package edu.scut.cs.hm.admin.web.model.volume;

import edu.scut.cs.hm.common.utils.Sugar;
import edu.scut.cs.hm.docker.model.volume.Volume;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UiVolume {
    private String name;
    private String driver;
    private final Map<String, String> options = new HashMap<>();
    private final Map<String, String> labels = new HashMap<>();
    @ApiModelProperty(readOnly = true)
    private String mountpoint;
    @ApiModelProperty(readOnly = true)
    private Volume.Scope scope;
    @ApiModelProperty(readOnly = true)
    private final Map<String, String> status = new HashMap<>();
    @ApiModelProperty(readOnly = true)
    private int usageRefCount;
    @ApiModelProperty(readOnly = true)
    private long usedSize;

    public static UiVolume from(Volume volume) {
        UiVolume ui = new UiVolume();
        ui.setName(volume.getName());
        ui.setDriver(volume.getDriver());
        Sugar.setIfNotNull(ui.getOptions()::putAll, volume.getOptions());
        Sugar.setIfNotNull(ui.getLabels()::putAll, volume.getLabels());
        ui.setMountpoint(volume.getMountpoint());
        ui.setScope(volume.getScope());
        Sugar.setIfNotNull(ui.getStatus()::putAll, volume.getStatus());
        Volume.VolumeUsageData ud = volume.getUsageData();
        if(ud != null) {
            ui.setUsageRefCount(ud.getRefCount());
            ui.setUsedSize(ud.getSize());
        }
        return ui;
    }
}
