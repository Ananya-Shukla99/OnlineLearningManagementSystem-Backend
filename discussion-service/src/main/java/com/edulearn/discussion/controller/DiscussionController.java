package com.edulearn.discussion.controller;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/discussion")
@CrossOrigin(origins = "http://localhost:4200")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    // ==================== THREAD ENDPOINTS ====================

    @PostMapping("/threads")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DiscussionThread> createThread(@RequestBody DiscussionThread thread) {
        DiscussionThread created = discussionService.createThread(thread);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/threads/course/{courseId}")
    public ResponseEntity<List<DiscussionThread>> getThreadsByCourse(@PathVariable Integer courseId) {
        List<DiscussionThread> threads = discussionService.getThreadsByCourse(courseId);
        return ResponseEntity.ok(threads);
    }

    @GetMapping("/threads/lesson/{lessonId}")
    public ResponseEntity<List<DiscussionThread>> getThreadsByLesson(@PathVariable Integer lessonId) {
        List<DiscussionThread> threads = discussionService.getThreadsByLesson(lessonId);
        return ResponseEntity.ok(threads);
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<DiscussionThread> getThreadById(@PathVariable Integer threadId) {
        Optional<DiscussionThread> thread = discussionService.getThreadById(threadId);
        return thread.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/threads/{threadId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DiscussionThread> updateThread(
            @PathVariable Integer threadId,
            @RequestBody DiscussionThread updated) {
        try {
            DiscussionThread result = discussionService.updateThread(threadId, updated);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/threads/{threadId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> deleteThread(@PathVariable Integer threadId) {
        try {
            discussionService.deleteThread(threadId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/threads/{threadId}/pin")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> pinThread(@PathVariable Integer threadId) {
        try {
            discussionService.pinThread(threadId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/threads/{threadId}/unpin")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> unpinThread(@PathVariable Integer threadId) {
        try {
            discussionService.unpinThread(threadId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/threads/{threadId}/close")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> closeThread(@PathVariable Integer threadId) {
        try {
            discussionService.closeThread(threadId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/threads/{threadId}/reopen")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> reopenThread(@PathVariable Integer threadId) {
        try {
            discussionService.reopenThread(threadId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/threads/author/{authorId}")
    public ResponseEntity<List<DiscussionThread>> getThreadsByAuthor(@PathVariable Integer authorId) {
        List<DiscussionThread> threads = discussionService.getThreadsByAuthor(authorId);
        return ResponseEntity.ok(threads);
    }

    @GetMapping("/threads/count/{courseId}")
    public ResponseEntity<Integer> getThreadCount(@PathVariable Integer courseId) {
        int count = discussionService.getThreadCount(courseId);
        return ResponseEntity.ok(count);
    }

    // ==================== REPLY ENDPOINTS ====================

    @PostMapping("/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reply> postReply(@RequestBody Reply reply) {
        try {
            Reply created = discussionService.postReply(reply);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/replies/thread/{threadId}")
    public ResponseEntity<List<Reply>> getRepliesByThread(@PathVariable Integer threadId) {
        List<Reply> replies = discussionService.getRepliesByThread(threadId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/replies/{replyId}")
    public ResponseEntity<Reply> getReplyById(@PathVariable Integer replyId) {
        Optional<Reply> reply = discussionService.getReplyById(replyId);
        return reply.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/replies/{replyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reply> updateReply(
            @PathVariable Integer replyId,
            @RequestBody Reply updated) {
        try {
            Reply result = discussionService.updateReply(replyId, updated);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/replies/{replyId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> deleteReply(@PathVariable Integer replyId) {
        try {
            discussionService.deleteReply(replyId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/replies/{replyId}/upvote")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Reply> upvoteReply(
            @PathVariable Integer replyId,
            @RequestParam Integer studentId) {
        try {
            Reply result = discussionService.upvoteReply(replyId, studentId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/replies/{replyId}/accept")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Reply> acceptReply(@PathVariable Integer replyId) {
        try {
            Reply result = discussionService.acceptReply(replyId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/replies/{replyId}/unaccept")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<Void> unacceptReply(@PathVariable Integer replyId) {
        try {
            discussionService.unacceptReply(replyId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/replies/author/{authorId}")
    public ResponseEntity<List<Reply>> getRepliesByAuthor(@PathVariable Integer authorId) {
        List<Reply> replies = discussionService.getRepliesByAuthor(authorId);
        return ResponseEntity.ok(replies);
    }
}

