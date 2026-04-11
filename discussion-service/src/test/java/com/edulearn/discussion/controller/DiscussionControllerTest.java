package com.edulearn.discussion.controller;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.service.DiscussionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DiscussionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscussionService discussionService;

    @Autowired
    private ObjectMapper objectMapper;

    private DiscussionThread testThread;
    private Reply testReply;

    @BeforeEach
    public void setUp() {
        testThread = new DiscussionThread();
        testThread.setThreadId(1);
        testThread.setCourseId(1);
        testThread.setAuthorId(101);
        testThread.setTitle("Java Basics");
        testThread.setBody("What is Java?");
        testThread.setIsPinned(false);
        testThread.setIsClosed(false);

        testReply = new Reply();
        testReply.setReplyId(1);
        testReply.setThreadId(1);
        testReply.setAuthorId(102);
        testReply.setBody("Java is a programming language");
        testReply.setIsAccepted(false);
        testReply.setUpvotes(0);
    }

    // ==================== THREAD ENDPOINT TESTS ====================

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testCreateThread() throws Exception {
        when(discussionService.createThread(any(DiscussionThread.class))).thenReturn(testThread);

        mockMvc.perform(post("/api/discussion/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testThread)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.threadId").value(1))
                .andExpect(jsonPath("$.title").value("Java Basics"));

        verify(discussionService, times(1)).createThread(any(DiscussionThread.class));
    }

    @Test
    public void testGetThreadsByCourse() throws Exception {
        when(discussionService.getThreadsByCourse(1)).thenReturn(Arrays.asList(testThread));

        mockMvc.perform(get("/api/discussion/threads/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Basics"));

        verify(discussionService, times(1)).getThreadsByCourse(1);
    }

    @Test
    public void testGetThreadsByLesson() throws Exception {
        when(discussionService.getThreadsByLesson(1)).thenReturn(Arrays.asList(testThread));

        mockMvc.perform(get("/api/discussion/threads/lesson/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(discussionService, times(1)).getThreadsByLesson(1);
    }

    @Test
    public void testGetThreadById() throws Exception {
        when(discussionService.getThreadById(1)).thenReturn(Optional.of(testThread));

        mockMvc.perform(get("/api/discussion/threads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadId").value(1))
                .andExpect(jsonPath("$.title").value("Java Basics"));

        verify(discussionService, times(1)).getThreadById(1);
    }

    @Test
    public void testGetThreadByIdNotFound() throws Exception {
        when(discussionService.getThreadById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/discussion/threads/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testUpdateThread() throws Exception {
        when(discussionService.updateThread(anyInt(), any(DiscussionThread.class))).thenReturn(testThread);

        mockMvc.perform(put("/api/discussion/threads/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testThread)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Basics"));

        verify(discussionService, times(1)).updateThread(anyInt(), any(DiscussionThread.class));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testDeleteThread() throws Exception {
        doNothing().when(discussionService).deleteThread(1);

        mockMvc.perform(delete("/api/discussion/threads/1"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).deleteThread(1);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testPinThread() throws Exception {
        doNothing().when(discussionService).pinThread(1);

        mockMvc.perform(put("/api/discussion/threads/1/pin"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).pinThread(1);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testUnpinThread() throws Exception {
        doNothing().when(discussionService).unpinThread(1);

        mockMvc.perform(put("/api/discussion/threads/1/unpin"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).unpinThread(1);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testCloseThread() throws Exception {
        doNothing().when(discussionService).closeThread(1);

        mockMvc.perform(put("/api/discussion/threads/1/close"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).closeThread(1);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testReopenThread() throws Exception {
        doNothing().when(discussionService).reopenThread(1);

        mockMvc.perform(put("/api/discussion/threads/1/reopen"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).reopenThread(1);
    }

    @Test
    public void testGetThreadsByAuthor() throws Exception {
        when(discussionService.getThreadsByAuthor(101)).thenReturn(Arrays.asList(testThread));

        mockMvc.perform(get("/api/discussion/threads/author/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(discussionService, times(1)).getThreadsByAuthor(101);
    }

    @Test
    public void testGetThreadCount() throws Exception {
        when(discussionService.getThreadCount(1)).thenReturn(5);

        mockMvc.perform(get("/api/discussion/threads/count/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(discussionService, times(1)).getThreadCount(1);
    }

    // ==================== REPLY ENDPOINT TESTS ====================

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testPostReply() throws Exception {
        when(discussionService.postReply(any(Reply.class))).thenReturn(testReply);

        mockMvc.perform(post("/api/discussion/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReply)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.replyId").value(1))
                .andExpect(jsonPath("$.body").value("Java is a programming language"));

        verify(discussionService, times(1)).postReply(any(Reply.class));
    }

    @Test
    public void testGetRepliesByThread() throws Exception {
        when(discussionService.getRepliesByThread(1)).thenReturn(Arrays.asList(testReply));

        mockMvc.perform(get("/api/discussion/replies/thread/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].body").value("Java is a programming language"));

        verify(discussionService, times(1)).getRepliesByThread(1);
    }


    @Test
    @WithMockUser(roles = "STUDENT")
    public void testUpdateReply() throws Exception {
        when(discussionService.updateReply(anyInt(), any(Reply.class))).thenReturn(testReply);

        mockMvc.perform(put("/api/discussion/replies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReply)))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).updateReply(anyInt(), any(Reply.class));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testDeleteReply() throws Exception {
        doNothing().when(discussionService).deleteReply(1);

        mockMvc.perform(delete("/api/discussion/replies/1"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).deleteReply(1);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testUpvoteReply() throws Exception {
        when(discussionService.upvoteReply(1, 101)).thenReturn(testReply);

        mockMvc.perform(put("/api/discussion/replies/1/upvote?studentId=101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replyId").value(1));

        verify(discussionService, times(1)).upvoteReply(1, 101);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testAcceptReply() throws Exception {
        testReply.setIsAccepted(true);
        when(discussionService.acceptReply(1)).thenReturn(testReply);

        mockMvc.perform(put("/api/discussion/replies/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAccepted").value(true));

        verify(discussionService, times(1)).acceptReply(1);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testUnacceptReply() throws Exception {
        doNothing().when(discussionService).unacceptReply(1);

        mockMvc.perform(put("/api/discussion/replies/1/unaccept"))
                .andExpect(status().isOk());

        verify(discussionService, times(1)).unacceptReply(1);
    }

    @Test
    public void testGetRepliesByAuthor() throws Exception {
        when(discussionService.getRepliesByAuthor(102)).thenReturn(Arrays.asList(testReply));

        mockMvc.perform(get("/api/discussion/replies/author/102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(discussionService, times(1)).getRepliesByAuthor(102);
    }
}

