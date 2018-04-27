package edu.scut.cs.hm.common.kv.mapping;

import com.fasterxml.jackson.databind.ObjectReader;
import edu.scut.cs.hm.common.kv.KvNode;

class LeafMapping<T> extends AbstractMapping<T> {

    LeafMapping(KvMapperFactory mapper, Class<T> type) {
        super(mapper, type);
    }


    @Override
    void save(String path, T object, KvSaveCallback callback) {
        try {
            String value = getObjectMapper().writeValueAsString(object);
            KvNode res = getStorage().set(path, value);
            if(callback != null) {
                callback.call(null, res);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can not save object at path: " + path, e);
        }
    }

    @Override
    void load(String path, T object) {
        KvNode node = getStorage().get(path);
        if(node == null) {
            return;
        }
        String str = node.getValue();
        try {
            ObjectReader reader = getObjectMapper().reader();
            reader.withValueToUpdate(object).readValue(str);
        } catch (Exception e) {
            throw new RuntimeException("Can not save object at path: " + path, e);
        }
    }

    @Override
    <S extends T> S load(String path, String name, Class<S> type) {
        KvNode node = getStorage().get(path);
        if(node == null) {
            return null;
        }
        String str = node.getValue();
        try {
            return getObjectMapper().readValue(str, type);
        } catch (Exception e) {
            throw new RuntimeException("Can not load object at path: " + path, e);
        }
    }
}
