package com.edulearn.lesson.service;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("Lesson Service Implementation Tests")
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Lesson testLesson;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testLesson = new Lesson();
        testLesson.setLessonId(1);
        testLesson.setCourseId(5);
        testLesson.setTitle("Java Basics");
        testLesson.setContentType("VIDEO");
        testLesson.setContentUrl("https://example.com/video.mp4");
        testLesson.setDurationMinutes(30);
        testLesson.setOrderIndex(0);
        testLesson.setDescription("Learn Java fundamentals");
        testLesson.setIsPreview(false);

        testResource = new Resource();
        testResource.setResourceId(1);
        testResource.setLessonId(1);
        testResource.setName("Java Cheat Sheet");
        testResource.setFileUrl("https://example.com/cheatsheet.pdf");
        testResource.setFileType("PDF");
        testResource.setSizeKb(500L);
    }

    // ==================== addLesson Tests ====================

    @Test
    @DisplayName("Should add lesson successfully")
    void testAddLessonSuccess() {
        // Arrange
        when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

        // Act
        Lesson result = lessonService.addLesson(testLesson);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLessonId());
        assertEquals("Java Basics", result.getTitle());
        assertEquals(5, result.getCourseId());
        verify(lessonRepository, times(1)).save(testLesson);
    }

    @Test
    @DisplayName("Should save lesson with all fields populated")
    void testAddLessonAllFields() {
        // Arrange
        when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

        // Act
        Lesson result = lessonService.addLesson(testLesson);

        // Assert
        assertEquals("VIDEO", result.getContentType());
        assertEquals("https://example.com/video.mp4", result.getContentUrl());
        assertEquals(30, result.getDurationMinutes());
        assertEquals(0, result.getOrderIndex());
        assertFalse(result.getIsPreview());
    }

    // ==================== getLessonsByCourse Tests ====================

    @Test
    @DisplayName("Should get lessons by course ID ordered by orderIndex")
    void testGetLessonsByCourse() {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Lesson 1", "VIDEO", "url1", 20, 0, "Desc1", false);
        Lesson lesson2 = new Lesson(2, 5, "Lesson 2", "ARTICLE", "url2", 15, 1, "Desc2", false);
        Lesson lesson3 = new Lesson(3, 5, "Lesson 3", "PDF", "url3", 25, 2, "Desc3", true);

        List<Lesson> lessons = Arrays.asList(lesson1, lesson2, lesson3);

        when(lessonRepository.findByCourseIdOrderByOrderIndex(5)).thenReturn(lessons);

        // Act
        List<Lesson> result = lessonService.getLessonsByCourse(5);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(0, result.get(0).getOrderIndex());
        assertEquals(1, result.get(1).getOrderIndex());
        assertEquals(2, result.get(2).getOrderIndex());
        verify(lessonRepository, times(1)).findByCourseIdOrderByOrderIndex(5);
    }

    @Test
    @DisplayName("Should return empty list when no lessons found")
    void testGetLessonsByCourseEmpty() {
        // Arrange
        when(lessonRepository.findByCourseIdOrderByOrderIndex(999)).thenReturn(Arrays.asList());

        // Act
        List<Lesson> result = lessonService.getLessonsByCourse(999);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getLessonById Tests ====================

    @Test
    @DisplayName("Should get lesson by ID successfully")
    void testGetLessonById() {
        // Arrange
        testLesson.setIsPreview(true); // Preview lesson - no enrollment check
        when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));

        // Act
        Lesson result = lessonService.getLessonById(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLessonId());
        assertEquals("Java Basics", result.getTitle());
        verify(lessonRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw exception when lesson not found")
    void testGetLessonByIdNotFound() {
        // Arrange
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.getLessonById(999, 1);
        });

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
        verify(lessonRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should return preview lesson without enrollment check")
    void testGetPreviewLessonBypass() {
        // Arrange
        testLesson.setIsPreview(true);
        when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));

        // Act
        Lesson result = lessonService.getLessonById(1, 1);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsPreview());
    }

    // ==================== updateLesson Tests ====================

    @Test
    @DisplayName("Should update lesson successfully")
    void testUpdateLessonSuccess() {
        // Arrange
        Lesson existingLesson = new Lesson(1, 5, "Old Title", "VIDEO", "url", 20, 0, "Old Desc", false);
        Lesson updatedData = new Lesson(null, null, "New Title", "ARTICLE", "newurl", 25, null, "New Desc", true);

        when(lessonRepository.findById(1)).thenReturn(Optional.of(existingLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(existingLesson);

        // Act
        Lesson result = lessonService.updateLesson(1, updatedData);

        // Assert
        assertNotNull(result);
        verify(lessonRepository, times(1)).findById(1);
        verify(lessonRepository, times(1)).save(existingLesson);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent lesson")
    void testUpdateLessonNotFound() {
        // Arrange
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.updateLesson(999, testLesson);
        });

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
    }

    // ==================== deleteLesson Tests ====================

    @Test
    @DisplayName("Should delete lesson and associated resources")
    void testDeleteLessonSuccess() {
        // Arrange
        Resource resource1 = new Resource(1, 1, "Resource 1", "url1", "PDF", 100L);
        Resource resource2 = new Resource(2, 1, "Resource 2", "url2", "CODE", 200L);
        List<Resource> resources = Arrays.asList(resource1, resource2);

        when(resourceRepository.findByLessonId(1)).thenReturn(resources);
        doNothing().when(resourceRepository).deleteAll(resources);
        doNothing().when(lessonRepository).deleteById(1);

        // Act
        lessonService.deleteLesson(1);

        // Assert
        verify(resourceRepository, times(1)).findByLessonId(1);
        verify(resourceRepository, times(1)).deleteAll(resources);
        verify(lessonRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should delete lesson without resources")
    void testDeleteLessonNoResources() {
        // Arrange
        when(resourceRepository.findByLessonId(1)).thenReturn(Arrays.asList());
        doNothing().when(lessonRepository).deleteById(1);

        // Act
        lessonService.deleteLesson(1);

        // Assert
        verify(lessonRepository, times(1)).deleteById(1);
    }

    // ==================== reorderLessons Tests ====================

    @Test
    @DisplayName("Should reorder lessons successfully")
    void testReorderLessonsSuccess() {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Lesson 1", "VIDEO", "url1", 20, 0, "Desc1", false);
        Lesson lesson2 = new Lesson(2, 5, "Lesson 2", "VIDEO", "url2", 20, 1, "Desc2", false);
        Lesson lesson3 = new Lesson(3, 5, "Lesson 3", "VIDEO", "url3", 20, 2, "Desc3", false);

        List<Integer> newOrder = Arrays.asList(3, 1, 2);

        when(lessonRepository.findById(3)).thenReturn(Optional.of(lesson3));
        when(lessonRepository.findById(1)).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findById(2)).thenReturn(Optional.of(lesson2));

        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        lessonService.reorderLessons(5, newOrder);

        // Assert
        verify(lessonRepository, times(3)).findById(anyInt());
        verify(lessonRepository, times(3)).save(any(Lesson.class));

        assertEquals(0, lesson3.getOrderIndex());
        assertEquals(1, lesson1.getOrderIndex());
        assertEquals(2, lesson2.getOrderIndex());
    }

    @Test
    @DisplayName("Should throw exception when reordering with non-existent lesson")
    void testReorderLessonsNotFound() {
        // Arrange
        List<Integer> newOrder = Arrays.asList(1, 999, 3);
        when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.reorderLessons(5, newOrder);
        });

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
    }

    // ==================== addResource Tests ====================

    @Test
    @DisplayName("Should add resource to lesson successfully")
    void testAddResourceSuccess() {
        // Arrange
        when(lessonRepository.findById(1)).thenReturn(Optional.of(testLesson));
        when(resourceRepository.save(any(Resource.class))).thenReturn(testResource);

        // Act
        Resource result = lessonService.addResource(1, testResource);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getResourceId());
        assertEquals(1, result.getLessonId());
        assertEquals("Java Cheat Sheet", result.getName());
        verify(lessonRepository, times(1)).findById(1);
        verify(resourceRepository, times(1)).save(testResource);
    }

    @Test
    @DisplayName("Should throw exception when adding resource to non-existent lesson")
    void testAddResourceLessonNotFound() {
        // Arrange
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.addResource(999, testResource);
        });

        assertEquals("Lesson not found with ID: 999", exception.getMessage());
    }

    // ==================== removeResource Tests ====================

    @Test
    @DisplayName("Should remove resource successfully")
    void testRemoveResourceSuccess() {
        // Arrange
        doNothing().when(resourceRepository).deleteById(1);

        // Act
        lessonService.removeResource(1);

        // Assert
        verify(resourceRepository, times(1)).deleteById(1);
    }

    // ==================== getPreviewLessons Tests ====================

    @Test
    @DisplayName("Should get preview lessons only")
    void testGetPreviewLessonsSuccess() {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Preview 1", "VIDEO", "url1", 20, 0, "Desc1", true);
        Lesson lesson2 = new Lesson(2, 5, "Paid 1", "VIDEO", "url2", 20, 1, "Desc2", false);
        Lesson lesson3 = new Lesson(3, 5, "Preview 2", "VIDEO", "url3", 20, 2, "Desc3", true);

        List<Lesson> allLessons = Arrays.asList(lesson1, lesson2, lesson3);

        when(lessonRepository.findByCourseIdOrderByOrderIndex(5)).thenReturn(allLessons);

        // Act
        List<Lesson> result = lessonService.getPreviewLessons(5);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Lesson::getIsPreview));
        assertEquals("Preview 1", result.get(0).getTitle());
        assertEquals("Preview 2", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should return empty list when no preview lessons found")
    void testGetPreviewLessonsEmpty() {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Paid 1", "VIDEO", "url1", 20, 0, "Desc1", false);
        Lesson lesson2 = new Lesson(2, 5, "Paid 2", "VIDEO", "url2", 20, 1, "Desc2", false);

        List<Lesson> allLessons = Arrays.asList(lesson1, lesson2);

        when(lessonRepository.findByCourseIdOrderByOrderIndex(5)).thenReturn(allLessons);

        // Act
        List<Lesson> result = lessonService.getPreviewLessons(5);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

