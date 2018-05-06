package edu.scut.cs.hm.docker.mng;


public interface ResponseStreamProcessor<T> {
    void processResponseStream(StreamContext<T> context);

}
