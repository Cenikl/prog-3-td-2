package app.foot.service;

import app.foot.controller.rest.mapper.PlayerRestMapper;
import app.foot.model.Player;
import app.foot.repository.PlayerRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.mapper.PlayerMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {
    private final PlayerRepository repository;
    private final PlayerMapper mapper;

    private final PlayerRestMapper restMapper;

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
    public ResponseEntity<app.foot.controller.rest.Player> getPlayer(int playerId){
        if(repository.findById(playerId).isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
        return new ResponseEntity<>(restMapper.toRest(mapper.toDomain(
                repository.findById(playerId).orElseThrow(() -> new RuntimeException("Player#" + playerId + " not found.")))), HttpStatus.OK);
        }
    }
    public ResponseEntity<PlayerEntity> updatePlayerName(int playerId,String playerName){
        if(repository.findById(playerId).isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
        PlayerEntity player = repository.findById(playerId).get();
        player.setName(playerName);
        return new ResponseEntity<>(repository.save(player),HttpStatus.OK);
        }
    }
    public ResponseEntity<PlayerEntity> updateGuardian(int playerId,Boolean isGuardian){
        if(repository.findById(playerId).isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
        PlayerEntity player = repository.findById(playerId).get();
        player.setGuardian(isGuardian);
        return new ResponseEntity<>(repository.save(player),HttpStatus.OK);
        }
    }
}
