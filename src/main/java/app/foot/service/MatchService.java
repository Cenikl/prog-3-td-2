package app.foot.service;

import app.foot.controller.rest.mapper.MatchRestMapper;
import app.foot.controller.validator.GoalValidator;
import app.foot.exception.ForbiddenException;
import app.foot.model.Match;
import app.foot.model.PlayerScorer;
import app.foot.repository.MatchRepository;
import app.foot.repository.entity.MatchEntity;
import app.foot.repository.mapper.MatchMapper;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MatchService {

  private final GoalValidator validator;
  private final MatchRepository repository;
  private final MatchMapper mapper;

  private final MatchRestMapper restMapper;
  private final PlayerScoreService scoreService;

  public List<app.foot.controller.rest.Match> getMatches() {
    return (repository.findAll()
            .stream().map(mapper::toDomain).toList().stream().map(restMapper::toRest).toList());
  }

  public Match getMatchById(int matchId) {
    return mapper.toDomain(
        repository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match#" + matchId + " not found."))
    );
  }

  public app.foot.controller.rest.Match addGoals(int matchId, List<PlayerScorer> scorers) {
      getMatchById(matchId);
      scoreService.addGoals(matchId, scorers);
      return restMapper.toRest(getMatchById(matchId));
  }
}
