package com.edulearn.lesson.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonServiceImpl Unit Tests")
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        testLesson = new Lesson();
        testLesson.setLessonId(1);
        testLesson.setCourseId(101);
        testLesson.setTitle("Test Lesson");
        testLesson.setDescription("Test Description");
        testLesson.setContentType("VIDEO");
        testLesson.setContentUrl("http://example.com/video.mp4");
        testLesson.setDurationMinutes(30);
        testLesson.setOrderIndex(1);
        testLesson.setIsPreview(false);
    }

    // ─── addLesson ────────────────────────────────────────────────────

    @Nested
    @DisplayName("addLesson()")
    class AddLesson {

        @Test
        @DisplayName("Should save and return lesson")
        void testAddLesson() {
            when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

            Lesson result = lessonService.addLesson(testLesson);

            assertNotNull(result);
            assertEquals("Test Lesson", result.getTitle());
            assertEquals(101, result.getCourseId());
            verify(lessonRepository, times(1)).save(testLesson);
        }

        @Test
        @DisplayName("Should preserve all fields when saving")
        void testAddLessonPreservesFields() {
            when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

            Lesson result = lessonService.addLesson(testLesson);

            assertEquals("VIDEO", result.getContentType());
            assertEquals("http://example.com/video.mp4", result.getContentUrl());
            assertEquals(30, result.getDurationMinutes());
            assertEquals(1, result.getOrderIndex());
            assertFalse(result.getIsPreview());
        }
    }

    // ─── getLessonsByCourse ───────────────────────────────────────────

    @Nested
    @DisplayName("getLessonsByCourse()")
    class GetLessonsByCourse {

        @Test
        @DisplayName("Should return lessons ordered by index")
        void testGetLessonsByCourse() {
            Lesson second = new Lesson();
            second.setLessonId(2);
            second.setCourseId(101);
            second.setOrderIndex(2);

            when(lessonRepository.findByCourseIdOrderByOrderIndex(101))
                    .thenReturn(List.of(testLesson, second));

            List<Lesson> result = lessonService.getLessonsByCourse(101);

            assertEquals(2, result.size());
            assertEquals(1, result.get(0).getOrderIndex());
            assertEquals(2, result.get(1).getOrderIndex());
            verify(lessonRepository).findByCourseIdOrderByOrderIndex(101);
        }

        @Test
        @DisplayName("Should return empty list when no lessons")
        void testGetLessonsByCourseEmpty() {
            when(lessonRepository.findByCourseIdOrderByOrderIndex(999))
                    .thenReturn(Collections.emptyList());

            List<Lesson> result = lessonService.getLessonsByCourse(999);

            assertTrue(result.isEmpty());
        }
    }

    // ─── getLessonById ────────────────────────────────────────────────

    @Nested
    @DisplayName("getLessonById()")
    class GetLessonById {

        @Test
        @DisplayName("Should return lesson when found")
        void testGetLessonById() {
            when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));

            Lesson result = lessonService.getLessonById(1);

            assertNotNull(result);
            assertEquals(1, result.getLessonId());
            assertEquals("Test Lesson", result.getTitle());
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void testGetLessonByIdNotFound() {
            when(lessonRepository.findById(999)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> lessonService.getLessonById(999));
            assertTrue(ex.getMessage().contains("Lesson not found with ID: 999"));
        }
    }

    // ─── updateLesson ─────────────────────────────────────────────────

    @Nested
    @DisplayName("updateLesson()")
    class UpdateLesson {

        @Test
        @DisplayName("Should update all mutable fields")
        void testUpdateLesson() {
            Lesson updateInfo = new Lesson();
            updateInfo.setTitle("Updated Title");
            updateInfo.setDescription("Updated Description");
            updateInfo.setContentType("PDF");
            updateInfo.setContentUrl("http://example.com/doc.pdf");
            updateInfo.setDurationMinutes(45);
            updateInfo.setIsPreview(true);

            when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));
            when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

            Lesson result = lessonService.updateLesson(1, updateInfo);

            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated Description", result.getDescription());
            assertEquals("PDF", result.getContentType());
            assertEquals("http://example.com/doc.pdf", result.getContentUrl());
            assertEquals(45, result.getDurationMinutes());
            assertTrue(result.getIsPreview());
            verify(lessonRepository).save(any(Lesson.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent lesson")
        void testUpdateLessonNotFound() {
            when(lessonRepository.findById(999)).thenReturn(Optional.empty());
            Lesson update = new Lesson();

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> lessonService.updateLesson(999, update));
            assertTrue(ex.getMessage().contains("999"));
            verify(lessonRepository, never()).save(any());
        }
    }

    // ─── deleteLesson ─────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteLesson()")
    class DeleteLesson {

        @Test
        @DisplayName("Should delete lesson and its resources")
        void testDeleteLesson() {
            Resource r1 = new Resource();
            r1.setResourceId(10);
            List<Resource> resources = List.of(r1);

            when(resourceRepository.findByLessonId(1)).thenReturn(resources);

            lessonService.deleteLesson(1);

            verify(resourceRepository).deleteAll(resources);
            verify(lessonRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should delete lesson with no resources")
        void testDeleteLessonNoResources() {
            when(resourceRepository.findByLessonId(1)).thenReturn(Collections.emptyList());

            lessonService.deleteLesson(1);

            verify(resourceRepository).deleteAll(Collections.emptyList());
            verify(lessonRepository).deleteById(1);
        }
    }

    // ─── reorderLessons ───────────────────────────────────────────────

    @Nested
    @DisplayName("reorderLessons()")
    class ReorderLessons {

        @Test
        @DisplayName("Should reorder all lessons")
        void testReorderLessons() {
            List<Integer> lessonIds = List.of(3, 1, 2);

            Lesson l1 = new Lesson(); l1.setLessonId(3);
            Lesson l2 = new Lesson(); l2.setLessonId(1);
            Lesson l3 = new Lesson(); l3.setLessonId(2);

            when(lessonRepository.findById(3)).thenReturn(Optional.of(l1));
            when(lessonRepository.findById(1)).thenReturn(Optional.of(l2));
            when(lessonRepository.findById(2)).thenReturn(Optional.of(l3));

            lessonService.reorderLessons(101, lessonIds);

            assertEquals(0, l1.getOrderIndex());
            assertEquals(1, l2.getOrderIndex());
            assertEquals(2, l3.getOrderIndex());
            verify(lessonRepository, times(3)).save(any(Lesson.class));
        }

        @Test
        @DisplayName("Should throw exception when lesson ID not found during reorder")
        void testReorderLessonNotFound() {
            when(lessonRepository.findById(999)).thenReturn(Optional.empty());
            List<Integer> ids = List.of(999);

            assertThrows(RuntimeException.class,
                    () -> lessonService.reorderLessons(101, ids));
        }

        @Test
        @DisplayName("Should handle empty list gracefully")
        void testReorderEmptyList() {
            lessonService.reorderLessons(101, Collections.emptyList());

            verify(lessonRepository, never()).findById(anyInt());
            verify(lessonRepository, never()).save(any());
        }
    }

    // ─── addResource ──────────────────────────────────────────────────

    @Nested
    @DisplayName("addResource()")
    class AddResource {

        @Test
        @DisplayName("Should add resource and set lessonId")
        void testAddResource() {
            Resource resource = new Resource();
            resource.setName("Handout");
            resource.setFileUrl("/files/handout.pdf");
            resource.setFileType("PDF");
            resource.setSizeKb(512L);

            when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));
            when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

            Resource result = lessonService.addResource(1, resource);

            assertNotNull(result);
            assertEquals(1, resource.getLessonId());
            assertEquals("Handout", result.getName());
            verify(resourceRepository).save(resource);
        }

        @Test
        @DisplayName("Should throw exception when lesson not found")
        void testAddResourceLessonNotFound() {
            when(lessonRepository.findById(999)).thenReturn(Optional.empty());
            Resource resource = new Resource();

            assertThrows(RuntimeException.class,
                    () -> lessonService.addResource(999, resource));
            verify(resourceRepository, never()).save(any());
        }
    }

    // ─── removeResource ───────────────────────────────────────────────

    @Nested
    @DisplayName("removeResource()")
    class RemoveResource {

        @Test
        @DisplayName("Should delete resource by ID")
        void testRemoveResource() {
            lessonService.removeResource(10);

            verify(resourceRepository).deleteById(10);
        }
    }

    // ─── getPreviewLessons ────────────────────────────────────────────

    @Nested
    @DisplayName("getPreviewLessons()")
    class GetPreviewLessons {

        @Test
        @DisplayName("Should return only preview lessons")
        void testGetPreviewLessons() {
            Lesson preview = new Lesson();
            preview.setLessonId(2);
            preview.setCourseId(101);
            preview.setIsPreview(true);

            Lesson nonPreview = new Lesson();
            nonPreview.setLessonId(1);
            nonPreview.setCourseId(101);
            nonPreview.setIsPreview(false);

            when(lessonRepository.findByCourseIdOrderByOrderIndex(101))
                    .thenReturn(List.of(nonPreview, preview));

            List<Lesson> result = lessonService.getPreviewLessons(101);

            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsPreview());
            assertEquals(2, result.get(0).getLessonId());
        }

        @Test
        @DisplayName("Should return empty when no preview lessons")
        void testGetPreviewLessonsNone() {
            Lesson nonPreview = new Lesson();
            nonPreview.setIsPreview(false);

            when(lessonRepository.findByCourseIdOrderByOrderIndex(101))
                    .thenReturn(List.of(nonPreview));

            List<Lesson> result = lessonService.getPreviewLessons(101);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return all when all are preview")
        void testGetPreviewLessonsAll() {
            Lesson p1 = new Lesson(); p1.setIsPreview(true);
            Lesson p2 = new Lesson(); p2.setIsPreview(true);

            when(lessonRepository.findByCourseIdOrderByOrderIndex(101))
                    .thenReturn(List.of(p1, p2));

            List<Lesson> result = lessonService.getPreviewLessons(101);

            assertEquals(2, result.size());
        }
    }

    // ─── countLessonsByCourse ─────────────────────────────────────────

    @Nested
    @DisplayName("countLessonsByCourse()")
    class CountLessonsByCourse {

        @Test
        @DisplayName("Should return correct count")
        void testCountLessonsByCourse() {
            when(lessonRepository.countByCourseId(101)).thenReturn(5);

            int count = lessonService.countLessonsByCourse(101);

            assertEquals(5, count);
            verify(lessonRepository).countByCourseId(101);
        }

        @Test
        @DisplayName("Should return zero for nonexistent course")
        void testCountZero() {
            when(lessonRepository.countByCourseId(999)).thenReturn(0);

            assertEquals(0, lessonService.countLessonsByCourse(999));
        }
    }
}
