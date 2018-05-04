package edu.scut.cs.hm.admin.component;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.config.configurer.SwarmProcessesConfigurer;
import edu.scut.cs.hm.common.utils.ProcessUtils;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.docker.model.swarm.Strategies;
import edu.scut.cs.hm.model.DockerServiceAddress;
import edu.scut.cs.hm.model.cluster.SwarmDiscoveryUrlFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.util.Arrays.asList;

/**
 * Manager of swarm processes
 */
@Slf4j
@Component
public class SwarmProcesses {

    public interface SwarmProcess extends DockerServiceAddress {
        /**
         * Wait a process start. <p/>
         * This method may throw exception if process was failed.
         */
        void waitStart();

        String getCluster();
    }

    private final class Proc implements SwarmProcess {
        private final Object lock = new Object();
        private final DockerConfig dockerConfig;
        private final int port;
        private final String host;

        private volatile int pid;
        private volatile Process process;
        private volatile Future<?> future;

        Proc(DockerConfig dockerConfig) {
            this.dockerConfig = dockerConfig;
            this.port = choosePort();
            this.host = config.getAddress();
        }

        @Override
        public String getCluster() {
            return dockerConfig.getCluster();
        }

        @Override
        public String getAddress() {
            return host + ":" + this.port;
        }

        private int choosePort() {
            int min = config.getMinPort();
            int max = config.getMaxPort();
            return min + (int) (Math.random() * (max - min));
        }

        void run() {
            log.info("Run swarm executable for {}", getProcessInfo());
            final long begin = System.currentTimeMillis();
            String sdstr = makeDiscoveryUrl(this);
            List<String> args = new ArrayList<>(asList(config.getPath(), "manage", "-H", getAddress()));
            Strategies strategy = MoreObjects.firstNonNull(dockerConfig.getStrategy(), config.getStrategy());
            if (strategy != null && strategy != Strategies.DEFAULT) {
                args.addAll(asList("--strategy", strategy.value()));
            }
            args.add(sdstr);
            ProcessBuilder pb = new ProcessBuilder(args);
            try {
                log.info("Trying to start {} process {} ", getProcessInfo(), pb.command());
                String datePrefix = getDatePrefix();
                File outf = File.createTempFile(datePrefix + "-out", ".log", logDir);
                File errf = File.createTempFile(datePrefix + "-err", ".log", logDir);
                pb.redirectError(errf);
                pb.redirectOutput(outf);
                process = pb.start();
                waitProcessStart();
                if(!process.isAlive()) {
                    log.error("Process {} unexpected dead with code {}", getProcessInfo(), process.exitValue());
                }
                log.info("Process {}, has been started in {} seconds.", getProcessInfo(), (System.currentTimeMillis() - begin)/ 1000L);
                this.pid = ProcessUtils.getPid(process);
            } catch (Throwable e) {
                log.error("While run swarm executable for {} " , getProcessInfo(), e);
                if(process != null) {
                    process.destroy();
                }
            }
        }

        private void waitProcessStart() throws Exception {
            RestTemplate rt = new RestTemplate();
            URI url = new URI("http", null, host, port, "/version", null, null);
            final int tries = 4;
            final int maxWaitOnStart = config.getMaxWaitOnStart();
            final long sleepTime = (maxWaitOnStart * 1000L) / (long)tries;
            int i = tries;
            while(i > 0) {
                try {
                    String res = rt.getForObject(url, String.class);
                    if(res != null) {
                        return;
                    }
                } catch(ResourceAccessException e) {
                    //wait for some tome before next trie
                    Thread.sleep(sleepTime);
                }
                i--;
            }
            throw new Exception("Process of '" + getCluster() + "' cluster not response at " + url + " in " + maxWaitOnStart + " seconds.");
        }


        private String getDatePrefix() {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
        }

        private String getProcessInfo() {
            return "cluster '" + this.getCluster() + "' at '"
                    + this.getAddress() + "'";
        }

        void executeIfNeed() {
            if(this.future == null) {
                synchronized(lock) {
                    if(this.future == null) {
                        this.future = executor.submit(this::run);
                    }
                }
            }
        }

        /**
         * Wait process start.
         */
        public void waitStart() {
            Future<?> future;
            synchronized (lock) {
                future = this.future;
            }
            long timeout = (long) (config.getMaxWaitOnStart() * 1.5 /* time must be greater than  */);
            try {
                future.get(timeout, TimeUnit.SECONDS);
            } catch(Exception e) {
                throw Throwables.asRuntime(e.getCause());
            }
        }

        /**
         * Checks that process has been run, and ended after it, or running process is failed.
         * @return
         */
        boolean isEnded() {
            Future<?> future;
            synchronized (lock) {
                future = this.future;
            }
            Process proc = this.process;
            return future != null && future.isDone() &&
                    proc != null && !proc.isAlive();
        }

    }

    private final SwarmProcessesConfigurer config;
    private final ConcurrentMap<String, Proc> procs = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final ScheduledExecutorService watcher;
    private final File logDir;
    private final SwarmDiscoveryUrlFunction discoveryUrlFunc;

    @Autowired
    public SwarmProcesses(SwarmProcessesConfigurer config,
                          SwarmDiscoveryUrlFunction discoveryUrlFunc) {
        this.config = config.clone();

        this.discoveryUrlFunc = discoveryUrlFunc;

        this.logDir = resolveLogDir();
        this.logDir.mkdirs();

        final String prefix = getClass().getSimpleName() + "-swarm-";
        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(prefix + "runner-%d")
                .build());
        this.watcher = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(prefix + "watcher-%d").build());

        this.watcher.scheduleAtFixedRate(this::watch, 10L, 2L, TimeUnit.SECONDS);
    }

    private void watch() {
        List<Proc> procs = new ArrayList<>(this.procs.values());
        for(Proc proc: procs) {
            if(proc.isEnded()) {
                this.procs.remove(proc.getCluster(), proc);
            }
        }
    }

    private File resolveLogDir() {
        String logDirName = this.config.getLogDir();
        File logDir = null;
        if(logDirName != null) {
            logDir = new File(logDirName);
            logDir.mkdirs();
            if(!logDir.isDirectory()) {
                log.error("Bad value of logDir '" + logDir.getAbsolutePath() + "': is not a directory.");
                logDir = null;
            }
        }
        if(logDir == null) {
            logDir = new File(System.getProperty("java.io.tmpdir") + "/hm-logs");
        }
        return logDir;
    }

    private String makeDiscoveryUrl(SwarmProcess proc) {
        return discoveryUrlFunc.supply(proc);
    }

    public SwarmProcess addCluster(DockerConfig cc) {
        String clusterId = cc.getCluster();
        Proc proc = procs.computeIfAbsent(clusterId, s -> new Proc(cc));
        int curr = procs.size();
        int max = config.getMaxProcesses();
        if(curr >= max) {
            //we cannot run yet another process
            procs.remove(clusterId, proc);
            throw new IllegalStateException("Count of running processes: " + curr + " that's greater than maximum limit: " + max);
        }
        proc.executeIfNeed();
        return proc;
    }

    @PreDestroy
    public void close() {
        this.executor.shutdownNow();
        this.watcher.shutdown();
    }
}
