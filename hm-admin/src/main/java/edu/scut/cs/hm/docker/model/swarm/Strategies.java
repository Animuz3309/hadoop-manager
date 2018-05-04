package edu.scut.cs.hm.docker.model.swarm;

public enum  Strategies {

    DEFAULT,
    SPREAD,
    BINPACK,
    RANDOM;

    public String value() {
        return name().toLowerCase();
    }
}
