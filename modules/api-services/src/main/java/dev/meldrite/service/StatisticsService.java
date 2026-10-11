package dev.meldrite.service;
import dev.meldrite.bridge.*;
import dev.meldrite.foundation.StatisticRequest;
import java.util.*;
public final class StatisticsService implements AutoCloseable {
    private final ExecutionPort execution;private final StatisticsPort statistics;private volatile boolean closed;
    public StatisticsService(ExecutionPort execution,StatisticsPort statistics){this.execution=Objects.requireNonNull(execution);this.statistics=Objects.requireNonNull(statistics);}
    private void ready(UUID account,StatisticRequest request){if(!execution.isPrimaryThread())throw new IllegalStateException("PRIMARY_THREAD_REQUIRED");if(closed)throw new IllegalStateException("STATISTICS_SERVICE_CLOSED");Objects.requireNonNull(account);Objects.requireNonNull(request);}
    public int get(UUID account,StatisticRequest request){ready(account,request);int value=statistics.get(account,request);ready(account,request);if(value<0)throw new IllegalStateException("INVALID_NATIVE_STATISTIC");return value;}
    public void set(UUID account,StatisticRequest request,int value){ready(account,request);if(value<0)throw new IllegalArgumentException("Negative statistic");int before=get(account,request);if(!statistics.compareAndSet(account,request,before,value))throw new IllegalStateException("STATISTIC_CHANGED");}
    public void adjust(UUID account,StatisticRequest request,int amount,boolean increment){ready(account,request);if(amount<=0)throw new IllegalArgumentException("Nonpositive statistic adjustment");int before=get(account,request),after;try{after=increment?Math.addExact(before,amount):Math.subtractExact(before,amount);}catch(ArithmeticException failure){throw new IllegalArgumentException("Statistic overflow",failure);}if(after<0)throw new IllegalArgumentException("Negative statistic");if(!statistics.compareAndSet(account,request,before,after))throw new IllegalStateException("STATISTIC_CHANGED");}
    public void close(){closed=true;}
}
