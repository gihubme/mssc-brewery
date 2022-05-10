package guru.springframework.msscbrewery.web.controller.v2;

import guru.springframework.msscbrewery.services.V2.BeerServiceV2;
import guru.springframework.msscbrewery.web.model.v2.BeerDtoV2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Validated
@RequestMapping("/api/v2/beer")
@RestController
public class BeerControllerV2 {

    private final BeerServiceV2 beerService;

    public BeerControllerV2(BeerServiceV2 beerService) {
        this.beerService = beerService;
    }

    @GetMapping({"/{beerId}"})
    public ResponseEntity<BeerDtoV2> getBeer(@NotNull @PathVariable("beerId") UUID beerId) {

        return new ResponseEntity<>(beerService.getBeerById(beerId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity handlePost(@NotNull @Valid @RequestBody BeerDtoV2 beerDto) {

        BeerDtoV2 savedDto = beerService.saveNewBeer(beerDto);

        HttpHeaders headers = new HttpHeaders();
        //todo add hostname to url
        headers.add("Location", "/api/v2/beer/" + savedDto.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping({"/{beerId}"})
    public ResponseEntity handlePut(@NotNull @PathVariable("beerId") UUID beerId,
                                    @NotNull @Valid @RequestBody BeerDtoV2 beerDto) {
        BeerDtoV2 savedDto = beerService.updateBeer(beerId, beerDto);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{beerId}")
    public void deleteBeer(@NotNull @PathVariable("beerId") UUID id) {
        beerService.deleteById(id);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<List> validationErrorHandling(MethodArgumentNotValidException nve) {
        List<String> errors = new ArrayList<>(nve.getBindingResult().getAllErrors().size());
        nve.getBindingResult().getAllErrors().forEach(objectErr -> {
            errors.add(objectErr.getCodes()[0] + " : " + objectErr.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
/* MethodArgumentNotValidException
[
    "NotBlank.beerDtoV2.beerName : must not be blank",
    "ValueOfEnum.beerDtoV2.beerStyleStr : must be any of enum class guru.springframework.msscbrewery.web.model.v2.BeerStyleEnum",
    "NotNull.beerDtoV2.beerStyle : must not be null",
    "Positive.beerDtoV2.upc : must be greater than 0",
    "Null.beerDtoV2.id : must be null"
]
*/
    }


}
