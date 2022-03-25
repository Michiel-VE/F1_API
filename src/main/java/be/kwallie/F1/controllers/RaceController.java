package be.kwallie.F1.controllers;

import be.kwallie.F1.models.response.RaceResponse;
import be.kwallie.F1.services.RaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RaceController {

    private final RaceService raceService;

    @GetMapping("/races")
    public List<RaceResponse> getAllRaces() {
        return raceService.getAllRaces();
    }
}

