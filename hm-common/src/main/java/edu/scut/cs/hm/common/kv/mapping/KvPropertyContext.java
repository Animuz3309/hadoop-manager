package edu.scut.cs.hm.common.kv.mapping;

import com.fasterxml.jackson.databind.JavaType;

/**
 * Context to hold Kv property
 */
public interface KvPropertyContext {
    String getKey();

    JavaType getType();
}
