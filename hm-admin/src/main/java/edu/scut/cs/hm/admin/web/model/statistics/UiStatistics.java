package edu.scut.cs.hm.admin.web.model.statistics;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.scut.cs.hm.docker.model.health.Statistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import static edu.scut.cs.hm.admin.web.model.UiUtils.convertToMb;
import static edu.scut.cs.hm.admin.web.model.UiUtils.convertToPercentFromJiffies;
import static edu.scut.cs.hm.admin.web.model.UiUtils.convertToStringFromJiffies;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class UiStatistics {
    private final LocalDateTime time = LocalDateTime.now();

    private final Map<String, Object> networks;
    private final Map<String, Object> blkioStats;

    //memoryfinal _stats in MB
    //usagefinal
    private final Double memoryMBUsage; //usage
    private final Double memoryMBMaxUsage; //max_usage
    private final Double memoryMBLimit;
    private final Double memoryPercentage;

    //cpu_stfinal ats
    //cpu_usfinal age
    private final String cpuTotalUsage; //total_usage
    private final String cpuKernel;     //usage_in_kernelmode
    private final String cpuUser;       //usage_in_usermode

    private final String cpuSystem;     //system_cpu_usage

    private final Double cpuTotalPercents; //total_usage

    @SuppressWarnings("unchecked")
    public static UiStatistics from(Statistics s) {
        UiStatisticsBuilder builder = UiStatistics.builder();
        Map<String, Object> cpuStats = s.getCpuStats();
        Map<String, Object> cpuUsage = (Map<String, Object>) cpuStats.get("cpu_usage");
        Long totalUsage = toLong(cpuUsage.get("total_usage"));
        builder.cpuTotalUsage(convertToStringFromJiffies(totalUsage));

        builder.cpuKernel(convertToStringFromJiffies(toLong(cpuUsage.get("usage_in_kernelmode"))));
        builder.cpuUser(convertToStringFromJiffies(toLong(cpuUsage.get("usage_in_usermode"))));
        Long systemCpuUsage = toLong(cpuStats.get("system_cpu_usage"));
        builder.cpuSystem(convertToStringFromJiffies(systemCpuUsage / 1000_000L));

        Map<String, Object> precpuStats = s.getPrecpuStats();
        Map<String, Object> preCpuUsage = (Map<String, Object>) precpuStats.get("cpu_usage");
        Long prevTotalUsage = toLong(preCpuUsage.get("total_usage"));

        Long prevSystemUsage = toLong(precpuStats.get("system_cpu_usage"));

        Collection percpu_usage = (Collection) cpuUsage.get("percpu_usage");

        double percents = convertToPercentFromJiffies(totalUsage, prevTotalUsage, systemCpuUsage, prevSystemUsage, percpu_usage == null ? 0 : percpu_usage.size());
        builder.cpuTotalPercents(percents);

        Map<String, Object> memoryStats = s.getMemoryStats();
        Long maxUsage = toLong(memoryStats.get("max_usage"));
        Long usage = toLong(memoryStats.get("usage"));
        Long limit = toLong(memoryStats.get("limit"));
        Double percentage = Math.round(usage / limit.doubleValue() * 100_00) / 100d;

        builder.memoryMBMaxUsage(convertToMb(maxUsage));
        builder.memoryMBUsage(convertToMb(usage));
        builder.memoryMBLimit(convertToMb(limit));
        builder.memoryPercentage(percentage);

        builder.networks(s.getNetworks());
        builder.blkioStats(s.getBlkioStats());
        return builder.build();
    }


    private static Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return new Long((Integer) value);
        } else {
            return 0L;
        }
    }
}
