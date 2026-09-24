package co.fcv.citas.config;

import co.fcv.citas.application.booking.BookingPorts.BookingRepository;
import co.fcv.citas.application.booking.BookingService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookingConfiguration {
    @Bean BookingService bookingService(BookingRepository repository, Clock clock) { return new BookingService(repository, clock); }
}
