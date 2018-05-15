package edu.scut.cs.hm.agent.gather;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.agent.notifier.SysInfo;
import edu.scut.cs.hm.common.utils.JacksonUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SysInfoGatherTest {

    private ObjectMapper mapper = JacksonUtils.objectMapperBuilder();
    @Test
    public void getSysInfo() throws Exception {
        SysInfoGather sysInfoGather = new SysInfoGather("/");
        SysInfo info = sysInfoGather.getSysInfo();
        assertNotNull(info);
        System.out.println(mapper.writeValueAsString(info));
    }
}