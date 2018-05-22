package edu.scut.cs.hm.admin.ds;

import com.google.common.base.Strings;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.agent.notifier.NotifierData;
import edu.scut.cs.hm.agent.notifier.SysInfo;
import edu.scut.cs.hm.common.utils.AddressUtils;
import edu.scut.cs.hm.model.node.DiskInfo;
import edu.scut.cs.hm.model.node.NetIfaceCounter;
import edu.scut.cs.hm.model.node.NodeMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@RequestMapping("/discovery")
public class DiscoveryNodeController {

    private final NodeStorage storage;
    private final String nodeSecret;
    private final String startString;

    @Autowired
    public DiscoveryNodeController(NodeStorage storage,
                                   @Value("${hm.agent.notifier.secret:}") String nodeSecret,
                                   @Value("${hm.agent.start}") String startString) {
        this.storage = storage;
        this.startString = startString;
        this.nodeSecret = Strings.emptyToNull(nodeSecret);
    }

    @RequestMapping(value = "/nodes/{name}", method = POST, consumes = {TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<String> registerNodeFromAgent(@RequestBody NotifierData data,
                                                        @PathVariable("name") String name,
                                                        @RequestHeader(name = NotifierData.HEADER, required = false) String nodeSecret,
                                                        @RequestParam(value = "ttl", required = false) Integer ttl,
                                                        HttpServletRequest request) {
        if (this.nodeSecret != null && !this.nodeSecret.equals(nodeSecret)) {
            return new ResponseEntity<>("Server required node auth, need correct value of '" + NotifierData.HEADER + "' header.", UNAUTHORIZED);
        }
        fixAddress(data, request);
        NodeMetrics health = createNodeHealth(data);
        if (ttl == null) {
            // it workaround, we must rewrite ttl system (it not used)
            ttl = Integer.MAX_VALUE;
        }
        try (TempAuth ta = TempAuth.asSystem()) {
            storage.updateNode(name, ttl, b -> {
                b.addressIfNeed(data.getAddress());
                b.mergeHealth(health);
            });
        }
        log.info("Update node: {}, health: {}", name, health);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void fixAddress(NotifierData data, HttpServletRequest request) {
        String host = request.getRemoteHost();
        String addrs = data.getAddress();
        String declaredHost  = AddressUtils.getHost(addrs);
        if(AddressUtils.isLocal(declaredHost)) {
            data.setAddress(AddressUtils.setHost(addrs, host));
        }
    }

    private NodeMetrics createNodeHealth(NotifierData nad) {
        SysInfo system = nad.getSystem();
        NodeMetrics.Builder nhb = NodeMetrics.builder();
        nhb.setTime(nad.getTime());
        if (system != null) {
            SysInfo.Memory mem = system.getMemory();
            if (mem != null) {
                nhb.setSysMemAvail(mem.getAvailable());
                nhb.setSysMemTotal(mem.getTotal());
                nhb.setSysMemUsed(mem.getUsed());
            }
            Map<String, SysInfo.Disk> disks = system.getDisks();
            if (disks != null) {
                for (Map.Entry<String, SysInfo.Disk> disk : disks.entrySet()) {
                    SysInfo.Disk value = disk.getValue();
                    if (value == null) {
                        continue;
                    }
                    long used = value.getUsed();
                    nhb.addDisk(new DiskInfo(disk.getKey(), used, value.getTotal()));
                }
            }
            Map<String, SysInfo.Net> net = system.getNet();
            if (net != null) {
                for (Map.Entry<String, SysInfo.Net> nic : net.entrySet()) {
                    if (nic == null) {
                        continue;
                    }
                    SysInfo.Net nicValue = nic.getValue();
                    NetIfaceCounter counter = new NetIfaceCounter(nic.getKey(), nicValue.getBytesIn(), nicValue.getBytesOut());
                    nhb.addNet(counter);
                }
            }
            nhb.setSysCpuLoad(system.getCpuLoad());
        }

        //we can resolve healthy through analysis of disk and mem availability
        nhb.setHealthy(true);
        return nhb.build();
    }

    @RequestMapping(value = "/agent/", method = GET)
    public String agent(HttpServletRequest request) {
        return StrSubstitutor.replace(startString,
                of("secret", nodeSecret == null ? "" : "-e \"hm_agent_notifier_secret=" + nodeSecret + "\"",
                        "server", getServerAddress(request)), "{", "}");
    }


    private String getServerAddress(HttpServletRequest request) {
        UriComponents build = UriComponentsBuilder
                .newInstance()
                .scheme(request.getScheme())
                .host(request.getServerName())
                .port(request.getServerPort())
                .build();
        return build.toUriString();
    }

}
