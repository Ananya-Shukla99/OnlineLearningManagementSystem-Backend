package com.edulearn.discussion.repository;

import com.edulearn.discussion.entity.UpvoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UpvoteRecordRepository extends JpaRepository<UpvoteRecord, Integer> {
    boolean existsByReplyIdAndStudentId(Integer replyId, Integer studentId);

    List<UpvoteRecord> findByReplyId(Integer replyId);

    List<UpvoteRecord> findByStudentId(Integer studentId);
}

