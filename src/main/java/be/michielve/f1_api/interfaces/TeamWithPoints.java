package be.michielve.f1_api.interfaces;

import java.util.UUID;

public interface TeamWithPoints {
    UUID getId();
    String getName();
    String getShortName();
    String getCountry();
    String getBase();
    Integer getTotalPoints();
}