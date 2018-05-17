package edu.scut.cs.hm.admin.web.model.msg;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.util.Date;

@Data
public class UiAddSubscription {
    /**
     * Evant source
     */
    private String source;
    private int historyCount;
    private Date historySince;

    @JsonCreator
    public static UiAddSubscription fromString(String source) {
        UiAddSubscription uas = new UiAddSubscription();
        uas.setHistoryCount(Integer.MAX_VALUE);// enable history
        uas.setSource(source);
        return uas;
    }
}
