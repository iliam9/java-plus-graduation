package ru.practicum.ewm;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.dto.StatDto;
import ru.practicum.ewm.stats.mapper.HitMapper;
import ru.practicum.ewm.stats.mapper.HitMapperImpl;
import ru.practicum.ewm.stats.mapper.StatMapperImpl;
import ru.practicum.ewm.stats.model.Hit;
import ru.practicum.ewm.stats.service.StartsServiceImpl;
import ru.practicum.ewm.stats.service.StatsService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({StartsServiceImpl.class, HitMapperImpl.class, StatMapperImpl.class})
public class StatsServiceTest {

    private final EntityManager em;
    private final StatsService service;
    private final HitMapper mapper;


    void getHits() {
        List<HitDto> hitDtos = Arrays.asList(
                new HitDto(0, "app", "uri1", "127.0.0.1", "2024-03-10 14:30:00"),
                new HitDto(0, "app", "uri1", "127.0.0.1", "2024-03-10 14:30:00"),
                new HitDto(0, "app", "uri2", "127.0.0.1", "2024-03-10 14:30:00")
        );
        hitDtos.stream().map(mapper::map).forEach(em::persist);
        em.flush();
    }

    @Test
    void addHit() {
        //given
        HitDto hitDto = new HitDto(0, "app", "uri", "127.0.0.1", "2024-03-10 14:30:00");

        //when
        service.addHit(hitDto);

        //then
        TypedQuery<Hit> query = em.createQuery("Select h from Hit h where h.app = :app", Hit.class);
        Hit targetHit = query.setParameter("app", hitDto.getApp()).getSingleResult();

        assertThat(targetHit, is(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("app", equalTo(hitDto.getApp())),
                hasProperty("uri", equalTo(hitDto.getUri())),
                hasProperty("ip", equalTo(hitDto.getIp()))
        )));
    }


    @Test
    void getStat_withoutUrisAndUnique() {
        //given
        getHits();

        //when
        List<StatDto> targetStateDto = service.getStats("2024-03-10 14:00:00",
                "2024-03-10 15:00:00",
                null,
                false);
        //then

        assertThat(targetStateDto.size(), is(2));

    }

    @Test
    void getStat_withUris() {
        //given
        getHits();

        //when
        List<StatDto> targetStateDto = service.getStats("2024-03-10 14:00:00",
                "2024-03-10 15:00:00",
                Arrays.asList("uri2"),
                false);
        //then

        assertThat(targetStateDto.size(), is(1));
        assertThat(targetStateDto, hasItem(allOf(
                hasProperty("app", equalTo("app")),
                hasProperty("uri", equalTo("uri2")),
                hasProperty("hits", equalTo(1L))
        )));

    }

    @Test
    void getStat_withUnique() {
        //given
        getHits();

        //when
        List<StatDto> targetStateDto = service.getStats("2024-03-10 14:00:00",
                "2024-03-10 15:00:00",
                null,
                true);
        //then

        assertThat(targetStateDto.size(), is(2));
        assertThat(targetStateDto, hasItem(allOf(
                hasProperty("app", equalTo("app")),
                hasProperty("uri", equalTo("uri1")),
                hasProperty("hits", equalTo(1L))
        )));

    }


}
