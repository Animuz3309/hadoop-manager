package edu.scut.cs.hm.admin.web.model.images;

import edu.scut.cs.hm.docker.model.image.ImageNameComparator;
import lombok.Data;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

@Data
public class UiImageData implements Comparable<UiImageData> {
    private final String id;
    private Date created;
    private long size;
    private final TreeSet<String> tags = new TreeSet<>(ImageNameComparator.getTagsComparator());
    private final Set<String> nodes = new TreeSet<>();

    public UiImageData(String id) {
        this.id = id;
    }

    /**
     * Deployed images have higher priority
     * @param o
     * @return
     */
    @Override
    public int compareTo(UiImageData o) {
        int compare = Integer.compare(nodes.size(), o.getNodes().size());
        if (compare == 0) {
            Date lc = created;
            Date rc = o.getCreated();
            if (lc == null || rc == null) {
                return lc != null ? 1 : (rc != null ? -1 : 0);
            }
            compare = lc.compareTo(rc);
        }
        return compare;
    }
}
