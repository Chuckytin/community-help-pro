package com.communityhelp.app.common.openroute.model;

public enum TransportMode {

    FOOT_WALKING("foot-walking"),
    DRIVING_CAR("driving-car"),
    CYCLING_REGULAR("cycling-regular");

    public final String apiValue;

    TransportMode(String apiValue) {
        this.apiValue = apiValue;
    }
}
