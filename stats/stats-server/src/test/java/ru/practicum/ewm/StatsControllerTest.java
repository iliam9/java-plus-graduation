package ru.practicum.ewm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.dto.StatDto;
import ru.practicum.ewm.stats.StatsController;
import ru.practicum.ewm.stats.service.StatsService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    StatsService service;

    @Autowired
    private MockMvc mvc;


    @Test
    void hit() throws Exception {
        //given
        HitDto hitDto = new HitDto(0L, "ewm-main-service", "/events/1", "121.0.0.1", "2024-10-11 10:14:00");

        //when

        //then
        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(hitDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(service, Mockito.times(1))
                .addHit(hitDto);
    }

    @Test
    void getStats_requestWithoutUrisAndUnique() throws Exception {
        //given

        URI uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "2024-03-10%14:30:00")
                .queryParam("end", "2024-03-10%14:30:00")
                .build().toUri();
        StatDto statDto = new StatDto("app", "uri", 15);


        //when
        when(service.getStats("2024-03-10%14:30:00","2024-03-10%14:30:00",null,false))
                .thenReturn(Arrays.asList(statDto));

        //then
        mvc.perform(get(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(statDto))));
    }

    @Test
    void getStats_requestWithUris() throws Exception {
        //given
        List<String> uris = Arrays.asList("uri1", "uri2");
        URI uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "2024-03-10%14:30:00")
                .queryParam("end", "2024-03-10%14:30:00")
                .queryParam("uris",uris)
                .build().toUri();
        StatDto statDto = new StatDto("app", "uri", 15);


        //when
        when(service.getStats("2024-03-10%14:30:00","2024-03-10%14:30:00",uris,false))
                .thenReturn(Arrays.asList(statDto));

        //then
        mvc.perform(get(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(statDto))));
    }

    @Test
    void getStats_requestWithUnique() throws Exception {
        //given
        URI uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "2024-03-10%14:30:00")
                .queryParam("end", "2024-03-10%14:30:00")
                .queryParam("unique", true)
                .build().toUri();
        StatDto statDto = new StatDto("app", "uri", 15);


        //when
        when(service.getStats("2024-03-10%14:30:00","2024-03-10%14:30:00",null,true))
                .thenReturn(Arrays.asList(statDto));

        //then
        mvc.perform(get(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(statDto))));
    }
}
