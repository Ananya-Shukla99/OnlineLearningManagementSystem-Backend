package com.edulearn.discussion.controller;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.service.DiscussionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Discussion Controller Tests")
class DiscussionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DiscussionService discussionService;

    @InjectMocks
    private DiscussionController discussionController;

    private ObjectMapper objectMapper;
    private DiscussionThread testThread;
    private Reply testReply;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(discussionController).build();
        objectMapper = new ObjectMapper();

        testThread = new DiscussionThread();
        testThread.setThreadId(1);
        testThread.setCourseId(10);
        testThread.setTitle("Title");
        testThread.setBody("Body");

        testReply = new Reply();
        testReply.setReplyId(1);
        testReply.setThreadId(1);
        testReply.setBody("Reply Body");
    }

    // ─── THREAD ENDPOINTS ─────────────────────────────────────────────

    @Nested
    @DisplayName("Thread Endpoints")
    class ThreadEndpoints {

        @Test
        @DisplayName("POST /api/discussion/threads - Success")
        void testCreateThread() throws Exception {
            when(discussionService.createThread(any(DiscussionThread.class))).thenReturn(testThread);

            mockMvc.perform(post("/api/discussion/threads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testThread)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.threadId").value(1));
        }

        @Test
        @DisplayName("GET /api/discussion/threads/course/{courseId} - Success")
        void testGetByCourse() throws Exception {
            when(discussionService.getThreadsByCourse(10)).thenReturn(List.of(testThread));

            mockMvc.perform(get("/api/discussion/threads/course/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("GET /api/discussion/threads/{threadId} - Success/NotFound")
        void testGetById() throws Exception {
            when(discussionService.getThreadById(1)).thenReturn(Optional.of(testThread));
            mockMvc.perform(get("/api/discussion/threads/1")).andExpect(status().isOk());

            when(discussionService.getThreadById(999)).thenReturn(Optional.empty());
            mockMvc.perform(get("/api/discussion/threads/999")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/discussion/threads/{threadId} - Success/NotFound")
        void testUpdateThread() throws Exception {
            when(discussionService.updateThread(eq(1), any(DiscussionThread.class))).thenReturn(testThread);
            mockMvc.perform(put("/api/discussion/threads/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testThread)))
                    .andExpect(status().isOk());

            when(discussionService.updateThread(eq(999), any(DiscussionThread.class)))
                    .thenThrow(new RuntimeException("Not Found"));
            mockMvc.perform(put("/api/discussion/threads/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testThread)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/discussion/threads/{threadId} - Success")
        void testDeleteThread() throws Exception {
            doNothing().when(discussionService).deleteThread(1);
            mockMvc.perform(delete("/api/discussion/threads/1")).andExpect(status().isOk());

            doThrow(new RuntimeException("Not Found")).when(discussionService).deleteThread(999);
            mockMvc.perform(delete("/api/discussion/threads/999")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Status updates (pin/close/etc.)")
        void testStatusEndpoints() throws Exception {
            doNothing().when(discussionService).pinThread(1);
            mockMvc.perform(put("/api/discussion/threads/1/pin")).andExpect(status().isOk());

            doNothing().when(discussionService).unpinThread(1);
            mockMvc.perform(put("/api/discussion/threads/1/unpin")).andExpect(status().isOk());

            doNothing().when(discussionService).closeThread(1);
            mockMvc.perform(put("/api/discussion/threads/1/close")).andExpect(status().isOk());

            doNothing().when(discussionService).reopenThread(1);
            mockMvc.perform(put("/api/discussion/threads/1/reopen")).andExpect(status().isOk());
        }
    }

    // ─── REPLY ENDPOINTS ──────────────────────────────────────────────

    @Nested
    @DisplayName("Reply Endpoints")
    class ReplyEndpoints {

        @Test
        @DisplayName("POST /api/discussion/replies - Success/BadRequest")
        void testAddReply() throws Exception {
            when(discussionService.postReply(any(Reply.class))).thenReturn(testReply);
            mockMvc.perform(post("/api/discussion/replies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReply)))
                    .andExpect(status().isCreated());

            when(discussionService.postReply(any(Reply.class))).thenThrow(new RuntimeException("Closed"));
            mockMvc.perform(post("/api/discussion/replies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReply)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /api/discussion/replies/{replyId}/upvote - Success/BadRequest")
        void testUpvote() throws Exception {
            when(discussionService.upvoteReply(1, 101)).thenReturn(testReply);
            mockMvc.perform(put("/api/discussion/replies/1/upvote").param("studentId", "101"))
                    .andExpect(status().isOk());

            when(discussionService.upvoteReply(1, 101)).thenThrow(new RuntimeException("Already"));
            mockMvc.perform(put("/api/discussion/replies/1/upvote").param("studentId", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Acceptance endpoints")
        void testAcceptEndpoints() throws Exception {
            when(discussionService.acceptReply(1)).thenReturn(testReply);
            mockMvc.perform(put("/api/discussion/replies/1/accept")).andExpect(status().isOk());

            doNothing().when(discussionService).unacceptReply(1);
            mockMvc.perform(put("/api/discussion/replies/1/unaccept")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Author and Thread queries")
        void testQueries() throws Exception {
            when(discussionService.getRepliesByThread(1)).thenReturn(List.of(testReply));
            mockMvc.perform(get("/api/discussion/replies/thread/1")).andExpect(status().isOk());

            when(discussionService.getRepliesByAuthor(102)).thenReturn(List.of(testReply));
            mockMvc.perform(get("/api/discussion/replies/author/102")).andExpect(status().isOk());
        }
    }
}
