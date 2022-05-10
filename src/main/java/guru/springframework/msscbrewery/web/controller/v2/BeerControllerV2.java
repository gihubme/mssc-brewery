package guru.springframework.msscbrewery.web.controller.v2;

import guru.springframework.msscbrewery.services.V2.BeerServiceV2;
import guru.springframework.msscbrewery.web.model.v2.BeerDtoV2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v2/beer")
@RestController
public class BeerControllerV2 {

    private final BeerServiceV2 beerService;

    public BeerControllerV2(BeerServiceV2 beerService) {
        this.beerService = beerService;
    }

    @GetMapping({"/{beerId}"})
    public ResponseEntity<BeerDtoV2> getBeer(@PathVariable("beerId") UUID beerId) {

        return new ResponseEntity<>(beerService.getBeerById(beerId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity handlePost(@Valid @RequestBody BeerDtoV2 beerDto) {

        BeerDtoV2 savedDto = beerService.saveNewBeer(beerDto);

        HttpHeaders headers = new HttpHeaders();
        //todo add hostname to url
        headers.add("Location", "/api/v2/beer/" + savedDto.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping({"/{beerId}"})
    public ResponseEntity handlePut(@PathVariable("beerId") UUID beerId, @Valid @RequestBody BeerDtoV2 beerDto) {
        BeerDtoV2 savedDto = beerService.updateBeer(beerId, beerDto);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{beerId}")
    public void deleteBeer(@PathVariable("beerId") UUID id) {
        beerService.deleteById(id);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<List> validationErrorHandling(Exception e) {
        if (e instanceof ConstraintViolationException) {
            ConstraintViolationException ce = (ConstraintViolationException) e;
            List<String> errors = new ArrayList<>(ce.getConstraintViolations().size());
            ce.getConstraintViolations().forEach(constraintViolation -> {
                errors.add(constraintViolation.getPropertyPath() + " : " + constraintViolation.getMessage());
            });
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        } else if (e instanceof ConstraintViolationException) {
            MethodArgumentNotValidException nve = (MethodArgumentNotValidException) e;
            List<String> errors = new ArrayList<>(nve.getBindingResult().getAllErrors().size());
            nve.getBindingResult().getAllErrors().forEach(objectErr -> {
                errors.add(objectErr.getCodes()[0] + " : " + objectErr.getDefaultMessage());
            });
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(Collections.singletonList("Bad Request"), HttpStatus.BAD_REQUEST);
        }
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

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<String> httpMessageNotReadableErrorHandling(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();

        String errors = cause.getMessage();
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
