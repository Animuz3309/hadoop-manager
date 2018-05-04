package edu.scut.cs.hm.docker.arg;

public enum Reschedule {

    ON_NODE_FAILURE("on-node-failure");

    private final String value;

    Reschedule(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
