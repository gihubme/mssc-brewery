package guru.springframework.msscbrewery.web.controller.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.V2.BeerServiceV2;
import guru.springframework.msscbrewery.web.model.v2.BeerDtoV2;
import guru.springframework.msscbrewery.web.model.v2.BeerStyleEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class BeerControllerV2Test {

    @MockBean
    private BeerServiceV2 service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    BeerDtoV2 validBeer;
    private static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
    }

    @BeforeEach
//    @BeforeAll
    void setUp() {
        validBeer = BeerDtoV2.builder()
                .beerName("Beer1")
                .beerStyle(BeerStyleEnum.ALE)
                .beerStyleStr(BeerStyleEnum.ALE.toString())
                .upc(635472L)
                .id(UUID.randomUUID())
                .build();
    }

    @DisplayName("GET /beerById success")
    @Test
    void getBeer() throws Exception {
        given(service.getBeerById(any(UUID.class))).willReturn(validBeer);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/beer/" + validBeer.getId().toString())
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
        BeerDtoV2 beerDto = validBeer;
        beerDto.setId(null);
        BeerDtoV2 savedDto = BeerDtoV2.builder().id(UUID.randomUUID()).beerName("New Beer").build();
        String dtoJson = objectMapper.writeValueAsString(beerDto);

        given(service.saveNewBeer(any())).willReturn(savedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        log.info(dtoJson);
    }

    @DisplayName("PUT /beerById noContent")
    @Test
    void handlePut() throws Exception {
        //given
        BeerDtoV2 beerDto = validBeer;
        beerDto.setId(null);
        String dtoJson = objectMapper.writeValueAsString(beerDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/beer/" + UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        then(service).should().updateBeer(any(), any());
    }

    @DisplayName("DELETE /beerById noContent")
    @Test
    void deleteBeer() throws Exception {
        //given
        BeerDtoV2 beerDto = validBeer;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/beer/" + validBeer.getId().toString()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        then(service).should().deleteById(any());
    }

    @Test
    public void whenAllNull_thenOnlyNotNullShouldGiveConstraintViolations() {
        BeerDtoV2 dto = BeerDtoV2.builder()
                .id(null)
                .beerName(null)
                .beerStyleStr(null)
                .upc(null)
                .beerStyle(null)
                .build();

        Set<ConstraintViolation<BeerDtoV2>> violations = validator.validate(dto);
        assertThat(violations.size()).isEqualTo(2);

        assertThat(violations)
                .anyMatch(havingPropertyPath("beerStyle")
                        .and(havingMessage("must not be null")));
        assertThat(violations)
                .anyMatch(havingPropertyPath("beerName")
                        .and(havingMessage("must not be blank")));
    }

    @Test
    public void whenAllInvalid_thenViolationsShouldBeReported() {
        BeerDtoV2 dto = BeerDtoV2.builder()
                .id(UUID.randomUUID())
                .beerName(" ")
                .beerStyleStr("Pineapple")
                .upc(-123L)
                .beerStyle(null)
                .build();

        Set<ConstraintViolation<BeerDtoV2>> violations = validator.validate(dto);
        assertThat(violations.size()).isEqualTo(5);

        assertThat(violations)
                .anyMatch(havingPropertyPath("beerStyle")
                        .and(havingMessage("must not be null")));
        assertThat(violations)
                .anyMatch(havingPropertyPath("beerName")
                        .and(havingMessage("must not be blank")));

        String className = BeerStyleEnum.class.getCanonicalName().toString();
        assertThat(violations)
                .anyMatch(havingPropertyPath("beerStyleStr")
                        .and(havingMessage(
                                "must be any of enum class " + className)));
        assertThat(violations)
                .anyMatch(havingPropertyPath("upc")
                        .and(havingMessage("must be greater than 0")));
        assertThat(violations)
                .anyMatch(havingPropertyPath("id")
                        .and(havingMessage("must be null")));

    }

    @Test
    public void whenAllInvalidPost_thenViolationsShouldBeReported() throws Exception {
        BeerDtoV2 dto = BeerDtoV2.builder()
                .id(UUID.randomUUID())
                .beerName(" ")
                .beerStyleStr("Pineapple")
                .upc(-123L)
                .beerStyle(null)
                .build();

        Set<ConstraintViolation<BeerDtoV2>> violations = validator.validate(dto);

        List<String> errors = new ArrayList<>(5);
        violations.forEach(constraintViolation -> {
            errors.add(constraintViolation.getPropertyPath() + " : " + constraintViolation.getMessage());
        });

        String dtoJson = objectMapper.writeValueAsString(errors);

        ConstraintViolationException e = new ConstraintViolationException(violations);
        given(service.saveNewBeer(dto)).willThrow(e);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        log.info("errorsJson: " + dtoJson);

//        when(myService.someMethod(...)).thenThrow(new ConstraintViolationException(...))

    }

    public static Predicate<ConstraintViolation<BeerDtoV2>> havingMessage(String message) {
        return l -> message.equals(l.getMessage());
    }

    public static Predicate<ConstraintViolation<BeerDtoV2>> havingPropertyPath(String propertyPath) {
        return l -> propertyPath.equals(l.getPropertyPath().toString());
    }
}
