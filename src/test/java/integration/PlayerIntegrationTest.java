package integration;

import app.foot.FootApi;
import app.foot.controller.rest.Player;
import app.foot.model.Team;
import app.foot.repository.PlayerRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.entity.TeamEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
class PlayerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PlayerRepository repository;

    Team team1(){
        return Team.builder()
                .id(1)
                .name("Madrid")
                .build();
    }
    TeamEntity team2(){
        return TeamEntity.builder()
                .id(10)
                .name("ground")
                .build();
    }

    Player player1() {
        return Player.builder()
                .id(1)
                .name("J1")
                .isGuardian(false)
                .teamName("E1")
                .build();
    }
    app.foot.model.Player playerOne() {
        return app.foot.model.Player.builder()
                .id(1)
                .name("J1")
                .isGuardian(false)
                .teamName("Madrid")
                .build();
    }

    Player player2() {
        return Player.builder()
                .id(2)
                .name("J2")
                .isGuardian(false)
                .teamName("E1")
                .build();
    }

    Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .isGuardian(false)
                .teamName("E2")
                .build();
    }
    @Test
    void read_players_ok() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/players"))
                .andReturn()
                .getResponse();
        List<Player> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(actual.containsAll(List.of(
                player1(),
                player2(),
                player3())));
    }

    @Test
    void create_players_ok() throws Exception {
        Player toCreate = Player.builder()
                .name("Joe Doe")
                .isGuardian(false)
                .teamName("E1")
                .build();
        MockHttpServletResponse response = mockMvc
                .perform(post("/players")
                        .content(objectMapper.writeValueAsString(List.of(toCreate)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();
        List<Player> actual = convertFromHttpResponse(response);

        assertEquals(1, actual.size());
        assertEquals(toCreate, actual.get(0).toBuilder().id(null).build());
    }
    @Test
    void update_player_ok() throws Exception {
        String newName = "Jaden";
        Boolean guardian = true;
        Player expected = Player.builder()
                .id(10)
                .name("Jaden")
                .isGuardian(true)
                .teamName("E1")
                .build();
        MockHttpServletResponse response = mockMvc
                .perform(put("/players/10")
                        .param("playerName",newName)
                        .param("isGuardian", String.valueOf(guardian)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Player actual = convertPlayerFromHttpResponse(response);
        assertEquals(expected,actual);
    }
    @Test
    void update_player_ko() throws Exception {
        String newName = "Jaden";
        Boolean guardian = true;
        MockHttpServletResponse response = mockMvc
                .perform(put("/players/100")
                        .param("playerName",newName)
                        .param("isGuardian", String.valueOf(guardian)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();

        assertEquals(404,response.getStatus());
    }

    private List<Player> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType playerListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Player.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerListType);
    }
    private Player convertPlayerFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        JavaType playerType = objectMapper.getTypeFactory()
                .constructType(Player.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerType);
    }
}
