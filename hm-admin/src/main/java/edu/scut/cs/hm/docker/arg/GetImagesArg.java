package edu.scut.cs.hm.docker.arg;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetImagesArg {

    /**
     * Instance which only has all=true
     */
    public static final GetImagesArg ALL = GetImagesArg.builder().all(true).build();
    /**
     * Instance which only has all=false
     */
    public static final GetImagesArg NOT_ALL = GetImagesArg.builder().all(false).build();

    private final boolean all;
    private final String name;
}