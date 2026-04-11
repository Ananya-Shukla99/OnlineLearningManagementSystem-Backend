package com.edulearn.discussion.repository;

import com.edulearn.discussion.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<DiscussionThread, Integer> {
    List<DiscussionThread> findByCourseId(Integer courseId);

    List<DiscussionThread> findByCourseIdOrderByIsPinnedDescCreatedAtDesc(Integer courseId);

    List<DiscussionThread> findByLessonId(Integer lessonId);

    List<DiscussionThread> findByAuthorId(Integer authorId);

    List<DiscussionThread> findByCourseIdAndIsPinned(Integer courseId, Boolean isPinned);

    int countByCourseId(Integer courseId);
}

