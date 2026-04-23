package com.edulearn.discussion.service;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.entity.UpvoteRecord;
import com.edulearn.discussion.repository.ReplyRepository;
import com.edulearn.discussion.repository.ThreadRepository;
import com.edulearn.discussion.repository.UpvoteRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscussionServiceImpl Unit Tests")
class DiscussionServiceImplTest {

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private UpvoteRecordRepository upvoteRecordRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private DiscussionThread testThread;
    private Reply testReply;

    @BeforeEach
    void setUp() {
        testThread = new DiscussionThread();
        testThread.setThreadId(1);
        testThread.setCourseId(10);
        testThread.setLessonId(20);
        testThread.setTitle("Original Title");
        testThread.setBody("Original Body");
        testThread.setAuthorId(101);
        testThread.setIsPinned(false);
        testThread.setIsClosed(false);

        testReply = new Reply();
        testReply.setReplyId(1);
        testReply.setThreadId(1);
        testReply.setAuthorId(102);
        testReply.setBody("Original Reply");
        testReply.setUpvotes(0);
        testReply.setIsAccepted(false);
    }

    // ─── THREAD OPERATIONS ────────────────────────────────────────────

    @Nested
    @DisplayName("Thread Operations")
    class ThreadOperations {

        @Test
        @DisplayName("createThread - should initialize status and save")
        void testCreateThread() {
            when(threadRepository.save(any(DiscussionThread.class))).thenReturn(testThread);

            DiscussionThread result = discussionService.createThread(testThread);

            assertNotNull(result);
            assertFalse(result.getIsPinned());
            assertFalse(result.getIsClosed());
            verify(threadRepository).save(testThread);
        }

        @Test
        @DisplayName("updateThread - success")
        void testUpdateThreadSuccess() {
            DiscussionThread update = new DiscussionThread();
            update.setTitle("New Title");
            update.setBody("New Body");

            when(threadRepository.findById(1)).thenReturn(Optional.of(testThread));
            when(threadRepository.save(any(DiscussionThread.class))).thenReturn(testThread);

            DiscussionThread result = discussionService.updateThread(1, update);

            assertEquals("New Title", result.getTitle());
            assertEquals("New Body", result.getBody());
        }

        @Test
        @DisplayName("updateThread - not found")
        void testUpdateThreadNotFound() {
            when(threadRepository.findById(999)).thenReturn(Optional.empty());
            DiscussionThread update = new DiscussionThread();

            assertThrows(RuntimeException.class, () -> discussionService.updateThread(999, update));
        }

        @Test
        @DisplayName("deleteThread - should delete replies and upvotes")
        void testDeleteThread() {
            when(threadRepository.findById(1)).thenReturn(Optional.of(testThread));
            when(replyRepository.findByThreadId(1)).thenReturn(List.of(testReply));
            when(upvoteRecordRepository.findByReplyId(1)).thenReturn(List.of(new UpvoteRecord()));

            discussionService.deleteThread(1);

            verify(upvoteRecordRepository).deleteAll(anyList());
            verify(replyRepository).deleteAll(anyList());
            verify(threadRepository).delete(testThread);
        }

        @Test
        @DisplayName("pin/unpin/close/reopen operations")
        void testStatusOperations() {
            when(threadRepository.findById(1)).thenReturn(Optional.of(testThread));

            discussionService.pinThread(1);
            assertTrue(testThread.getIsPinned());

            discussionService.unpinThread(1);
            assertFalse(testThread.getIsPinned());

            discussionService.closeThread(1);
            assertTrue(testThread.getIsClosed());

            discussionService.reopenThread(1);
            assertFalse(testThread.getIsClosed());

            verify(threadRepository, times(4)).save(testThread);
        }
        
        @Test
        @DisplayName("getThreadCount - success")
        void testGetThreadCount() {
            when(threadRepository.countByCourseId(10)).thenReturn(5);
            assertEquals(5, discussionService.getThreadCount(10));
        }
    }

    // ─── REPLY OPERATIONS ─────────────────────────────────────────────

    @Nested
    @DisplayName("Reply Operations")
    class ReplyOperations {

        @Test
        @DisplayName("postReply - success")
        void testPostReplySuccess() {
            when(threadRepository.findById(1)).thenReturn(Optional.of(testThread));
            when(replyRepository.save(any(Reply.class))).thenReturn(testReply);

            Reply result = discussionService.postReply(testReply);

            assertNotNull(result);
            assertFalse(result.getIsAccepted());
            assertEquals(0, result.getUpvotes());
            verify(threadRepository).save(testThread); // check if updatedAt updated
        }

