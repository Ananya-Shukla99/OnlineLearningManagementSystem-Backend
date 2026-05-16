package com.edulearn.course.controller;

import com.edulearn.course.entity.Wishlist;
import com.edulearn.course.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/courses/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @PostMapping("/toggle")
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestParam Long studentId, @RequestParam Long courseId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Wishlist> existing = wishlistRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            response.put("success", true);
            response.put("message", "Removed from wishlist");
            response.put("isWishlisted", false);
        } else {
            Wishlist w = new Wishlist();
            w.setStudentId(studentId);
            w.setCourseId(courseId);
            wishlistRepository.save(w);
            response.put("success", true);
            response.put("message", "Added to wishlist");
            response.put("isWishlisted", true);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{studentId}")
    public ResponseEntity<Map<String, Object>> getUserWishlist(@PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        List<Wishlist> wishlists = wishlistRepository.findByStudentId(studentId);
        List<Long> courseIds = wishlists.stream().map(Wishlist::getCourseId).collect(Collectors.toList());
        response.put("success", true);
        response.put("courseIds", courseIds);
        return ResponseEntity.ok(response);
    }
}
