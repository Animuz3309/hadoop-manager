package edu.scut.cs.hm.admin.web.model.msg;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UiCountResult {
    @Data
    public static class FilteredResult {
        private String filter;
        private int count;
    }
    private String source;
    private int count;
    private LocalDateTime from;
    private List<FilteredResult> filtered;
}