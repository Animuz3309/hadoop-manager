package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import edu.scut.cs.hm.common.utils.Keeper;
import org.springframework.util.MimeType;

/**
 * Defines platform extensions that will be registered with {@link ObjectMapper}
 */
public class DmJacksonModule extends Module {

    /**
     * Method that returns a display that can be used by Jackson
     * for informational purposes, as well as in associating extensions with
     * module that provides them.
     */
    @Override
    public String getModuleName() {
        return getClass().getName();
    }

    /**
     * Method that returns version of this module. Can be used by Jackson for
     * informational purposes.
     */
    @Override
    public Version version() {
        return new Version(1, 0, 0, null, null, null);
    }

    /**
     * Method called by {@link ObjectMapper} when module is registered.
     * It is called to let module register functionality it provides,
     * using callback methods passed-in context object exposes.
     */
    @Override
    public void setupModule(SetupContext setupContext) {
        SimpleSerializers serializers = new SimpleSerializers();
        addSerializers(serializers);
        setupContext.addSerializers(serializers);

        SimpleDeserializers deserializers = new SimpleDeserializers();
        addDeserializers(deserializers);
        setupContext.addDeserializers(deserializers);
        setupContext.addBeanDeserializerModifier(new KeeperBeanDeserializerModifier());
    }

    @SuppressWarnings("unchecked")
    private void addDeserializers(SimpleDeserializers deserializers) {
        deserializers.addDeserializer(MimeType.class, new MimeTypeDeserializer());
        deserializers.addDeserializer(Keeper.class, (JsonDeserializer) new KeeperDeserializer());
    }

    private void addSerializers(SimpleSerializers serializers) {
        serializers.addSerializer(MimeType.class, new MimeTypeSerializer());
        serializers.addSerializer(Keeper.class, new KeeperSerializer());
    }

}
