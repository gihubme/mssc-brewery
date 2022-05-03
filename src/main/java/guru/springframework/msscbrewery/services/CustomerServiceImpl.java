package guru.springframework.msscbrewery.services;

import guru.springframework.msscbrewery.web.model.CustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {
    @Override
    public CustomerDto getCustomerById(UUID customerId) {
        return CustomerDto.builder()
                .id(UUID.randomUUID())
                .name("Joe Buck")
                .build();
    }

    @Override
    public CustomerDto saveNewCustomer(CustomerDto dto) {
        return CustomerDto.builder()
                .id(UUID.randomUUID())
                .name("Sommy Rut")
                .build();
    }

    @Override
    public CustomerDto handlePut(UUID id, CustomerDto dto) {
        return CustomerDto.builder()
                .id(id)
                .name("Sommy Rut Update")
                .build();
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("deleted id: " + id.toString());
    }
}
