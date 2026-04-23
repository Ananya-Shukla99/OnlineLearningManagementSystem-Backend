package com.edulearn.discussion.service;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.entity.UpvoteRecord;
import com.edulearn.discussion.repository.ReplyRepository;
import com.edulearn.discussion.repository.ThreadRepository;
import com.edulearn.discussion.repository.UpvoteRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UpvoteRecordRepository upvoteRecordRepository;

    // ==================== THREAD OPERATIONS ====================

    @Override
    public DiscussionThread createThread(DiscussionThread thread) {
        thread.setIsPinned(false);
        thread.setIsClosed(false);
        return threadRepository.save(thread);
    }

    @Override
    public List<DiscussionThread> getThreadsByCourse(Integer courseId) {
        return threadRepository.findByCourseIdOrderByIsPinnedDescCreatedAtDesc(courseId);
    }

    @Override
    public List<DiscussionThread> getThreadsByLesson(Integer lessonId) {
        return threadRepository.findByLessonId(lessonId);
    }

    @Override
    public Optional<DiscussionThread> getThreadById(Integer threadId) {
        return threadRepository.findById(threadId);
    }

    @Override
    public DiscussionThread updateThread(Integer threadId, DiscussionThread updated) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (updated.getTitle() != null) {
            thread.setTitle(updated.getTitle());
        }
        if (updated.getBody() != null) {
            thread.setBody(updated.getBody());
        }

        return threadRepository.save(thread);
    }

    @Override
    @Transactional
    public void deleteThread(Integer threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        // Get all replies for this thread
        List<Reply> replies = replyRepository.findByThreadId(threadId);

        // Delete all upvote records for these replies
        for (Reply reply : replies) {
            List<UpvoteRecord> upvotes = upvoteRecordRepository.findByReplyId(reply.getReplyId());
            upvoteRecordRepository.deleteAll(upvotes);
        }

        // Delete all replies
        replyRepository.deleteAll(replies);

        // Delete thread
        threadRepository.delete(thread);
    }

    @Override
    public void pinThread(Integer threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.setIsPinned(true);
        threadRepository.save(thread);
    }

    @Override
    public void unpinThread(Integer threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.setIsPinned(false);
        threadRepository.save(thread);
    }

    @Override
    public void closeThread(Integer threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.setIsClosed(true);
        threadRepository.save(thread);
    }

    @Override
    public void reopenThread(Integer threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.setIsClosed(false);
        threadRepository.save(thread);
    }

    @Override
    public List<DiscussionThread> getThreadsByAuthor(Integer authorId) {
        return threadRepository.findByAuthorId(authorId);
    }

    @Override
    public int getThreadCount(Integer courseId) {
        return threadRepository.countByCourseId(courseId);
    }

    // ==================== REPLY OPERATIONS ====================

    @Override
    @Transactional
    public Reply postReply(Reply reply) {
        // Check if thread is closed
        DiscussionThread thread = threadRepository.findById(reply.getThreadId())
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        if (thread.getIsClosed()) {
            throw new RuntimeException("Thread is closed");
        }

        reply.setIsAccepted(false);
        reply.setUpvotes(0);

        Reply saved = replyRepository.save(reply);

        // Update thread's updatedAt
        thread.setUpdatedAt(LocalDateTime.now());
        threadRepository.save(thread);

        return saved;
    }

    @Override
    public List<Reply> getRepliesByThread(Integer threadId) {
        return replyRepository.findByThreadIdOrderByIsAcceptedDescUpvotesDescCreatedAtAsc(threadId);
    }

    @Override
    public Optional<Reply> getReplyById(Integer replyId) {
        return replyRepository.findById(replyId);
    }

    @Override
    public Reply updateReply(Integer replyId, Reply updated) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        if (updated.getBody() != null) {
            reply.setBody(updated.getBody());
        }

        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public void deleteReply(Integer replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        // Delete all upvote records for this reply
        List<UpvoteRecord> upvotes = upvoteRecordRepository.findByReplyId(replyId);
        upvoteRecordRepository.deleteAll(upvotes);

        // Delete reply
        replyRepository.delete(reply);
    }

    @Override
    @Transactional
    public Reply upvoteReply(Integer replyId, Integer studentId) {
        // STEP 1: Check if already upvoted
        boolean alreadyUpvoted = upvoteRecordRepository.existsByReplyIdAndStudentId(replyId, studentId);
        if (alreadyUpvoted) {
            throw new RuntimeException("You have already upvoted this reply");
        }

        // STEP 2: Create upvote record
        UpvoteRecord record = new UpvoteRecord();
        record.setReplyId(replyId);
        record.setStudentId(studentId);
        upvoteRecordRepository.save(record);

        // STEP 3: Increment upvote count on the reply
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));
        reply.setUpvotes(reply.getUpvotes() + 1);
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public Reply acceptReply(Integer replyId) {
        // STEP 1: Load the reply
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        // STEP 2: Load the thread
        DiscussionThread thread = threadRepository.findById(reply.getThreadId())
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        // STEP 3: Find if any other reply is already accepted
        Optional<Reply> alreadyAccepted = replyRepository.findByThreadIdAndIsAccepted(reply.getThreadId(), true);
        if (alreadyAccepted.isPresent()) {
            alreadyAccepted.get().setIsAccepted(false);
            replyRepository.save(alreadyAccepted.get());
        }

        // STEP 4: Accept this reply
        reply.setIsAccepted(true);
        return replyRepository.save(reply);
    }

    @Override
    public void unacceptReply(Integer replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));
        reply.setIsAccepted(false);
        replyRepository.save(reply);
    }

    @Override
    public List<Reply> getRepliesByAuthor(Integer authorId) {
        return replyRepository.findByAuthorId(authorId);
    }
}

