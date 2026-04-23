package com.edulearn.discussion.repository;

import com.edulearn.discussion.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findByThreadId(Integer threadId);

    List<Reply> findByThreadIdOrderByIsAcceptedDescUpvotesDescCreatedAtAsc(Integer threadId);

    List<Reply> findByAuthorId(Integer authorId);

    int countByThreadId(Integer threadId);

    Optional<Reply> findByThreadIdAndIsAccepted(Integer threadId, Boolean isAccepted);
}

