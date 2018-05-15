package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.model.container.AbstractParser;
import edu.scut.cs.hm.model.container.ContainerCreationContext;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
@Slf4j
public class DefaultParser extends AbstractParser {

    private final DefaultConversionService defaultConversionService = new DefaultConversionService();

    @Override
    public void parse(String fileName, ContainerCreationContext context) {
        parse(fileName, context, ".props");
        parse(fileName, context, ".yml");

    }

    @Override
    public void parse(File file, ContainerCreationContext context) {
       //todo
    }

    @Override
    public void parse(Map<String, Object> map, ContainerCreationContext context) {

        ContainerSource arg = new ContainerSource();
        context.addCreateContainerArg(arg);
        parse(map, arg);

    }

    @Override
    public void parse(Map<String, Object> map, ContainerSource arg) {
       //todo

    }


    protected void parse(String fileName, ContainerCreationContext context, String extension) {
        File initialFile = new File(fileName + extension);
        log.info("checking for existing file {}", initialFile);
        if (initialFile.exists()) {
            log.info("ok. parsing file {}", initialFile);
            parse(initialFile, context);
        }
    }

}
