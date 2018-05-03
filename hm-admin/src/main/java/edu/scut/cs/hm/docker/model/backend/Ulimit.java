package edu.scut.cs.hm.docker.model.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static com.google.common.base.Preconditions.checkNotNull;

@Data
public class Ulimit {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Soft")
    private Integer soft;

    @JsonProperty("Hard")
    private Integer hard;

    public Ulimit() {
    }

    public Ulimit(String name, int soft, int hard) {
        checkNotNull(name, "Name is null");
        this.name = name;
        this.soft = soft;
        this.hard = hard;
    }

}
