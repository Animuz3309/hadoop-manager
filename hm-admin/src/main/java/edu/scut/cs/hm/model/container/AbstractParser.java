package edu.scut.cs.hm.model.container;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public abstract class AbstractParser implements Parser {

    protected void parse(String fileName, ContainerCreationContext context, String extension) {
        File initialFile = new File(fileName + extension);
        log.info("checking for existing file {}", initialFile);
        if (initialFile.exists()) {
            log.info("ok. parsing file {}", initialFile);
            parse(initialFile, context);
        }
    }

}
