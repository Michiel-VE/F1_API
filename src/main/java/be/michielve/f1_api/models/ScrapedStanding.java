package be.michielve.f1_api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ScrapedStanding {
    public int position;
    public String firstName;
    public String lastName;
    public String driver_code;
    public String nationality;
    public String team;
    public int points;
}
