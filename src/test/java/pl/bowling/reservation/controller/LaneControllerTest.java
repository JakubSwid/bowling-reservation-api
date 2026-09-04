package pl.bowling.reservation.controller;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.bowling.reservation.dto.CreateLaneRequest;
import pl.bowling.reservation.dto.UpdateLaneRequest;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.enums.Status;
import pl.bowling.reservation.exception.LaneDoesntExistException;
import pl.bowling.reservation.service.LaneService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(LaneController.class)
public class LaneControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaneService service;

    @Test
    void shouldGetAllLanes() throws Exception{
        //given
        Lane lane1 = new Lane(1L, 5, Status.ACTIVE, 1);
        Lane lane2 = new Lane(2L, 6, Status.OUT_OF_SERVICE, 1);
        when(service.getAllLanes()).thenReturn(List.of(lane1,lane2));

        //then and when
        mockMvc.perform(get("/api/lanes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].laneNumber").value(5))
                .andExpect(jsonPath("$[1].laneNumber").value(6));
    }

    @Test
    void shouldGetLaneById() throws Exception {
        // given
        Lane lane = new Lane(1L, 5, Status.ACTIVE, 1);
        when(service.getLane(1L)).thenReturn(lane);

        // when + then
        mockMvc.perform(get("/api/lanes/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.laneNumber").value(5))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    @Test
    void shouldReturn404WhenLaneNotFound() throws Exception {
        // given
        when(service.getLane(999L)).thenThrow(new LaneDoesntExistException("..."));

        // when + then
        mockMvc.perform(get("/api/lanes/{id}",999L))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldCreateLane() throws Exception {
        // given
        CreateLaneRequest request = new CreateLaneRequest(5);
        Lane createdLane = new Lane(1L, 5, Status.ACTIVE, 1);
        when(service.createLane(any(CreateLaneRequest.class))).thenReturn(createdLane);

        // when + then
        mockMvc.perform(post("/api/lanes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "laneNumber": 5
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.laneNumber").value(5));
    }
    @Test
    void shouldReturn400WhenLaneNumberInvalid() throws Exception {
        // when + then
        mockMvc.perform(post("/api/lanes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "laneNumber": 0
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldUpdateLane() throws Exception {
        // given
        UpdateLaneRequest request = new UpdateLaneRequest(7, Status.OUT_OF_SERVICE);
        Lane updatedLane = new Lane(1L, 7, Status.OUT_OF_SERVICE, 2);
        when(service.updateLane(any(UpdateLaneRequest.class), eq(1L))).thenReturn(updatedLane);

        // when + then
        mockMvc.perform(put("/api/lanes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "laneNumber": 7,
                                "status": "OUT_OF_SERVICE"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laneNumber").value(7))
                .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"));
    }
    @Test
    void shouldDeleteLane() throws Exception {
        // given
        doNothing().when(service).deleteLane(1L);

        // when + then
        mockMvc.perform(delete("/api/lanes/{id}",1))
                .andExpect(status().isNoContent());

        verify(service).deleteLane(1L);
    }
}
