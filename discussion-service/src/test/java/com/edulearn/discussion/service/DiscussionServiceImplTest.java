package com.edulearn.discussion.service;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.entity.UpvoteRecord;
import com.edulearn.discussion.repository.ReplyRepository;
import com.edulearn.discussion.repository.ThreadRepository;
import com.edulearn.discussion.repository.UpvoteRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DiscussionServiceImplTest {

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private UpvoteRecordRepository upvoteRecordRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== THREAD TESTS ====================

    @Test
    public void testCreateThread() {
        DiscussionThread thread = new DiscussionThread();
        thread.setCourseId(1);
        thread.setTitle("Java Basics");
        thread.setBody("What is Java?");
        thread.setAuthorId(101);

        when(threadRepository.save(any(DiscussionThread.class))).thenReturn(thread);

        DiscussionThread result = discussionService.createThread(thread);

        assertNotNull(result);
        assertFalse(result.getIsPinned());
        assertFalse(result.getIsClosed());
        verify(threadRepository, times(1)).save(thread);
    }

    @Test
    public void testGetThreadById() {
        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(1);
        thread.setTitle("Java Basics");

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));

        Optional<DiscussionThread> result = discussionService.getThreadById(1);

        assertTrue(result.isPresent());
        assertEquals("Java Basics", result.get().getTitle());
        verify(threadRepository, times(1)).findById(1);
    }

    @Test
    public void testUpdateThread() {
        Integer threadId = 1;
        DiscussionThread existingThread = new DiscussionThread();
        existingThread.setThreadId(threadId);
        existingThread.setTitle("Old Title");
        existingThread.setBody("Old Body");

        DiscussionThread updatedThread = new DiscussionThread();
        updatedThread.setTitle("New Title");
        updatedThread.setBody("New Body");

        when(threadRepository.findById(threadId)).thenReturn(Optional.of(existingThread));
        when(threadRepository.save(any(DiscussionThread.class))).thenReturn(existingThread);

        DiscussionThread result = discussionService.updateThread(threadId, updatedThread);

        assertEquals("New Title", result.getTitle());
        assertEquals("New Body", result.getBody());
        verify(threadRepository, times(1)).findById(threadId);
    }

    @Test
    public void testPinThread() {
        Integer threadId = 1;
        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(threadId);
        thread.setIsPinned(false);

        when(threadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadRepository.save(any(DiscussionThread.class))).thenReturn(thread);

        discussionService.pinThread(threadId);

        verify(threadRepository, times(1)).findById(threadId);
        verify(threadRepository, times(1)).save(thread);
    }

    @Test
    public void testCloseThread() {
        Integer threadId = 1;
        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(threadId);
        thread.setIsClosed(false);

        when(threadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadRepository.save(any(DiscussionThread.class))).thenReturn(thread);

        discussionService.closeThread(threadId);

        verify(threadRepository, times(1)).findById(threadId);
        verify(threadRepository, times(1)).save(thread);
    }

    @Test
    public void testDeleteThread() {
        Integer threadId = 1;
        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(threadId);

        when(threadRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(replyRepository.findByThreadId(threadId)).thenReturn(java.util.List.of());

        discussionService.deleteThread(threadId);

        verify(threadRepository, times(1)).delete(thread);
    }

    @Test
    public void testGetThreadCount() {
        Integer courseId = 1;
        when(threadRepository.countByCourseId(courseId)).thenReturn(5);

        int count = discussionService.getThreadCount(courseId);

        assertEquals(5, count);
        verify(threadRepository, times(1)).countByCourseId(courseId);
    }

    // ==================== REPLY TESTS ====================

    @Test
    public void testPostReply() {
        Reply reply = new Reply();
        reply.setThreadId(1);
        reply.setAuthorId(101);
        reply.setBody("This is a reply");

        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(1);
        thread.setIsClosed(false);

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);
        when(threadRepository.save(any(DiscussionThread.class))).thenReturn(thread);

        Reply result = discussionService.postReply(reply);

        assertNotNull(result);
        assertFalse(result.getIsAccepted());
        assertEquals(0, result.getUpvotes());
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    public void testPostReplyOnClosedThread() {
        Reply reply = new Reply();
        reply.setThreadId(1);

        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(1);
        thread.setIsClosed(true);

        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));

        assertThrows(RuntimeException.class, () -> discussionService.postReply(reply));
    }

    @Test
    public void testGetReplyById() {
        Reply reply = new Reply();
        reply.setReplyId(1);
        reply.setBody("Test reply");

        when(replyRepository.findById(1)).thenReturn(Optional.of(reply));

        Optional<Reply> result = discussionService.getReplyById(1);

        assertTrue(result.isPresent());
        assertEquals("Test reply", result.get().getBody());
    }

    @Test
    public void testUpvoteReply() {
        Integer replyId = 1;
        Integer studentId = 101;

        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setUpvotes(0);

        when(upvoteRecordRepository.existsByReplyIdAndStudentId(replyId, studentId)).thenReturn(false);
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(upvoteRecordRepository.save(any(UpvoteRecord.class))).thenReturn(new UpvoteRecord());
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);

        Reply result = discussionService.upvoteReply(replyId, studentId);

        assertEquals(1, result.getUpvotes());
        verify(upvoteRecordRepository, times(1)).save(any(UpvoteRecord.class));
    }

    @Test
    public void testUpvoteReplyAlreadyUpvoted() {
        Integer replyId = 1;
        Integer studentId = 101;

        when(upvoteRecordRepository.existsByReplyIdAndStudentId(replyId, studentId)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> discussionService.upvoteReply(replyId, studentId));
    }

    @Test
    public void testAcceptReply() {
        Integer replyId = 1;

        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setThreadId(1);
        reply.setIsAccepted(false);

        DiscussionThread thread = new DiscussionThread();
        thread.setThreadId(1);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(threadRepository.findById(1)).thenReturn(Optional.of(thread));
        when(replyRepository.findByThreadIdAndIsAccepted(1, true)).thenReturn(Optional.empty());
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);

        Reply result = discussionService.acceptReply(replyId);

        assertTrue(result.getIsAccepted());
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    public void testUnacceptReply() {
        Integer replyId = 1;

        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setIsAccepted(true);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);

        discussionService.unacceptReply(replyId);

        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    public void testDeleteReply() {
        Integer replyId = 1;

        Reply reply = new Reply();
        reply.setReplyId(replyId);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(upvoteRecordRepository.findByReplyId(replyId)).thenReturn(java.util.List.of());

        discussionService.deleteReply(replyId);

        verify(replyRepository, times(1)).delete(reply);
    }
}

