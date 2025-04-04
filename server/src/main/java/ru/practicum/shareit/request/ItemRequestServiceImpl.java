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
import ru.practicum.shareit.request.dto.ItemRequestMapper;
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
        User user = getUserById(userId);

        var request = ItemRequestMapper.toEntity(requestDto, user);
        request.setCreated(LocalDateTime.now());

        requestRepository.save(request);
        log.debug("ItemRequest saved: {}", request);

        return ItemRequestMapper.toDto(request, List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Called getUserRequests(userId={})", userId);
        getUserById(userId);

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        log.debug("Found {} requests for userId={}", requests.size(), userId);

        return requests.stream()
                .map(r -> {
                    List<Item> items = itemRepository.findByRequestId(r.getId());
                    List<ItemShortDto> itemShortDtos = items.stream()
                            .map(i -> new ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toDto(r, itemShortDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        log.info("Called getAllRequests(userId={}, from={}, size={})", userId, from, size);
        getUserById(userId);

        var pageable = PageRequest.of(from / size, size);
        var page = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);
        log.debug("Found {} requests in page (from={}, size={}) for userId={}",
                page.getContent().size(), from, size, userId);

        return page.getContent().stream()
                .map(r -> {
                    List<Item> items = itemRepository.findByRequestId(r.getId());
                    List<ItemShortDto> itemShortDtos = items.stream()
                            .map(i -> new ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toDto(r, itemShortDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Called getRequestById(userId={}, requestId={})", userId, requestId);
        getUserById(userId);

        var request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Request with id={} not found", requestId);
                    return new NotFoundException("Запрос не найден");
                });
        List<Item> items = itemRepository.findByRequestId(requestId);
        List<ItemShortDto> itemShortDtos = items.stream()
                .map(i -> new ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                .collect(Collectors.toList());
        return ItemRequestMapper.toDto(request, itemShortDtos);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", id);
                    return new NotFoundException("Пользователь не найден");
                });
    }
}