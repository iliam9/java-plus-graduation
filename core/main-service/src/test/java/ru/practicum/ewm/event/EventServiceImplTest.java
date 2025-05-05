/*
package ru.practicum.ewm.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.StatClientImpl;
import ru.practicum.ewm.category.mapper.CategoryMapperImpl;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.EventMapperImpl;
import ru.practicum.ewm.request.mapper.RequestMapperImpl;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.service.EventServiceImpl;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.user.mapper.UserMapperImpl;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({EventServiceImpl.class, EventMapperImpl.class, RequestMapperImpl.class,
        UserMapperImpl.class, CategoryMapperImpl.class, StatClientImpl.class})
public class EventServiceImplTest {

    private final EntityManager em;
    private final EventService eventService;
    private final EventMapper eventMapper;

    private final NewEventDto newEvent = new NewEventDto(
            "annotation",
            1,
            "description",
            LocalDateTime.now().plusDays(2),
            new Location(34.56, 25.98),
            true,
            5,
            true,
            "title"
    );

    private final UpdateEventUserRequest updateEvent = new UpdateEventUserRequest(
            "newAnnotation",
            1,
            "newDescription",
            LocalDateTime.now().plusDays(3),
            new Location(34.56, 25.98),
            10,
            true,
            EventState.PENDING,
            false,
            "title"
    );

    private User getUser() {
        User user = new User("test@mail.ru", null, "name");
        em.persist(user);
        return user;
    }

    private Category getCategory() {
        Category category = new Category(null, "name");
        em.persist(category);
        return category;
    }

    private Request getRequest(Event event) {
        User user = new User("test@yandex.ru", null, "name");
        em.persist(user);
        Request request = new Request(0, event,user, RequestStatus.CONFIRMED, LocalDateTime.now());
        em.persist(request);
        return request;
    }

    private Event getEvent() {
        User user = getUser();
        Category category = getCategory();
        Event event = eventMapper.map(newEvent);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        em.persist(event);
        getRequest(event);
        return event;
    }

    @Test
    void createEvent() {
        // given
        User user = getUser();
        Category category = getCategory();
        newEvent.setCategory(category.getId());

        // when
        eventService.create(user.getId(), newEvent);

        // then
        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.title = :title", Event.class);
        Event targetEvent = query.setParameter("title", newEvent.getTitle()).getSingleResult();

        assertThat(targetEvent, is(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("title", equalTo(newEvent.getTitle())),
                hasProperty("annotation", equalTo(newEvent.getAnnotation())),
                hasProperty("description", equalTo(newEvent.getDescription()))
        )));
    }

    @Test
    void findBy() {
        // given
        Event sourceEvent = getEvent();
        ParamEventDto paramEventDto = new ParamEventDto(sourceEvent.getInitiator().getId(), sourceEvent.getId());

        // when
        EventFullDto targetEvent = eventService.findBy(paramEventDto, "127.0.0.1");
        EventFullDto targetEvent2 = eventService.findBy(paramEventDto, "127.0.0.2");
        EventFullDto targetEvent3 = eventService.findBy(paramEventDto, "127.0.0.2");

        // then

        assertThat(targetEvent3, is(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("title", equalTo(sourceEvent.getTitle())),
                hasProperty("annotation", equalTo(sourceEvent.getAnnotation())),
                hasProperty("description", equalTo(sourceEvent.getDescription()))
        )));
    }

    @Test
    void findBy_withSearchParam() {
        // given
        Event sourceEvent = getEvent();

        // when
         Collection<EventShortDto> targetEvents = eventService
                 .findBy(new PrivateSearchEventDto(
                         sourceEvent.getInitiator().getId(), 0L,10L, "127.0.0.1"));

        // then

        assertThat(targetEvents, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("title", equalTo(sourceEvent.getTitle())),
                hasProperty("annotation", equalTo(sourceEvent.getAnnotation()))
        )));
    }


    @Test
    void updateEvent() {
        // given
        Event sourceEvent = getEvent();
        ParamEventDto paramEventDto = new ParamEventDto(sourceEvent.getInitiator().getId(), sourceEvent.getId());
        updateEvent.setCategory(sourceEvent.getCategory().getId());


        // when
        eventService.update(paramEventDto, updateEvent);

        // then
        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event targetEvent = query.setParameter("id", sourceEvent.getId()).getSingleResult();

        assertThat(targetEvent, is(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("title", equalTo(newEvent.getTitle())),
                hasProperty("annotation", equalTo(updateEvent.getAnnotation())),
                hasProperty("description", equalTo(updateEvent.getDescription()))
        )));
    }


}
*/
