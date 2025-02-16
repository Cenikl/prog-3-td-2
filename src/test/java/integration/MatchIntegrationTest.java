package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import app.foot.exception.BadRequestException;
import app.foot.exception.ForbiddenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.player1;
import static utils.TestUtils.scorer1;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping

    @Test
    //execute this test before the other tests
    void read_matches_ok() throws Exception{
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect((status().isOk()))
                .andReturn().getResponse();

        List<Match> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(),response.getStatus());
        assertEquals(3,actual.size());
        assertTrue(actual.containsAll(List.of(expectedMatch1(),expectedMatch2(),expectedMatch3())));

    }
    @Test
    @Sql(statements = "update player_score set own_goal = null where id = 1; ",executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "update player_score set own_goal = false where id = 1; ",executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void read_matches_ko() throws Exception{
    //if someone changes the data in the database to a wrong value, the controller should throw an internal server error if they try to retrieve it
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect((status().isInternalServerError()))
                .andReturn().getResponse();


        assertEquals(500,response.getStatus());
        assertEquals("Can not set boolean field app.foot.repository.entity.PlayerScoreEntity.ownGoal to null value",response.getForwardedUrl());

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

        mockMvc.perform(post("/matches/3/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorer)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        MockHttpServletResponse response = mockMvc.perform(get("/matches/3")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
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
                .player(player6())
                .isOG(false)
                .scoreTime(100)
                .build();
        String expectMess = "400 BAD_REQUEST : Player#J6 cannot score before after minute 90.";
        Exception exception = assertThrows(ServletException.class, () -> {mockMvc
                .perform(post("/matches/3/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorer)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();});

        assertEquals(BadRequestException.class,exception.getCause().getClass());
        assertEquals(expectMess,exception.getCause().getMessage());

    }
    private static Match expectedMatch1() {
        return Match.builder()
                .id(1)
                .teamA(teamMatch1A())
                .teamB(teamMatch1B())
                .stadium("S1")
                .datetime(Instant.parse("2023-01-01T10:00:00Z"))
                .build();
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
    private static Match expectedMatch3() {
        return Match.builder()
                .id(3)
                .teamA(teamMatch3A())
                .teamB(teamMatch3B())
                .stadium("S3")
                .datetime(Instant.parse("2023-01-01T18:00:00Z"))
                .build();
    }
    private static TeamMatch teamMatch3A() {
        return TeamMatch.builder()
                .team(team1())
                .score(0)
                .scorers(List.of())
                .build();
    }
    private static TeamMatch teamMatch3B() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchB() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }
    private static TeamMatch teamMatch1A() {
        return TeamMatch.builder()
                .team(team1())
                .score(4)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player1())
                                .scoreTime(30)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player1())
                                .scoreTime(20)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player1())
                                .scoreTime(10)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player4())
                                .scoreTime(60)
                                .isOG(true)
                                .build()))
                .build();
    }
    private static TeamMatch teamMatch1B() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                        .player(player2())
                        .scoreTime(40)
                        .isOG(true)
                        .build(),
                        PlayerScorer.builder()
                        .player(player3())
                        .scoreTime(50)
                        .isOG(false)
                        .build()))
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
    private static Player player1() {
        return Player.builder()
                .id(1)
                .name("J1")
                .isGuardian(false)
                .teamName(team1().getName())
                .build();
    }
    private static Player player2() {
        return Player.builder()
                .id(2)
                .name("J2")
                .isGuardian(false)
                .teamName(team1().getName())
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
    private static Player player4() {
        return Player.builder()
                .id(4)
                .name("J4")
                .isGuardian(false)
                .teamName(team2().getName())
                .build();
    }
    private static Player player5() {
        return Player.builder()
                .id(5)
                .name("J5")
                .isGuardian(false)
                .teamName(team3().getName())
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

    private static Team team1() {
        return Team.builder()
                .id(1)
                .name("E1")
                .build();
    }
    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("E2")
                .build();
    }

    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("E3")
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
