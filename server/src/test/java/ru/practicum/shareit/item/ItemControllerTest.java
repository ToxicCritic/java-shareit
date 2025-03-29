package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("POST /items - Success")
    void testAddItem() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, Collections.emptyList());
        Mockito.when(itemService.addItem(any(ItemDto.class), eq(1L))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("PATCH /items/{id} - Success")
    void testUpdateItem() throws Exception {
        ItemDto updated = new ItemDto(1L, "Updated Item", "Updated Description", false, null, Collections.emptyList());
        Mockito.when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L))).thenReturn(updated);

        mvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("GET /items/{id} - Success")
    void testGetItemById() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, Collections.emptyList());
        Mockito.when(itemService.getItemById(eq(1L), eq(1L))).thenReturn(itemDto);

        mvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    @DisplayName("GET /items - Get items by owner")
    void testGetItemsByOwner() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, Collections.emptyList());
        Mockito.when(itemService.getItemsByOwner(eq(1L))).thenReturn(List.of(itemDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /items/search - Success")
    void testSearchItems() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Searchable", "Unique description", true, null, Collections.emptyList());
        Mockito.when(itemService.searchItems(eq("unique"))).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "unique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Searchable"));
    }

    @Test
    @DisplayName("POST /items/{id}/comment - Success")
    void testAddComment() throws Exception {
        CommentDto commentDto = new CommentDto(1L, "Great item!", "John Doe", null);
        Mockito.when(itemService.addComment(eq(1L), eq(2L), any(CommentDto.class))).thenReturn(commentDto);

        mvc.perform(post("/items/{id}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /items/{id} - Not Found")
    void testGetItemByIdNotFound() throws Exception {
        Mockito.when(itemService.getItemById(eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mvc.perform(get("/items/{id}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}