        @Test
        @DisplayName("postReply - thread closed")
        void testPostReplyThreadClosed() {
            testThread.setIsClosed(true);
            when(threadRepository.findById(1)).thenReturn(Optional.of(testThread));

            assertThrows(RuntimeException.class, () -> discussionService.postReply(testReply));
        }

        @Test
        @DisplayName("updateReply - success")
        void testUpdateReplySuccess() {
            Reply update = new Reply();
            update.setBody("New Body");

            when(replyRepository.findById(1)).thenReturn(Optional.of(testReply));
            when(replyRepository.save(any(Reply.class))).thenReturn(testReply);

            Reply result = discussionService.updateReply(1, update);

            assertEquals("New Body", result.getBody());
        }

        @Test
        @DisplayName("upvoteReply - success")
        void testUpvoteReplySuccess() {
            when(upvoteRecordRepository.existsByReplyIdAndStudentId(1, 101)).thenReturn(false);
            when(replyRepository.findById(1)).thenReturn(Optional.of(testReply));
            when(replyRepository.save(any(Reply.class))).thenReturn(testReply);

            Reply result = discussionService.upvoteReply(1, 101);

            assertEquals(1, result.getUpvotes());
            verify(upvoteRecordRepository).save(any(UpvoteRecord.class));
        }

        @Test
        @DisplayName("upvoteReply - already upvoted")
        void testUpvoteReplyAlreadyUpvoted() {
            when(upvoteRecordRepository.existsByReplyIdAndStudentId(1, 101)).thenReturn(true);

            assertThrows(RuntimeException.class, () -> discussionService.upvoteReply(1, 101));
        }

        @Test
        @DisplayName("acceptReply - switches acceptance")
        void testAcceptReply() {
            Reply alreadyAccepted = new Reply();
            alreadyAccepted.setIsAccepted(true);

            when(replyRepository.findById(1)).thenReturn(Optional.of(testReply));
            when(threadRepository.findById(1)).thenReturn(Optional.of(testThread));
            when(replyRepository.findByThreadIdAndIsAccepted(1, true)).thenReturn(Optional.of(alreadyAccepted));
            when(replyRepository.save(any(Reply.class))).thenReturn(testReply);

            Reply result = discussionService.acceptReply(1);

            assertTrue(result.getIsAccepted());
            assertFalse(alreadyAccepted.getIsAccepted());
            verify(replyRepository, times(2)).save(any(Reply.class));
        }

        @Test
        @DisplayName("unacceptReply - success")
        void testUnacceptReply() {
            testReply.setIsAccepted(true);
            when(replyRepository.findById(1)).thenReturn(Optional.of(testReply));
            when(replyRepository.save(any(Reply.class))).thenReturn(testReply);

            discussionService.unacceptReply(1);

            assertFalse(testReply.getIsAccepted());
        }
        
        @Test
        @DisplayName("deleteReply - should remove upvotes")
        void testDeleteReply() {
            when(replyRepository.findById(1)).thenReturn(Optional.of(testReply));
            when(upvoteRecordRepository.findByReplyId(1)).thenReturn(List.of(new UpvoteRecord()));

            discussionService.deleteReply(1);

            verify(upvoteRecordRepository).deleteAll(anyList());
            verify(replyRepository).delete(testReply);
        }
    }

    // ─── QUERY OPERATIONS ─────────────────────────────────────────────

    @Nested
    @DisplayName("Query Operations")
    class QueryOperations {

        @Test
        void testQueries() {
            when(threadRepository.findByCourseIdOrderByIsPinnedDescCreatedAtDesc(10)).thenReturn(List.of(testThread));
            assertFalse(discussionService.getThreadsByCourse(10).isEmpty());

            when(threadRepository.findByLessonId(20)).thenReturn(List.of(testThread));
            assertFalse(discussionService.getThreadsByLesson(20).isEmpty());

            when(threadRepository.findByAuthorId(101)).thenReturn(List.of(testThread));
            assertFalse(discussionService.getThreadsByAuthor(101).isEmpty());

            when(replyRepository.findByThreadIdOrderByIsAcceptedDescUpvotesDescCreatedAtAsc(1)).thenReturn(List.of(testReply));
            assertFalse(discussionService.getRepliesByThread(1).isEmpty());

            when(replyRepository.findByAuthorId(102)).thenReturn(List.of(testReply));
            assertFalse(discussionService.getRepliesByAuthor(102).isEmpty());
        }
    }
}
