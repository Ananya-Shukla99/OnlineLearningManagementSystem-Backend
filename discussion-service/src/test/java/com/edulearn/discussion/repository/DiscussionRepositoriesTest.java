package com.edulearn.discussion.repository;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.entity.UpvoteRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class DiscussionRepositoriesTest {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UpvoteRecordRepository upvoteRecordRepository;

    @Autowired
    private EntityManager entityManager;

    private DiscussionThread testThread;
    private Reply testReply;

    @BeforeEach
    public void setUp() {
        testThread = new DiscussionThread();
        testThread.setCourseId(1);
        testThread.setAuthorId(101);
        testThread.setTitle("Thread Title");
        testThread.setBody("Thread Body");
        testThread.setIsPinned(false);
        testThread.setIsClosed(false);

        testReply = new Reply();
        testReply.setAuthorId(102);
        testReply.setBody("Reply Body");
        testReply.setIsAccepted(false);
        testReply.setUpvotes(0);
    }

    // ==================== THREAD REPOSITORY TESTS ====================

    @Test
    public void testSaveThread() {
        DiscussionThread saved = threadRepository.save(testThread);
        entityManager.flush();

        assertNotNull(saved.getThreadId());
        assertEquals("Thread Title", saved.getTitle());
    }

    @Test
    public void testFindThreadById() {
        DiscussionThread saved = threadRepository.save(testThread);
        entityManager.flush();

        Optional<DiscussionThread> found = threadRepository.findById(saved.getThreadId());

        assertTrue(found.isPresent());
        assertEquals(saved.getThreadId(), found.get().getThreadId());
    }

    @Test
    public void testFindByCourseId() {
        threadRepository.save(testThread);
        entityManager.flush();

        List<DiscussionThread> threads = threadRepository.findByCourseId(1);

        assertFalse(threads.isEmpty());
        assertEquals(1, threads.size());
    }

    @Test
    public void testFindByCourseIdOrderByIsPinnedDescCreatedAtDesc() {
        DiscussionThread pinnedThread = new DiscussionThread();
        pinnedThread.setCourseId(1);
        pinnedThread.setAuthorId(101);
        pinnedThread.setTitle("Pinned");
        pinnedThread.setBody("Pinned Body");
        pinnedThread.setIsPinned(true);
        pinnedThread.setIsClosed(false);

        threadRepository.save(testThread);
        threadRepository.save(pinnedThread);
        entityManager.flush();

        List<DiscussionThread> threads = threadRepository.findByCourseIdOrderByIsPinnedDescCreatedAtDesc(1);

        assertEquals(2, threads.size());
        assertTrue(threads.get(0).getIsPinned());
    }

    @Test
    public void testFindByAuthorId() {
        threadRepository.save(testThread);
        entityManager.flush();

        List<DiscussionThread> threads = threadRepository.findByAuthorId(101);

        assertFalse(threads.isEmpty());
        assertEquals(101, threads.get(0).getAuthorId());
    }

    @Test
    public void testCountByCourseId() {
        // Create first thread
        DiscussionThread thread1 = new DiscussionThread();
        thread1.setCourseId(1);
        thread1.setAuthorId(101);
        thread1.setTitle("Thread 1");
        thread1.setBody("Body 1");
        thread1.setIsPinned(false);
        thread1.setIsClosed(false);
        threadRepository.save(thread1);

        // Create second thread
        DiscussionThread thread2 = new DiscussionThread();
        thread2.setCourseId(1);
        thread2.setAuthorId(101);
        thread2.setTitle("Thread 2");
        thread2.setBody("Body 2");
        thread2.setIsPinned(false);
        thread2.setIsClosed(false);
        threadRepository.save(thread2);

        entityManager.flush();

        int count = threadRepository.countByCourseId(1);

        assertEquals(2, count);
    }

    @Test
    public void testFindByCourseIdAndIsPinned() {
        testThread.setIsPinned(true);
        threadRepository.save(testThread);
        entityManager.flush();

        List<DiscussionThread> threads = threadRepository.findByCourseIdAndIsPinned(1, true);

        assertFalse(threads.isEmpty());
        assertTrue(threads.get(0).getIsPinned());
    }

    // ==================== REPLY REPOSITORY TESTS ====================

    @Test
    public void testSaveReply() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        Reply saved = replyRepository.save(testReply);
        entityManager.flush();

        assertNotNull(saved.getReplyId());
        assertEquals("Reply Body", saved.getBody());
    }

    @Test
    public void testFindByThreadId() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        replyRepository.save(testReply);
        entityManager.flush();

        List<Reply> replies = replyRepository.findByThreadId(savedThread.getThreadId());

        assertFalse(replies.isEmpty());
        assertEquals(1, replies.size());
    }

    @Test
    public void testFindByThreadIdOrderByIsAcceptedDescUpvotesDescCreatedAtAsc() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        Reply reply1 = new Reply();
        reply1.setThreadId(savedThread.getThreadId());
        reply1.setAuthorId(102);
        reply1.setBody("Reply 1");
        reply1.setIsAccepted(true);
        reply1.setUpvotes(5);

        Reply reply2 = new Reply();
        reply2.setThreadId(savedThread.getThreadId());
        reply2.setAuthorId(103);
        reply2.setBody("Reply 2");
        reply2.setIsAccepted(false);
        reply2.setUpvotes(10);

        replyRepository.save(reply1);
        replyRepository.save(reply2);
        entityManager.flush();

        List<Reply> replies = replyRepository.findByThreadIdOrderByIsAcceptedDescUpvotesDescCreatedAtAsc(savedThread.getThreadId());

        assertEquals(2, replies.size());
        assertTrue(replies.get(0).getIsAccepted()); // Accepted first
    }

    @Test
    public void testFindByAuthorIdReply() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        replyRepository.save(testReply);
        entityManager.flush();

        List<Reply> replies = replyRepository.findByAuthorId(102);

        assertFalse(replies.isEmpty());
        assertEquals(102, replies.get(0).getAuthorId());
    }

    @Test
    public void testCountByThreadId() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        // Create first reply
        Reply reply1 = new Reply();
        reply1.setThreadId(savedThread.getThreadId());
        reply1.setAuthorId(102);
        reply1.setBody("First Reply");
        reply1.setIsAccepted(false);
        reply1.setUpvotes(0);
        replyRepository.save(reply1);

        // Create second reply
        Reply reply2 = new Reply();
        reply2.setThreadId(savedThread.getThreadId());
        reply2.setAuthorId(103);
        reply2.setBody("Second Reply");
        reply2.setIsAccepted(false);
        reply2.setUpvotes(0);
        replyRepository.save(reply2);

        entityManager.flush();

        int count = replyRepository.countByThreadId(savedThread.getThreadId());

        assertEquals(2, count);
    }

    @Test
    public void testFindByThreadIdAndIsAccepted() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        testReply.setIsAccepted(true);
        replyRepository.save(testReply);
        entityManager.flush();

        Optional<Reply> reply = replyRepository.findByThreadIdAndIsAccepted(savedThread.getThreadId(), true);

        assertTrue(reply.isPresent());
        assertTrue(reply.get().getIsAccepted());
    }

    // ==================== UPVOTE RECORD REPOSITORY TESTS ====================

    @Test
    public void testSaveUpvoteRecord() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        Reply savedReply = replyRepository.save(testReply);
        entityManager.flush();

        UpvoteRecord record = new UpvoteRecord();
        record.setReplyId(savedReply.getReplyId());
        record.setStudentId(101);

        UpvoteRecord saved = upvoteRecordRepository.save(record);
        entityManager.flush();

        assertNotNull(saved.getUpvoteId());
    }

    @Test
    public void testExistsByReplyIdAndStudentId() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        Reply savedReply = replyRepository.save(testReply);
        entityManager.flush();

        UpvoteRecord record = new UpvoteRecord();
        record.setReplyId(savedReply.getReplyId());
        record.setStudentId(101);
        upvoteRecordRepository.save(record);
        entityManager.flush();

        boolean exists = upvoteRecordRepository.existsByReplyIdAndStudentId(savedReply.getReplyId(), 101);

        assertTrue(exists);
    }

    @Test
    public void testExistsByReplyIdAndStudentIdNotFound() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        Reply savedReply = replyRepository.save(testReply);
        entityManager.flush();

        boolean exists = upvoteRecordRepository.existsByReplyIdAndStudentId(savedReply.getReplyId(), 999);

        assertFalse(exists);
    }

    @Test
    public void testFindByReplyId() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        Reply savedReply = replyRepository.save(testReply);
        entityManager.flush();

        UpvoteRecord record = new UpvoteRecord();
        record.setReplyId(savedReply.getReplyId());
        record.setStudentId(101);
        upvoteRecordRepository.save(record);
        entityManager.flush();

        List<UpvoteRecord> records = upvoteRecordRepository.findByReplyId(savedReply.getReplyId());

        assertFalse(records.isEmpty());
        assertEquals(1, records.size());
    }

    @Test
    public void testFindByStudentId() {
        DiscussionThread savedThread = threadRepository.save(testThread);
        entityManager.flush();

        testReply.setThreadId(savedThread.getThreadId());
        Reply savedReply = replyRepository.save(testReply);
        entityManager.flush();

        UpvoteRecord record = new UpvoteRecord();
        record.setReplyId(savedReply.getReplyId());
        record.setStudentId(101);
        upvoteRecordRepository.save(record);
        entityManager.flush();

        List<UpvoteRecord> records = upvoteRecordRepository.findByStudentId(101);

        assertFalse(records.isEmpty());
        assertEquals(101, records.get(0).getStudentId());
    }
}

