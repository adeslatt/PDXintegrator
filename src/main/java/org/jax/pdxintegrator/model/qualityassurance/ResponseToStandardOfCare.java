package org.jax.pdxintegrator.model.qualityassurance;

public enum ResponseToStandardOfCare {

    NOT_ASSESSED("Not assessed"),
    COMPLETE_RESPONSE("Complete response"),
    PARTIAL_RESPONSE("Partial response"),
    STABLE_DISEASE("Stable disease"),
    PROGRESSIVE_DISEASE("Progressive disease");

    private final String name;

    ResponseToStandardOfCare(String n) {
        name=n;
    }

    public String getResponseToStandardOfCareString(){ return name; }
}

