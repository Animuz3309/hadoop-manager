package edu.scut.cs.hm.docker;

import com.google.common.base.Strings;
import edu.scut.cs.hm.docker.model.container.ContainerConfig;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.node.NodeInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Some utils
 */
public final class DockerUtils {

    public static final String RESTART = "restart";
    public static final String SCALABLE = "scalable";

    static ServiceCallResult getServiceCallResult(ResponseEntity<?> res) {
        return getServiceCallResult(res, new ServiceCallResult());
    }

    static <T extends ServiceCallResult> T getServiceCallResult(ResponseEntity<?> response, T callResult) {
        Assert.notNull(response, "ResponseEntity is null");
        Assert.notNull(callResult, "callResult is null");
        Object body = response.getBody();
        if (body != null) {
            callResult.setMessage(body.toString());
        }
        setCode(response, callResult);
        return callResult;
    }

    /**
     * Set code from service response entity
     * @param response
     * @param result
     */
    public static <T extends ServiceCallResult> void setCode(ResponseEntity<?> response, T result) {
        Assert.notNull(response, "ResponseEntity is null");
        setCode(response.getStatusCode(), result);
    }

    /**
     * Set code from httpCode
     * @param httpCode
     * @param result
     */
    public static <T extends ServiceCallResult> void setCode(HttpStatus httpCode, T result) {
        Assert.notNull(httpCode, "httpCode is null");
        Assert.notNull(result, "result is null");
        ResultCode code;
        switch (httpCode.value()) {
            case 200:
            case 201:
            case 202:
            case 204:
                code = ResultCode.OK;
                break;
            case 304:
                code = ResultCode.NOT_MODIFIED;
                break;
            case 404:
                code = ResultCode.NOT_FOUND;
                break;
            case 409:
                code = ResultCode.CONFLICT;
                break;
            default:
                //due doc 500 - is error, but any other than above codes is undefined we interpret this cases as error also.
                code = ResultCode.ERROR;
        }
        result.setStatus(httpCode);
        result.setCode(code);

    }

    /**
     * Return full host name
     *
     * @param config
     * @return
     */
    public static String getFullHostName(ContainerConfig config) {
        String domainname = config.getDomainName();
        String hostname = config.getHostName();
        if (Strings.isNullOrEmpty(hostname)) {
            return null;
        }
        if (Strings.isNullOrEmpty(domainname)) {
            return hostname;
        }
        return hostname + "." + domainname;
    }

    public static List<String> listNodes(Collection<NodeInfo> nodes) {
        List<String> list = new ArrayList<>();
        nodes.forEach(ni -> list.add(ni.getName()));
        return list;
    }

}
