package edu.scut.cs.hm.admin.web.controller;

import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/nodes")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NodesController {
    static final String VIEW_NODE_LIST = "node/list";
    static final String MODEL_ATTR_NODES = "node";

    private final NodeStorage nodeStorage;

    @RequestMapping(value = "/", method = GET)
    public String listNodes(ModelMap modelMap) {
        List<NodeInfo> nodes = new ArrayList<>(nodeStorage.getNodes(ni -> true));
        modelMap.addAttribute(MODEL_ATTR_NODES, nodes);
        return VIEW_NODE_LIST;
    }

}
