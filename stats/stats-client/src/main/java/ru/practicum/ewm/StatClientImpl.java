package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.dto.ParamDto;
import ru.practicum.ewm.dto.StatDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
public class StatClientImpl implements StatClient {
    private final WebClient webClient;

    public StatClientImpl(@Value("${client.url}") String uri) {
        webClient = WebClient.builder()
                .baseUrl(uri)
                .build();
    }

    @Override
    public void hit(HitDto hitDto) {
        webClient.post()
                .uri("/hit")
                .bodyValue(hitDto)
                .retrieve()
                .toBodilessEntity()
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorComplete()
                .block();
    }

    @Override
    public List<StatDto> stat(ParamDto paramDto) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(URI.create("/stats"))
                .queryParam("start", paramDto.getStart())
                .queryParam("end", paramDto.getEnd());
        if (paramDto.getUris() != null) {
            uriComponentsBuilder.queryParam("uris",paramDto.getUris());
        }
        if (paramDto.isUnique()) {
            uriComponentsBuilder.queryParam("unique", true);
        }
        StatDto[] result = webClient.get()
                .uri(uriComponentsBuilder.build().toString())
                .retrieve()
                .bodyToMono(StatDto[].class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorComplete()
                .block();
        if (result != null) {
            return Arrays.stream(result).toList();
        } else {
            return  new ArrayList<>();
        }
    }
}
