package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        log.info("Called createRequest(userId={}, requestDto={})", userId, requestDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found, cannot create request", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        requestRepository.save(request);
        log.debug("ItemRequest saved: {}", request);

        return toDto(request, List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Called getUserRequests(userId={})", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found, cannot get requests", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        log.debug("Found {} requests for userId={}", requests.size(), userId);

        return requests.stream()
                .map(r -> {
                    List<Item> items = itemRepository.findByRequestId(r.getId());
                    return toDto(r, toItemShortDto(items));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        log.info("Called getAllRequests(userId={}, from={}, size={})", userId, from, size);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found, cannot get all requests", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        var pageable = PageRequest.of(from / size, size);
        var page = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);

        log.debug("Found {} requests in page (from={}, size={}) for userId={}",
                page.getContent().size(), from, size, userId);

        return page.getContent().stream()
                .map(r -> {
                    List<Item> items = itemRepository.findByRequestId(r.getId());
                    return toDto(r, toItemShortDto(items));
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Called getRequestById(userId={}, requestId={})", userId, requestId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found, cannot get request by id={}", userId, requestId);
                    return new NotFoundException("Пользователь не найден");
                });

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Request with id={} not found", requestId);
                    return new NotFoundException("Запрос не найден");
                });

        List<Item> items = itemRepository.findByRequestId(requestId);
        return toDto(request, toItemShortDto(items));
    }

    // ========================================
    // PRIVATE METHODS
    // ========================================

    private ItemRequestDto toDto(ItemRequest request, List<ItemShortDto> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(items);
        return dto;
    }

    private List<ItemShortDto> toItemShortDto(List<Item> items) {
        return items.stream()
                .map(i -> new ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                .collect(Collectors.toList());
    }
}