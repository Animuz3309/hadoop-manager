package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.common.fc.FbStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FbConfiguration {
    @Bean
    FbStorage fileBackedStorage(@Value("${hm.fbstorage.location}") String storagePath) {
        return FbStorage.builder()
                .maxFileSize(1024 * 1024 * 512)
                .path(storagePath)
                .build();
    }
}
