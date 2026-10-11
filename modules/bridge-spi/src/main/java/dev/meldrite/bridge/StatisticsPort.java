package dev.meldrite.bridge;
import dev.meldrite.foundation.StatisticRequest;
import java.util.UUID;
public interface StatisticsPort {
    /** Known absent statistic is zero; unknown keys/unsupported codecs fail explicitly. */
    int get(UUID account,StatisticRequest request);
    /** False leaves every statistic unchanged. */
    boolean compareAndSet(UUID account,StatisticRequest request,int expected,int replacement);
}
