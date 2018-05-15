package edu.scut.cs.hm.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public final class ProcessUtils {
    private ProcessUtils() {}

    /**
     * Get system ProcessID of specified process. <p/>
     * Currently it method work only on unix systems.
     * @param process
     * @return PID or -1 on fail
     */
    public static int getPid(Process process) {
        Class<? extends Process> clazz = process.getClass();
        try {
            Field field = clazz.getDeclaredField("pid");
            if(field.isAccessible()) {
                field.setAccessible(true);
            }
            return (int)field.get(process);
        } catch (IllegalAccessException|NoSuchFieldException e) {
            return -1;
        }
    }
}
