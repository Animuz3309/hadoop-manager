package edu.scut.cs.hm.kv.etcd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.kv.DeleteDirOptions;
import edu.scut.cs.hm.kv.KeyValueStorage;
import edu.scut.cs.hm.kv.KvNode;
import edu.scut.cs.hm.kv.WriteOptions;
import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyDeleteRequest;
import mousio.etcd4j.requests.EtcdKeyPutRequest;
import mousio.etcd4j.requests.EtcdKeyRequest;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static mousio.etcd4j.responses.EtcdErrorCode.*;

/**
 * Wrapper etcd client as a KeyValueStorage
 */
@Slf4j
public class EtcdClientWrapper implements KeyValueStorage {

    private final EtcdClient etcd;
    private final String prefix;
    private final ExecutorService executor;

    public EtcdClientWrapper(EtcdClient etcd, String prefix) {
        this.etcd = etcd;
        this.prefix = prefix;

        // TODO etcd CRUD event listeners
        // execute event listener
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat(getClass().getName() + "-bus-%d")
                .setDaemon(true)
                .build());
    }

    private KvNode toNode(EtcdKeysResponse resp) {
        EtcdKeysResponse.EtcdNode node = resp.getNode();
        if (node.dir) {
            // we must not return dir value
            return KvNode.dir(node.modifiedIndex);
        }
        return KvNode.leaf(node.modifiedIndex, node.value);
    }

    private KvNode toNode(EtcdException e) {
        return KvNode.leaf(e.index, null);
    }

    @Override
    public KvNode get(String key) {
        try {
            EtcdResponsePromise<EtcdKeysResponse> send = etcd.get(key).send();
            EtcdKeysResponse resp = send.get();
            return toNode(resp);
        } catch (EtcdException e) {
            if (e.errorCode != KeyNotFound) {
                log.error("Error during fetching key", e);
            }
            // key not found return null;
            return null;
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    @Override
    public KvNode set(String key, String value) {
        try {
            EtcdResponsePromise<EtcdKeysResponse> send = etcd.put(key, value).send();
            EtcdKeysResponse resp = send.get();
            log.debug("set value {} for key {}", resp.node.value, resp.node.key);
            return toNode(resp);
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    @Override
    public KvNode set(String key, String value, WriteOptions ops) {
        EtcdKeyPutRequest req = etcd.put(key, value);
        fillPutReq(ops, req);
        try {
            EtcdKeysResponse resp = executeRequest(req);
            log.debug("set value {} for key, ops {}", resp.node.value, resp.node.key, ops);
            return toNode(resp);
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    @Override
    public KvNode setDir(String key, WriteOptions ops) {
        EtcdKeyPutRequest req = etcd.putDir(key);
        fillPutReq(ops, req);
        try {
            EtcdKeysResponse resp = executeRequest(req);
            log.debug("make dir at key {}", resp.node.key);
            return toNode(resp);
        } catch (EtcdException e) {
            // means dir exist
            if (e.errorCode == NotFile || e.errorCode == NodeExist) {
                if (ops.isFailIfExists()) {
                    throw new RuntimeException(key + " already exists.", e);
                }
                return toNode(e);
            } else {
                throw new RuntimeException("unchecked", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    private void fillPutReq(WriteOptions ops, EtcdKeyPutRequest req) {
        if (ops == null) {
            return;
        }
        // ops.isFailIfAbsent and ops.isFailIfExists will not be true at same time
        if (ops.isFailIfAbsent() || ops.isFailIfExists()) {
            req.prevExist(ops.isFailIfAbsent());
        }
        final long ttl = ops.getTtl();
        if (ttl > 0) {
            if (ttl > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("TTL value is to big: " + ttl);
            }
            req.ttl((int) ttl);
        }
        int prevIndex = ops.getPrevIndex();
        if (prevIndex > 0) {
            req.prevIndex(prevIndex);
        }
    }

    @Override
    public KvNode delDir(String key, DeleteDirOptions ops) {
        EtcdKeyDeleteRequest req = etcd.deleteDir(key);
        if (ops.isRecursive()) {
            req.recursive();
        }
        fillDeleteReq(ops, req);
        try {
            EtcdKeysResponse resp = executeRequest(req);
            log.debug("deleted key {}", resp.node.key);
            return toNode(resp);
        } catch (EtcdException e) {
            if(e.errorCode != KeyNotFound || ops.isFailIfAbsent()) {
                throw new RuntimeException("unchecked", e);
            }
            return toNode(e);
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    @Override
    public KvNode delete(String key, DeleteDirOptions ops) {
        EtcdKeyDeleteRequest req = etcd.delete(key);
        fillDeleteReq(ops, req);
        try {
            EtcdKeysResponse resp = executeRequest(req);
            log.debug("deleted key {}", resp.node.key);
            return toNode(resp);
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    private void fillDeleteReq(DeleteDirOptions ops, EtcdKeyDeleteRequest req) {
        if(ops == null) {
            return;
        }
        final int prevIndex = ops.getPrevIndex();
        if(prevIndex > 0) {
            req.prevIndex(prevIndex);
        }
    }

    private EtcdKeysResponse executeRequest(EtcdKeyRequest req) throws Exception {
        EtcdResponsePromise<EtcdKeysResponse> send = req.send();
        return send.get();
    }

    @Override
    public List<String> list(String prefix) {
        try {
            EtcdResponsePromise<EtcdKeysResponse> send = etcd.getDir(prefix).send();
            EtcdKeysResponse resp = send.get();
            return resp.node.nodes.stream().map(n -> n.key).collect(Collectors.toList());
        } catch (EtcdException e) {
            if (e.errorCode != KeyNotFound) {
                log.error("Error during fetching key", e);
            }
            // key not found return null;
            return null;
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    @Override
    public Map<String, String> map(String prefix) {
        try {
            EtcdResponsePromise<EtcdKeysResponse> send = etcd.get(prefix).recursive().send();
            EtcdKeysResponse r = send.get();
            return r.node.nodes.stream().collect(Collectors.toMap((n) -> n.key, (n) -> n.value));
        } catch (EtcdException e) {
            if (e.errorCode != KeyNotFound) {
                log.error("Error during fetching key", e);
            }
            // key not found return null;
            return null;
        } catch (Exception e) {
            throw new RuntimeException("unchecked", e);
        }
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}
