package app.foot.controller;

import app.foot.controller.rest.Match;
import app.foot.controller.rest.Player;
import app.foot.controller.rest.PlayerScorer;
import app.foot.controller.rest.mapper.MatchRestMapper;
import app.foot.controller.rest.mapper.PlayerScorerRestMapper;
import app.foot.controller.validator.GoalValidator;
import app.foot.exception.ForbiddenException;
import app.foot.exception.InternalServerException;
import app.foot.repository.entity.PlayerScoreEntity;
import app.foot.service.MatchService;
import app.foot.service.PlayerScoreService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class MatchController {
    private final MatchService service;
    private final GoalValidator validator;
    private final MatchRestMapper mapper;
    private final PlayerScorerRestMapper scorerMapper;

    @GetMapping("/matches/{id}")
    public Match getMatchById(@PathVariable Integer id) {
        return mapper.toRest(service.getMatchById(id));
    }
    @GetMapping("/matches")
    public List<Match> getMatches() {
         return service.getMatches();
    }
    @PostMapping("/matches/{matchId}/goals")
    public Match addGoals(@PathVariable int matchId, @RequestBody List<PlayerScorer> scorers) {
        scorers.forEach(validator);
        List<app.foot.model.PlayerScorer> scorerList = scorers.stream()
                .map(scorerMapper::toDomain)
                .toList();
        return service.addGoals(matchId, scorerList);
    }
}
