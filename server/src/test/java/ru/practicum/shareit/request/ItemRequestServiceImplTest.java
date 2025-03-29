package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@example.com");
        requestor = userRepository.save(requestor);

        otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void testCreateRequest() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна вещь для теста");
        ItemRequestDto created = requestService.createRequest(requestor.getId(), requestDto);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна вещь для теста");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void testGetUserRequests() {
        // Создаем два запроса
        ItemRequestDto req1 = new ItemRequestDto();
        req1.setDescription("Запрос 1");
        requestService.createRequest(requestor.getId(), req1);

        ItemRequestDto req2 = new ItemRequestDto();
        req2.setDescription("Запрос 2");
        requestService.createRequest(requestor.getId(), req2);

        List<ItemRequestDto> requests = requestService.getUserRequests(requestor.getId());
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Запрос 2");
    }

    @Test
    void testGetAllRequests() {
        ItemRequestDto req = new ItemRequestDto();
        req.setDescription("Запрос от requestor");
        requestService.createRequest(requestor.getId(), req);

        ItemRequestDto reqOther = new ItemRequestDto();
        reqOther.setDescription("Запрос от другого пользователя");
        requestService.createRequest(otherUser.getId(), reqOther);

        List<ItemRequestDto> allRequests = requestService.getAllRequests(requestor.getId(), 0, 10);
        assertThat(allRequests).hasSize(1);
        assertThat(allRequests.get(0).getDescription()).isEqualTo("Запрос от другого пользователя");
    }

    @Test
    void testGetRequestById() {
        ItemRequestDto req = new ItemRequestDto();
        req.setDescription("Запрос для получения");
        ItemRequestDto created = requestService.createRequest(requestor.getId(), req);

        ItemRequestDto fetched = requestService.getRequestById(requestor.getId(), created.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.getDescription()).isEqualTo("Запрос для получения");
    }

    @Test
    void testGetRequestByIdUserNotFound() {
        Exception ex = assertThrows(NotFoundException.class, () ->
                requestService.getRequestById(999L, 1L));
        assertThat(ex.getMessage()).contains("Пользователь не найден");
    }
}