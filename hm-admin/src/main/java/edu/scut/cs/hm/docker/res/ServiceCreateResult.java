package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceCreateResult extends ServiceCallResult {
    @JsonProperty("ID")
    private String serviceId;
}
