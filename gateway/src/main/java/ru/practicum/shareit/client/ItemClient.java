package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(Long ownerId, ItemDto itemDto) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long itemId, Long ownerId, ItemDto itemDto) {
        String path = "/" + itemId;
        return patch(path, ownerId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long requesterId) {
        String path = "/" + itemId;
        return get(path, requesterId, null);
    }

    public ResponseEntity<Object> getItemsByOwner(Long ownerId) {
        return get("", ownerId, null);
    }

    public ResponseEntity<Object> searchItems(String text) {
        String path = "/search?text={text}";
        Map<String, Object> parameters = Map.of("text", text);
        return get(path, null, parameters);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentDto commentDto) {
        String path = "/" + itemId + "/comment";
        return post(path, userId, commentDto);
    }
}