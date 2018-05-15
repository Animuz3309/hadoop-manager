package edu.scut.cs.hm.agent.notifier;

import edu.scut.cs.hm.agent.config.props.NotifierProperties;
import edu.scut.cs.hm.agent.gather.SysInfoGather;
import edu.scut.cs.hm.common.utils.AddressUtils;
import edu.scut.cs.hm.common.utils.OSUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;

import static lombok.AccessLevel.PACKAGE;

@Component
class DataProvider {
    private final SysInfoGather gather;
    @Getter(PACKAGE)
    private final String hostname;
    private final String address;

    @Autowired
    DataProvider(NotifierProperties notifierProperties, ServerProperties serverProperties) {
        this.gather = new SysInfoGather(notifierProperties.getRootPath());
        this.address = getAddress(notifierProperties.getAddress(), serverProperties);
        this.hostname = OSUtils.getHostName();
    }

    NotifierData getData() {
        gather.refresh();
        NotifierData data = new NotifierData();
        data.setName(hostname);
        data.setAddress(address);
        data.setSystem(gather.getSysInfo());
        data.setTime(ZonedDateTime.now());
        return data;
    }

    private String getAddress(String predefinedAddress, ServerProperties serverProperties) {
        String protocol = (serverProperties.getSsl() == null) ? "http://" : "https://";
        if (StringUtils.hasText(predefinedAddress)) {
            String hostAndPort = AddressUtils.getHostPort(predefinedAddress);
            return protocol + hostAndPort;
        }

        int port = serverProperties.getPort();
        String host;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            host = "localhost";
        }
        return protocol + host + ":" + port;
    }
}
