package edu.scut.cs.hm.common.kv.mapping;

/*
    KeyValueStorage 是一个抽象的key-value存储器，它存储的方式是一个string key 对应一个 KvNode

    我们生成了一个实现，基于Etcd的KeyValueStorage，我们将etcd中的路径path作为key，将获取的etcd node 解包为KvNode

    举例：@see src/test/java/edu.scut.cs.hm.common.kv.mapping.KvMapTest
        存储一个对象Entry
        1. 将需要存储的Field用@KvMapping注解
        <code>
            public static class Entry {
                private static AtomicInteger counter = new AtomicInteger();
                @KvMapping
                private String text;
                @KvMapping
                private int number;

                public Entry() {
                    this.text = UUIDs.longUid();
                    this.number = counter.incrementAndGet();
                }
            }
        </code>
        2. 使用KvMap.Builder创建KvMap
        <code>
            KvMap<Entry> map = KvMap.builder(Entry.class)               // KvMap保存的对象类型
                .mapperFactory(factory)                                        // 传入KvMapperFactory
                .path(factory.getStorage().getPrefix() + "/entries")    // entry对象保存在etcd上的路径前缀
                .build();
        </code>
       在KvMap#KvMap(Builder)构造函数里，会通过调用传入的KvMapperFactory#buildClassMapper方法创建KvClassMapper
       KvClassMapper会调用KvMapperFactory#loadProps方法将被@KvMapping注解的Entry的Field转变成KvProperty，从而便于
       未来通过调用AbstractMapping实现Entry对象与保存在KeyValueStorage内的记录的映射

       3.
       - 如/entries 是保存所有Entry对象的目录
       - /entries/one 是保存了key值为one的entry对象，‘one’是KvMap#put(String, T)中的key值
         - /entries/one/@class不再是一个目录，它拥有值，值为Entry对象的class type
         - /entries/one/text 保存了Entry对象的的text值
         - /entries/one/number 保存了Entry对象的的number值
 */