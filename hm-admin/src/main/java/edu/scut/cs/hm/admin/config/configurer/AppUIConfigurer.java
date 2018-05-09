package edu.scut.cs.hm.admin.config.configurer;

import edu.scut.cs.hm.admin.web.model.user.UiHeader;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("hm.ui")
@Data
public class AppUIConfigurer {
    @NestedConfigurationProperty
    private UiHeader header = new UiHeader();
}
