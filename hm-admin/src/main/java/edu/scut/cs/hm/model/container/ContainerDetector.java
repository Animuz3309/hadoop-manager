package edu.scut.cs.hm.model.container;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility which detect that we run in docker container and return info about it
 */
public final class ContainerDetector {
    private static final boolean container;
    private static final String id;

    private static final int ID_LEN = 64;

    static {
        //see http://stackoverflow.com/questions/20010199/determining-if-a-process-runs-inside-lxc-docker

        Optional<String> idopt = Optional.empty();
        try {
            Stream<String> lines = Files.lines(Paths.get("/proc/1/cgroup"));
            idopt = findId(lines);
        } catch (Exception e) {
            // we do not want to see any exceptions
        }
        id = idopt.orElse(null);

        boolean hasDockeEnv = false;
        try {
            hasDockeEnv = Files.exists(Paths.get("/.dockerenv"));
        } catch (Exception e) {
            //nothing
        }
        container = hasDockeEnv || id != null;
    }

    static Optional<String> findId(Stream<String> lines) {
        return lines.filter((s) -> s.contains("docker")).map(s -> {
            int idBegin = s.lastIndexOf('/');
            if (s.length() - idBegin < ID_LEN) {
                // we expect 64symbol id
                return null;
            }
            return s.substring(idBegin + 1);
        }).filter(Objects::nonNull).findFirst();
    }

    /**
     *
     * @return true if app running in docker container
     */
    public static boolean isContainer() {
        return container;
    }

    public static String getId() {
        return id;
    }
}
