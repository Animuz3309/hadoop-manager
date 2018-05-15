package edu.scut.cs.hm.agent.notifier;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for node agent
 */
@Data
public class NotifierData {
    public static final String HEADER = "X-Auth-Node";

    private ZonedDateTime time;
    private String name;
    private String address;
    private SysInfo system;
}