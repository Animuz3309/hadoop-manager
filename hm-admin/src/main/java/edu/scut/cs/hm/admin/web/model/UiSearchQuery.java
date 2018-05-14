package edu.scut.cs.hm.admin.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class UiSearchQuery {

    @ApiModelProperty(value = "Example: \"health.sysCpuLoad < 0.99 && health.healthy == true || health.sysMemUsed > 1000\"")
    private final String wheres;

    private final List<SearchOrder> orders;

    @Min(value = 1)
    private final int size;

    @Min(value = 0)
    private final int page;

    public enum SortOrder {
        ASC, DESC
    }

    public enum Operation {
        GREATER, LESS, EQUAL
    }

    @Value
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    public static class SearchOrder {
        @NotNull
        private final String field;
        @NotNull
        private final SortOrder order;

    }
}
