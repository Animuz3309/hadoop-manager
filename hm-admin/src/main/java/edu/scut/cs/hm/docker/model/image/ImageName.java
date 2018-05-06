package edu.scut.cs.hm.docker.model.image;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Parsed representation of image name. <p/>
 * Note that all fields can not be null, when it not have any data then it present as empty string.
 */
@Data
public class ImageName {

    private static final String SHA256 = "sha256:";
    public static final String TAG_LATEST = "latest";
    /**
     * Docker use below string constant as tag when can not find any tag or name for image.
     */
    public static final String NONE = "<none>";
    /**
     * Docker use below string constant as name when can not find any tag or name for image.
     */
    public static final String NONE_NAME = NONE + ":" + NONE;
    /**
     * Length of id which is used as name.
     */
    public static final int NAME_ID_LEN = 12;

    private final String registry;
    private final String name;
    private final String tag;
    private final String fullName;
    private final String id;

    // do not publish this constructor
    ImageName(String registry, String name, String tag, String id) {
        this.registry = registry;
        this.name = name;
        this.tag = tag;
        this.id = id;
        this.fullName = toFullName(registry, name, tag);
    }

    private ImageName(String src, String id) {
        this.fullName = src;
        this.id = id;
        final int len = src.length();
        int registryEnd = src.indexOf('/');
        int tagBegin = src.indexOf(':', registryEnd);
        if(tagBegin == -1) {
            tagBegin = len;
        }
        String registry = registryEnd < 0 ? "" : src.substring(0, registryEnd);
        if(!isRegistry(registry)) {
            registry = "";
            registryEnd = -1;
        }
        this.registry = registry;
        this.name = src.substring(registryEnd + 1, tagBegin);
        this.tag = (tagBegin < len)?  src.substring(tagBegin + 1) : "";
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTag() {
        return tag;
    }

    public String getRegistry() {
        return registry;
    }

    public String getName() {
        return name;
    }

    private static String toFullName(String registry, String name, String tag) {
        if(registry == null && name == null && tag == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if(StringUtils.hasText(registry)) {
            sb.append(registry).append('/');
        }
        sb.append(name);
        if(StringUtils.hasText(tag)) {
            sb.append(':').append(tag);
        }
        return sb.toString();
    }

    public static ImageName parse(String src) {
        if(src == null) {
            return null;
        }
        int at = src.indexOf('@');
        if(at < 0) {
            if(isId(src)) {
                return new ImageName("", src);
            }
            return new ImageName(src, null);
        }
        return new ImageName(src.substring(0, at), src.substring(at + 1));
    }

    /**
     * Docker allow namespaces like 'some/ubuntu' and we need to differ its from registry name
     * for that we <a href="https://github.com/docker/docker/blob/master/reference/reference.go#L176">use code from docker </a>
     * @param registry
     * @return true if it valid registry name
     */
    public static boolean isRegistry(String registry) {
        return "localhost".equals(registry) || registry.indexOf('.') > 0 || registry.indexOf(':') > 0;
    }

    /**
     * Get name of image. <p/>
     * Some images does not has any tag, then docker use {@link #NONE_NAME }, but docker client show first
     * 12 digits of imageId, we reproduce this behaviour.
     * @return
     */
    public static String getName(String name, String imageId) {
        if(name != null && !name.contains(ImageName.NONE)) {
            return name;
        }
        return nameFromId(imageId);
    }

    public static String nameFromId(String imageId) {
        int start = imageId.indexOf(':') + 1;
        return imageId.substring(start, start + ImageName.NAME_ID_LEN);
    }

    /**
     * Concatenate name and id. It support on new docker versions only.
     * @return name + '@' + id, also correct handle null.
     */
    public static String nameWithId(String name, String id) {
        String res = name;
        if(id != null) {
            if(!isId(id)) {
                throw new IllegalArgumentException("Id of '" + name + "' is invalid: " + id);
            }
            if(!StringUtils.isEmpty(res)) {
                res = res + "@" + id;
            } else {
                res = id;
            }
        }
        return res;
    }

    public static boolean isId(String image) {
        if(StringUtils.isEmpty(image)) {
            return false;
        }
        // see https://docs.docker.com/registry/spec/api/#/content-digests
        int length = SHA256.length();
        if(image.regionMatches(true, 0, SHA256, 0, length)) {
            return true;
        }
        // sometime image name is created from id,
        // usual it has ImageName.NAME_ID_LEN first symbols from id
        return edu.scut.cs.hm.common.utils.StringUtils.matchHex(image);
    }

    /**
     * Check that argument is not empty and valid image name (not an image id)
     * @see #isId(String)
     * @param image
     */
    public static void assertName(String image) {
        if (!StringUtils.hasText(image)) {
            throw new IllegalArgumentException("Image name is null or empty");
        }
        if (isId(image)) {
            throw new IllegalArgumentException(image + " is image id, but we expect name");
        }
    }

    /**
     * Return 'registry/image' name without version (also remove image id)
     * example: example.com/com.example.core:172 -> example.com/com.example.core
     * @param name
     * @return name without tag or throw exception
     */
    public static String withoutTag(String name) {
        assertName(name);
        return removeTagP(name);
    }

    private static String removeTagP(String name) {
        int tagStart = getTagStart(name);
        return name.substring(0, tagStart);
    }

    private static int getTagStart(String name) {
        int catPos = name.indexOf('@');
        int tagStart = name.lastIndexOf(':');
        int regEnd = name.indexOf('/');
        // we check that ':' is not part or registry name
        if (tagStart < 0 || tagStart <= regEnd) {
            tagStart = name.length();
        }
        if(catPos > 0 && catPos < tagStart) {
            // in thi case image has image id instead of tag, and we must remove it
            tagStart = catPos;
        }
        return tagStart;
    }

    /**
     * Return 'registry/image' name without version
     * example: example.com/com.example.core:172 -> example.com/com.example.core
     * @param name
     * @return name without tag or null
     */
    public static String withoutTagOrNull(String name) {
        if(!StringUtils.hasText(name) || isId(name)) {
            return null;
        }
        return removeTagP(name);
    }

    public static String setTag(String image, String tag) {
        assertName(image);
        if (!StringUtils.hasText(tag)) {
            return image;
        }
        int tagStart = getTagStart(image);
        return image.substring(0, tagStart) + ":" + tag;
    }
}
