package edu.scut.cs.hm.admin.ds;

import edu.scut.cs.hm.admin.service.NodeStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/discovery")
public class DiscoveryNodeController {

    private final NodeStorage storage;
    private final String nodeSecret;
    private final String startString;

    @Autowired
    public DiscoveryNodeController(NodeStorage storage,
                                   @Value("${dm.agent.notifier.secret:}") String nodeSecret,
                                   @Value("${hm.agent.start}") String startString) {
        this.storage = storage;
        this.nodeSecret = nodeSecret;
        this.startString = startString;
    }

    public ResponseEntity<String> registerNodeFromAgent() {
        return null;
    }
}
