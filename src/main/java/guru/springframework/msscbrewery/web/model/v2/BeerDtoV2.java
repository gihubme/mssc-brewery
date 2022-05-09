package guru.springframework.msscbrewery.web.model.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDtoV2 {
    @Null
    private UUID id;
    @NotBlank
    private String beerName;

    @NotNull
    private BeerStyleEnum beerStyle;

    @ValueOfEnum(enumClass = BeerStyleEnum.class)
    private String beerStyleStr;
    @Positive
    private Long upc;
}
