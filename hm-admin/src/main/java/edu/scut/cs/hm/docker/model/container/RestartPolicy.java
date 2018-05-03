package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestartPolicy {

    public static final String NO = "no";
    public static final String ALWAYS = "always";
    public static final String ON_FAILURE = "on-failure";
    public static final String UNLESS_STOPPED = "unless-stopped";

    private final int maximumRetryCount;

    private final String name;

    private RestartPolicy(@JsonProperty("Name") String name,
                          @JsonProperty("MaximumRetryCount") int maximumRetryCount) {
        this.maximumRetryCount = maximumRetryCount;
        this.name = name;
    }

    public static RestartPolicy noRestart() {
        return new RestartPolicy(NO, 0);
    }

    public static RestartPolicy alwaysRestart() {
        return new RestartPolicy(ALWAYS, 0);
    }

    public static RestartPolicy unlessStopped() {
        return new RestartPolicy(UNLESS_STOPPED, 0);
    }

    public static RestartPolicy onFailureRestart(int maximumRetryCount) {
        return new RestartPolicy(ON_FAILURE, maximumRetryCount);
    }

    public static RestartPolicy parse(String serialized) throws IllegalArgumentException {
        try {
            String[] parts = serialized.split(":");
            String name = parts[0];
            switch (name) {
                case NO:
                    return noRestart();
                case ALWAYS:
                    return alwaysRestart();
                case UNLESS_STOPPED:
                    return unlessStopped();
                case ON_FAILURE:
                    int count = 0;
                    if (parts.length == 2) {
                        count = Integer.parseInt(parts[1]);
                    }
                    return onFailureRestart(count);
                default:
                    throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing RestartPolicy '" + serialized + "'");
        }
    }

    /**
     * Returns a string representation of this {@link RestartPolicy}. The format is <code>name[:count]</code>, like the argument in
     * {@link #parse(String)}.
     *
     * @return a string representation of this {@link RestartPolicy}
     */
    @Override
    public String toString() {
        String result = name.isEmpty() ? "no" : name;
        return maximumRetryCount > 0 ? result + ":" + maximumRetryCount : result;
    }

}