package edu.scut.cs.hm.common.utils;

import com.jcabi.manifests.Manifests;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tool for gathering application info
 */
public class AppInfo {

    /**
     * extract '$artifactId' from manifest (Implementation-Title) or other places.
     *
     * @return
     */
    public static String getApplicationName() {
        return getValue("hm-admin-info-name");
    }

    /**
     * extract '$version' from manifest (Implementation-Version) or other places.
     *
     * @return
     */
    public static String getApplicationVersion() {
        return getValue("hm-admin-info-version");
    }

    public static String getBuildRevision() {
        return getValue("hm-admin-info-buildRevision");
    }

    public static OffsetDateTime getBuildTime() {
        try {
            return OffsetDateTime.parse(getValue("hm-admin-info-date"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.now();
        }
    }

    private static String getValue(String key) {
        try {
            // we expect error like IllegalArgumentException: Attribute 'dm-cluman-info-version' not found in MANIFEST.MF file(s) among 90 other attribute(s):
            // which appear anytime when we run app without jar file
            return Manifests.read(key);
        } catch (IllegalArgumentException e) {
            return "MANIFEST_WAS_NOT_FOUND";
        }
    }
}
