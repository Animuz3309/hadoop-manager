package edu.scut.cs.hm.admin.web.model.service;

import edu.scut.cs.hm.docker.model.network.Port;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.model.source.ServiceSource;
import edu.scut.cs.hm.model.source.SourceUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * UI representation for Container service create.
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UiContainerServiceCreate extends ServiceSource {

    @ApiModelProperty("service.version, need for update")
    protected long version;
    protected final List<Port> ports = new ArrayList<>();

    public Service.ServiceSpec.Builder toServiceSpec() {
        Service.ServiceSpec.Builder ssb = Service.ServiceSpec.builder();
        SourceUtil.fromSource(this, ssb);
        return ssb;
    }
}
