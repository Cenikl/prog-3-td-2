package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import app.foot.exception.ForbiddenException;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping

    @Test
    void read_matches_ok() throws Exception{
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect((status().isOk()))
                .andReturn().getResponse();

        List<Match> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(),response.getStatus());
        assertEquals(3,actual.size());
        assertTrue(actual.contains(expectedMatch2()));

    }
    @Test
    void read_matches_ko() throws Exception{
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect((status().is5xxServerError()))
                .andReturn().getResponse();
    }

    @Test
    void read_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches/2")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch2(), actual);
    }
    @Test
    void create_goals_ok() throws Exception{
        PlayerScorer playerScorer = PlayerScorer.builder()
                .player(player6())
                .isOG(false)
                .scoreTime(50)
                .build();
        MockHttpServletResponse response = mockMvc
                .perform(post("/matches/2/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorer)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(),response.getStatus());
        assertTrue(actual.getTeamB().getScorers().contains(playerScorer));
       // assertEquals(5,actual.getTeamB().getScorers().size());
    }
    @Test
    void create_goals_ko() throws Exception {
        PlayerScorer playerScorer = PlayerScorer.builder()
                .player(player3())
                .isOG(false)
                .scoreTime(50)
                .build();
        MockHttpServletResponse response = mockMvc
                .perform(post("/matches/3/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorer)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse();

        assertEquals(403,response.getStatus());
    }

    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static TeamMatch teamMatchB() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchA() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(80)
                                .isOG(true)
                                .build()))
                .build();
    }

    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("E3")
                .build();
    }

    private static Player player6() {
        return Player.builder()
                .id(6)
                .name("J6")
                .isGuardian(false)
                .teamName(team3().getName())
                .build();
    }

    private static Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .isGuardian(false)
                .teamName(team2().getName())
                .build();
    }

    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("E2")
                .build();
    }
    private List<Match> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType matchListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                matchListType);
    }

}
