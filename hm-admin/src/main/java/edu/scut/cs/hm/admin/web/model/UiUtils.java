package edu.scut.cs.hm.admin.web.model;

import edu.scut.cs.hm.admin.web.model.container.UiContainer;
import edu.scut.cs.hm.admin.web.model.container.UiContainerIface;
import edu.scut.cs.hm.admin.web.model.error.UiError;
import edu.scut.cs.hm.common.utils.Booleans;
import edu.scut.cs.hm.docker.model.discovery.Node;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.ContainerBaseIface;
import edu.scut.cs.hm.model.application.Application;
import edu.scut.cs.hm.model.application.ApplicationService;
import edu.scut.cs.hm.model.cluster.ClusterUtils;
import edu.scut.cs.hm.model.container.ContainerUtils;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import org.joda.time.LocalTime;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

/**
 * some utilities
 */
public final class UiUtils {

    public static HttpStatus toStatus(ResultCode resultCode) {
        switch (resultCode) {
            case OK:
                return OK;
            case CONFLICT:
                return CONFLICT;
            case NOT_FOUND:
                return NOT_FOUND;
            case NOT_MODIFIED:
                return NOT_MODIFIED;
            default:
                return INTERNAL_SERVER_ERROR;
        }
    }

    public static void convertCode(ServiceCallResult res, UiError error) {
        error.setCode(toStatus(res.getCode()).value());
    }

    public static ResponseEntity<UiResult> createResponse(ServiceCallResult result) {
        ResultCode code = result.getCode();

        String message = code + " " + (result.getMessage() == null ? "" : result.getMessage());
        if (code == ResultCode.OK) {
            return okResponse(message);
        } else {
            return errResponse(code, message);
        }
    }

    public static ResponseEntity<UiResult> errResponse(ResultCode code, String message) {
        UiError err = new UiError();
        err.setMessage(message);
        err.setCode(toStatus(code).value());
        return new ResponseEntity<>(err, toStatus(code));
    }

    public static ResponseEntity<UiResult> okResponse(String message) {
        UiResult res = new UiResult();
        res.setMessage(message);
        res.setCode(OK.value());
        return new ResponseEntity<>(res, OK);
    }

    public static double convertToGB(long memory) {
        return Math.round(memory * 100 / (1024 * 1024 * 1024)) / 100d;
    }

    public static double convertToKb(long memory) {
        return Math.round(memory / 1024);
    }

    public static double convertToMb(long memory) {
        return Math.round(memory * 100 / (1024 * 1024)) / 100d;
    }

    public static double convertToMb(int memory) {
        return convertToMb((long)memory);
    }

    public static String convertToStringFromJiffies(Long jiffies) {
        LocalTime timeOfDay = LocalTime.fromMillisOfDay(jiffies / 1000_000L);
        return timeOfDay.toString("HH:mm:ss");
    }

    public static double convertToPercentFromJiffies(Long cpu, Long prevCpu, Long system, Long previousSystem, int cores) {

        double cpuPercent = 0d;
        // calculate the change for the cpu usage of the container in between readings
        double cpuDelta = cpu - prevCpu;
        // calculate the change for the entire system between readings
        double systemDelta = system - previousSystem;

        if (systemDelta > 0d && cpuDelta > 0d) {
            cpuPercent = (cpuDelta / systemDelta) * cores * 100;
        }
        return round(cpuPercent, 2);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String nodeTostring(Node node) {
        if (node == null) {
            // docker service instead of swarm does not return node
            return "";
        }
        return "Name: " + node.getName() + "; Address: " + node.getAddr();
    }

    public static void resolveContainerLock(UiContainerIface target, ContainerBaseIface src) {
        boolean ourContainer = ContainerUtils.isOurContainer(src);
        if (ourContainer || Booleans.valueOf(src.getLabels().get("lock"))) {
            target.setLock(true);
            if (ourContainer) {
                target.setLockCause("with this container we launched");
            } else {
                target.setLockCause("lock by appropriate label");
            }
        }
    }

    /**
     * Make app -> containerId map.
     * @param applicationService
     * @param cluster
     * @return
     */
    public static Map<String, String> mapAppContainer(ApplicationService applicationService, NodesGroup cluster) {
        try {
            Map<String, String> containerApp = new HashMap<>();
            if(ClusterUtils.isDockerBased(cluster)) {
                addContainerMapping(applicationService, cluster.getName(), containerApp);
            } else {
                for(String child: cluster.getGroups()) {
                    addContainerMapping(applicationService, child, containerApp);
                }
            }
            return containerApp;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static void addContainerMapping(ApplicationService applicationService, String cluster, Map<String, String> map) {
        List<Application> applications = applicationService.getApplications(cluster);
        for (Application application : applications) {
            // container belongs to single application
            application.getContainers().forEach(container -> map.put(container, application.getName()));
        }
    }

    /**
     * Resolve public address of this app, if it not configured return null.
     * @param environment env
     * @return host:port or null
     */
    public static String getAppAddress(Environment environment) {
        String host = environment.getProperty("server.address");
        if(host == null) {
            return null;
        }
        return host + ":" + environment.getProperty("server.port");
    }

    public static List<UiContainer> sortAndFilterContainers(List<UiContainer> list) {
        List<UiContainer> filteredContainers = filterEmptyContainers(list);
        Collections.sort(filteredContainers);
        return filteredContainers;
    }

    /**
     * workaround for preventing getting empty lines at UI
     */
    public static List<UiContainer> filterEmptyContainers(List<UiContainer> list) {
        return list.stream().filter(c -> StringUtils.hasText(c.getNode())).collect(Collectors.toList());
    }

}
