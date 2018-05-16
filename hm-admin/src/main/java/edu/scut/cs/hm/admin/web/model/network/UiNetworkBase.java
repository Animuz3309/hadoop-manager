package edu.scut.cs.hm.admin.web.model.network;

import edu.scut.cs.hm.common.utils.Sugar;
import edu.scut.cs.hm.docker.cmd.CreateNetworkCmd;
import edu.scut.cs.hm.docker.model.network.Network;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class UiNetworkBase implements Comparable<UiNetworkBase> {
    private String name;
    private String cluster;
    private String driver;
    private final Map<String, String> labels = new HashMap<>();
    private final Map<String, String> options = new HashMap<>();
    private Boolean enableIpv6;
    private Boolean internal;
    private Ipam ipam;

    public void to(CreateNetworkCmd cmd) {
        Sugar.setIfNotNull(cmd::setName, getName());
        Sugar.setIfNotNull(cmd::setDriver, getDriver());
        Sugar.setIfNotNull(cmd.getOptions()::putAll, getOptions());
        Ipam ipam = getIpam();
        if(ipam != null) {
            cmd.setIpam(ipam.to());
        }
        Sugar.setIfNotNull(cmd::setInternal, getInternal());
        Sugar.setIfNotNull(cmd::setEnableIpv6, getEnableIpv6());
        Sugar.setIfNotNull(cmd.getLabels()::putAll, getLabels());
    }

    public UiNetworkBase from(Network net) {
        setName(net.getName());
        setDriver(net.getDriver());
        Sugar.setIfNotNull(getOptions()::putAll, net.getOptions());
        Network.Ipam ipam = net.getIpam();
        if(ipam != null) {
            setIpam(new Ipam().from(ipam));
        }
        setInternal(net.isInternal());
        setEnableIpv6(net.isEnableIpv6());
        Sugar.setIfNotNull(getLabels()::putAll, net.getLabels());
        return this;
    }

    @Override
    public int compareTo(UiNetworkBase o) {
        return ObjectUtils.compare(this.getName(), o.getName());
    }

    @Data
    public static class Ipam {

        private String driver;

        private final List<IpamConfig> config = new ArrayList<>();

        private final Map<String, String> options = new HashMap<>();

        public Network.Ipam to() {
            Network.Ipam.Builder ib = Network.Ipam.builder();
            ib.driver(getDriver());
            Sugar.setIfNotNull(ib::options, getOptions());
            getConfig().forEach(csrc -> ib.config(csrc.to()));
            return ib.build();
        }

        public Ipam from(Network.Ipam ipam) {
            String driver = ipam.getDriver();
            if(driver == null) {
                // docker does not allow null value for this field.
                driver = "default";
            }
            setDriver(driver);
            List<Network.IpamConfig> configs = ipam.getConfigs();
            if(configs != null) {
                configs.forEach(oc -> getConfig().add(IpamConfig.from(oc)));
            }
            return this;
        }
    }


    @Data
    public static class IpamConfig {

        private String subnet;

        private String range;

        private String gateway;

        public static IpamConfig from(Network.IpamConfig oc) {
            IpamConfig ic = new IpamConfig();
            ic.setGateway(oc.getGateway());
            ic.setRange(oc.getIpRange());
            ic.setSubnet(oc.getSubnet());
            return ic;
        }

        public Network.IpamConfig to() {
            return Network.IpamConfig.builder()
                    .gateway(getGateway())
                    .ipRange(getRange())
                    .subnet(getSubnet())
                    .build();
        }
    }
}
