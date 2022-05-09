package guru.springframework.msscbrewery.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.web.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jt on 2019-04-20.
 */
@Slf4j
@Service
public class BeerServiceImpl implements BeerService {
    @Override
    public BeerDto getBeerById(UUID beerId) {
        return BeerDto.builder()
                .id(beerId)
                .beerName("Galaxy Cat")
                .beerStyle("Pale Ale")
                .build();
    }

    @Override
    public BeerDto saveNewBeer(BeerDto beerDto) {
        return BeerDto.builder()
                .id(UUID.randomUUID())
                .build();
    }

    @Override
    public BeerDto updateBeer(UUID beerId, BeerDto beerDto) {
        return BeerDto.builder()
                .id(beerId)
                .build();
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("deleted id: " + id.toString());
    }

    @Override
    public BeerDto saveNewBeerDtoViaForm(MultiValueMap<String, String> paramMap) throws IOException {
        paramMap.remove("id");
        paramMap.add("id", UUID.randomUUID().toString());
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, String> bmap = new HashMap<>();
        paramMap.entrySet().stream()
                .forEach(e -> {
                    if (!e.getValue().get(0).isBlank()) {
                        if (e.equals("id")) bmap.put("id", e.getValue().get(0));
                        if (e.equals("beerName")) bmap.put("beerName", e.getValue().get(0));
                        if (e.equals("upc")) bmap.put("upc", e.getValue().get(0));
                    }
                });


        String json = objectMapper.writeValueAsString(bmap);
        // Deserialization into the `Employee` class
        BeerDto dto = objectMapper.readValue(json, BeerDto.class);

        return dto;
    }
}
