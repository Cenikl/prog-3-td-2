package app.foot.service;

import app.foot.model.PlayerScorer;
import app.foot.repository.PlayerScoreRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.entity.PlayerScoreEntity;
import app.foot.repository.mapper.PlayerMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PlayerScoreService {
  private final PlayerScoreRepository repository;
  private final PlayerMapper mapper;

  @Transactional
  public List<PlayerScorer> addGoals(int matchId, List<PlayerScorer> scorers) {
    return repository.saveAll(
            scorers.stream()
                .map(scorer -> mapper.toEntity(matchId, scorer))
                .toList()
        ).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
