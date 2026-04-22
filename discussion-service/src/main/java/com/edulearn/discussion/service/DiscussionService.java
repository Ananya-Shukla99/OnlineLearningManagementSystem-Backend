package com.edulearn.discussion.service;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import java.util.List;
import java.util.Optional;

public interface DiscussionService {
    // Thread operations
    DiscussionThread createThread(DiscussionThread thread);
    List<DiscussionThread> getThreadsByCourse(Integer courseId);
    List<DiscussionThread> getThreadsByLesson(Integer lessonId);
    Optional<DiscussionThread> getThreadById(Integer threadId);
    DiscussionThread updateThread(Integer threadId, DiscussionThread updated);
    void deleteThread(Integer threadId);
    void pinThread(Integer threadId);
    void unpinThread(Integer threadId);
    void closeThread(Integer threadId);
    void reopenThread(Integer threadId);
    List<DiscussionThread> getThreadsByAuthor(Integer authorId);
    int getThreadCount(Integer courseId);

    // Reply operations
    Reply postReply(Reply reply);
    List<Reply> getRepliesByThread(Integer threadId);
    Optional<Reply> getReplyById(Integer replyId);
    Reply updateReply(Integer replyId, Reply updated);
    void deleteReply(Integer replyId);
    Reply upvoteReply(Integer replyId, Integer studentId);
    Reply acceptReply(Integer replyId);
    void unacceptReply(Integer replyId);
    List<Reply> getRepliesByAuthor(Integer authorId);
}

