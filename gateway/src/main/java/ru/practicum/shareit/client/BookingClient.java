package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.HashMap;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> addBooking(Long bookerId, BookingDto dto) {
        return post("", bookerId, dto);
    }

    public ResponseEntity<Object> approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("approved", approved);
        String path = "/" + bookingId + "?approved={approved}";

        return patch(path, ownerId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(Long bookingId, Long userId) {
        String path = "/" + bookingId;
        return get(path, userId, null);
    }

    public ResponseEntity<Object> getBookingsByBooker(Long bookerId, BookingState state) {
        String path = "?state={state}";
        Map<String, Object> parameters = Map.of("state", state.name());

        return get(path, bookerId, parameters);
    }

    public ResponseEntity<Object> getBookingsByOwner(Long ownerId, BookingState state) {
        String path = "/owner?state={state}";
        Map<String, Object> parameters = Map.of("state", state.name());

        return get(path, ownerId, parameters);
    }
}