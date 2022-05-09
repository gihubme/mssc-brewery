package guru.springframework.msscbrewery.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.BeerService;
import guru.springframework.msscbrewery.web.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

//@RunWith(SpringRunner.class)
//@WebMvcTest(BeerController.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class BeerControllerTest {

    @MockBean
    private BeerService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    BeerDto validBeer;

    @BeforeEach
//    @BeforeAll
    void setUp() {
        validBeer = BeerDto.builder()
                .beerName("Beer1")
                .beerStyle("PALE_ALE")
                .upc(635472L)
                .id(UUID.randomUUID())
                .build();
    }

    @DisplayName("GET /beerById success")
    @Test
    void getBeer() throws Exception {
        given(service.getBeerById(any(UUID.class))).willReturn(validBeer);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/" + validBeer.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(validBeer.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.beerName", is("Beer1")));

    }

    @DisplayName("POST /beer created")
    @Test
    void handlePost() throws Exception {
        //given
        BeerDto beerDto = validBeer;
        beerDto.setId(null);
        BeerDto savedDto = BeerDto.builder().id(UUID.randomUUID()).beerName("New Beer").build();
        String dtoJson = objectMapper.writeValueAsString(beerDto);

        given(service.saveNewBeer(any())).willReturn(savedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        log.info(dtoJson);
    }

    @DisplayName("POST /beer/form created")
    @Test
    void handleUrlEncodedPost() throws Exception {
        //given
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("id", UUID.randomUUID().toString());
        paramMap.add("beerName", "tasty Beer");
        paramMap.add("upc", Long.toString(56382L));

        Map<String, String> bmap = new HashMap<>();
        paramMap.entrySet().stream()
                .forEach(e -> {
                    if (!e.getValue().get(0).isBlank()) {
                        if (e.getKey().equals("id")) bmap.put("id", e.getValue().get(0));
                        if (e.getKey().equals("beerName")) bmap.put("beerName", e.getValue().get(0));
                        if (e.getKey().equals("upc")) bmap.put("upc", e.getValue().get(0));
                    }
                });

        String json = objectMapper.writeValueAsString(bmap);
        log.info("json: " + json);
        // Deserialization into the `Employee` class
        BeerDto dto = objectMapper.readValue(json, BeerDto.class);//.readValue(json, BeerDto.class);

        given(service.saveNewBeerDtoViaForm(any())).willReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/beer/form")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("id", bmap.get("id"))
                        .param("beerName", bmap.get("beerName"))
                        .param("upc", bmap.get("upc")))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)) //MediaType.valueOf("text/plain;charset=UTF-8")
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(bmap.get("id"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.beerName", is("tasty Beer")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.upc", is(56382)));

        log.info("dto: " + dto + "\njson: " + json);
    }

    @DisplayName("PUT /beerById noContent")
    @Test
    void handlePut() throws Exception {
        //given
        BeerDto beerDto = validBeer;
        String dtoJson = objectMapper.writeValueAsString(beerDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/beer/" + validBeer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        then(service).should().updateBeer(any(), any());
    }

    @DisplayName("DELETE /beerById noContent")
    @Test
    void deleteBeer() throws Exception {
        //given
        BeerDto beerDto = validBeer;
        String dtoJson = objectMapper.writeValueAsString(beerDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/beer/" + validBeer.getId().toString()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        then(service).should().deleteById(any());
    }

}
