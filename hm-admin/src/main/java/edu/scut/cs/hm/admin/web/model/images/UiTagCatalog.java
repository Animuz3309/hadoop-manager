package edu.scut.cs.hm.admin.web.model.images;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class UiTagCatalog implements Comparable<UiTagCatalog> {
    private final String registry;
    private final String name;
    private final String node;
    private final String tag;
    private final String image;

    private final Date created;
    private final Map<String, String> labels;

    @Override
    public int compareTo(UiTagCatalog o) {
        return tag.compareTo(o.tag) ;
    }
}
