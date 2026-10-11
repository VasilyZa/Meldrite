package dev.meldrite.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public record CompatibilityReport(String status, List<String> checks, List<String> limitations) {
    public CompatibilityReport {
        checks = List.copyOf(checks);
        limitations = List.copyOf(limitations);
    }
    public String json() throws Exception {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }
}
