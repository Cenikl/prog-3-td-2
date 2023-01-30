package app.foot.controller;

import app.foot.controller.rest.Player;
import app.foot.controller.rest.mapper.PlayerRestMapper;
import app.foot.repository.entity.PlayerEntity;
import app.foot.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class PlayerController {
    private final PlayerRestMapper mapper;
    private final PlayerService service;

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return service.getPlayers().stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PostMapping("/players")
    public List<Player> addPlayers(@RequestBody List<Player> toCreate) {
        List<app.foot.model.Player> domain = toCreate.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toUnmodifiableList());
        return service.createPlayers(domain).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    // TODO: add PUT /players where you can modify the name and the guardian status of a player
    //  Don't forget to add integration tests for this

    @PutMapping("/players/{playerId}")
    public Player updatePlayer(@PathVariable int playerId,
                               @RequestParam(required = false) String playerName,
                               @RequestParam(required = false) Boolean isGuardian){
        service.updatePlayerName(playerId,playerName);
        service.updateGuardian(playerId,isGuardian);
        return mapper.toRest(service.getPlayer(playerId));
    }

}
