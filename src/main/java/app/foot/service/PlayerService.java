package app.foot.service;

import app.foot.model.Player;
import app.foot.repository.PlayerRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.mapper.PlayerMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {
    private final PlayerRepository repository;
    private final PlayerMapper mapper;

    public List<Player> getPlayers() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Player> createPlayers(List<Player> toCreate) {
        return repository.saveAll(toCreate.stream()
                        .map(mapper::toEntity)
                        .collect(Collectors.toUnmodifiableList())).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toUnmodifiableList());
    }
    public Player getPlayer(int playerId){
        return mapper.toDomain(
                repository.findById(playerId)
                        .orElseThrow(() -> new RuntimeException("Player#" + playerId + " not found."))
        );
    }
    public PlayerEntity updatePlayerName(int playerId,String playerName){
        PlayerEntity player = repository.findById(playerId).get();
        player.setName(playerName);
        return repository.save(player);
    }
    public PlayerEntity updateGuardian(int playerId,Boolean isGuardian){
        PlayerEntity player = repository.findById(playerId).get();
        player.setGuardian(isGuardian);
        return repository.save(player);
    }
}
