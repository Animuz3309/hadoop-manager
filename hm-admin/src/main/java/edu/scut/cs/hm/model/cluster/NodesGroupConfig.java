package edu.scut.cs.hm.model.cluster;

/**
 * Configuration for nodes group
 */
public interface NodesGroupConfig {

    /**
     * Cluster is present simple group of ndoes
     */
    String TYPE_DEFAULT = "DEFAULT";

    /**
     * Cluster contains nodes united by standalone swarm
     */
    String TYPE_SWARM = "SWARM";

    /**
     * Cluster contains nodes united by docker in swarm mode
     */
    String TYPE_DOCKER = "DOCKER";

    /**
     * @see NodesGroup#getName()
     * @return
     */
    String getName();
    void setName(String name);

    /**
     * @see NodesGroup#getTitle()
     * @return
     */
    String getTitle();
    void setTitle(String title);

    /**
     * @see NodesGroup#getImageFilter()
     * @return
     */
    String getImageFilter();
    void setImageFilter(String imageFilter);

    /**
     * @see NodesGroup#getDescription()
     * @return
     */
    String getDescription();
    void setDescription(String description);

    static <T extends NodesGroupConfig> T copy(NodesGroupConfig src, T dst) {
        dst.setName(src.getName());
        dst.setTitle(src.getTitle());
        dst.setImageFilter(src.getImageFilter());
        dst.setDescription(src.getDescription());
        return dst;
    }
}
