package com.edulearn.course.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

import com.edulearn.course.entity.Course;
import com.edulearn.course.repository.CourseRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseServiceImpl Unit Tests")
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setCourseId(1);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setInstructorId(101);
        testCourse.setPrice(99.99);
        testCourse.setCategory("Programming");
        testCourse.setLevel("Beginner");
        testCourse.setTotalDuration(300);
        testCourse.setIsPublished(true);
        testCourse.setApprovalStatus("APPROVED");
        testCourse.setLanguage("English");
    }

    // ─── createCourse ─────────────────────────────────────────────────

    @Nested
    @DisplayName("createCourse()")
    class CreateCourse {

        @Test
        @DisplayName("Should create course with valid data")
        void testCreateCourseSuccess() {
            Course newCourse = new Course();
            newCourse.setTitle("New Course");
            newCourse.setInstructorId(101);
            newCourse.setPrice(49.99);

            when(courseRepository.save(any(Course.class))).thenReturn(newCourse);

            Course created = courseService.createCourse(newCourse);

            assertNotNull(created);
            assertEquals("New Course", created.getTitle());
            assertEquals("DRAFT", created.getApprovalStatus());
            assertFalse(created.getIsPublished());
            verify(courseRepository).save(newCourse);
        }

        @Test
        @DisplayName("Should create free course with price 0")
        void testCreateFreeCourse() {
            Course freeCourse = new Course();
            freeCourse.setTitle("Free Course");
            freeCourse.setInstructorId(101);
            freeCourse.setPrice(0.0);

            when(courseRepository.save(any(Course.class))).thenReturn(freeCourse);

            Course created = courseService.createCourse(freeCourse);

            assertNotNull(created);
            assertEquals(0.0, created.getPrice());
        }

        @Test
        @DisplayName("Should throw when title is null")
        void testCreateCourseNoTitle() {
            Course noTitle = new Course();
            noTitle.setInstructorId(101);
            noTitle.setPrice(10.0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> courseService.createCourse(noTitle));
            assertEquals("Course title is required", ex.getMessage());
            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when title is empty")
        void testCreateCourseEmptyTitle() {
            Course emptyTitle = new Course();
            emptyTitle.setTitle("");
            emptyTitle.setInstructorId(101);
            emptyTitle.setPrice(10.0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> courseService.createCourse(emptyTitle));
            assertEquals("Course title is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when instructorId is null")
        void testCreateCourseNoInstructor() {
            Course noInstructor = new Course();
            noInstructor.setTitle("Valid Title");
            noInstructor.setPrice(10.0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> courseService.createCourse(noInstructor));
            assertEquals("Instructor ID is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when price is negative")
        void testCreateCourseNegativePrice() {
            Course negPrice = new Course();
            negPrice.setTitle("Course");
            negPrice.setInstructorId(101);
            negPrice.setPrice(-10.0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> courseService.createCourse(negPrice));
            assertEquals("Course price must be non-negative", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when price is null")
        void testCreateCourseNullPrice() {
            Course nullPrice = new Course();
            nullPrice.setTitle("Course");
            nullPrice.setInstructorId(101);
            nullPrice.setPrice(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> courseService.createCourse(nullPrice));
            assertEquals("Course price must be non-negative", ex.getMessage());
        }
    }

    // ─── getAllCourses ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllCourses()")
    class GetAllCourses {

        @Test
        @DisplayName("Should return only published courses")
        void testGetAllCourses() {
            when(courseRepository.findByIsPublished(true)).thenReturn(List.of(testCourse));

            List<Course> result = courseService.getAllCourses();

            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsPublished());
            verify(courseRepository).findByIsPublished(true);
        }

        @Test
        @DisplayName("Should return empty when no published courses")
        void testGetAllCoursesEmpty() {
            when(courseRepository.findByIsPublished(true)).thenReturn(Collections.emptyList());

            assertTrue(courseService.getAllCourses().isEmpty());
        }
    }

    // ─── getCourseById ────────────────────────────────────────────────

    @Nested
    @DisplayName("getCourseById()")
    class GetCourseById {

        @Test
        @DisplayName("Should return course when found")
        void testGetCourseById() {
            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));

            Optional<Course> result = courseService.getCourseById(1);

            assertTrue(result.isPresent());
            assertEquals(1, result.get().getCourseId());
            assertEquals("Test Course", result.get().getTitle());
        }

        @Test
        @DisplayName("Should return empty when not found")
        void testGetCourseByIdNotFound() {
            when(courseRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Course> result = courseService.getCourseById(999);

            assertTrue(result.isEmpty());
        }
    }

    // ─── getCoursesByCategory ─────────────────────────────────────────

    @Nested
    @DisplayName("getCoursesByCategory()")
    class GetCoursesByCategory {

        @Test
        @DisplayName("Should return only published courses in category")
        void testGetByCategoryFiltersPublished() {
            Course unpublished = new Course();
            unpublished.setCourseId(2);
            unpublished.setCategory("Programming");
            unpublished.setIsPublished(false);

            when(courseRepository.findByCategory("Programming"))
                    .thenReturn(List.of(testCourse, unpublished));

            List<Course> result = courseService.getCoursesByCategory("Programming");

            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsPublished());
        }

        @Test
        @DisplayName("Should return empty for unknown category")
        void testGetByCategoryEmpty() {
            when(courseRepository.findByCategory("Unknown")).thenReturn(Collections.emptyList());

            assertTrue(courseService.getCoursesByCategory("Unknown").isEmpty());
        }

        @Test
        @DisplayName("Should return empty when all in category are unpublished")
        void testGetByCategoryAllUnpublished() {
            Course unpub = new Course();
            unpub.setIsPublished(false);

            when(courseRepository.findByCategory("Math")).thenReturn(List.of(unpub));

            assertTrue(courseService.getCoursesByCategory("Math").isEmpty());
        }
    }

    // ─── getCoursesByInstructor ────────────────────────────────────────

    @Nested
    @DisplayName("getCoursesByInstructor()")
    class GetCoursesByInstructor {

        @Test
        @DisplayName("Should return all courses by instructor")
        void testGetByInstructor() {
            when(courseRepository.findByInstructorId(101)).thenReturn(List.of(testCourse));

            List<Course> result = courseService.getCoursesByInstructor(101);

            assertEquals(1, result.size());
            assertEquals(101, result.get(0).getInstructorId());
        }

        @Test
        @DisplayName("Should return empty for unknown instructor")
        void testGetByInstructorEmpty() {
            when(courseRepository.findByInstructorId(999)).thenReturn(Collections.emptyList());

            assertTrue(courseService.getCoursesByInstructor(999).isEmpty());
        }
    }

    // ─── searchCourses ────────────────────────────────────────────────

    @Nested
    @DisplayName("searchCourses()")
    class SearchCourses {

        @Test
        @DisplayName("Should return matching courses")
        void testSearchCourses() {
            when(courseRepository.searchByKeyword("Test")).thenReturn(List.of(testCourse));

            List<Course> result = courseService.searchCourses("Test");

            assertEquals(1, result.size());
            assertTrue(result.get(0).getTitle().contains("Test"));
            verify(courseRepository).searchByKeyword("Test");
        }

        @Test
        @DisplayName("Should return empty for no matches")
        void testSearchNoResults() {
            when(courseRepository.searchByKeyword("xyz")).thenReturn(Collections.emptyList());

            assertTrue(courseService.searchCourses("xyz").isEmpty());
        }
    }

    // ─── updateCourse ─────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCourse()")
    class UpdateCourse {

        @Test
        @DisplayName("Should update all provided fields")
        void testUpdateAllFields() {
            Course update = new Course();
            update.setTitle("Updated Title");
            update.setDescription("Updated Desc");
            update.setCategory("Design");
            update.setLevel("Advanced");
            update.setPrice(149.99);
            update.setThumbnailUrl("http://img.com/thumb.png");
            update.setTotalDuration(600);
            update.setLanguage("Spanish");

            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
            when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

            Course result = courseService.updateCourse(1, update);

            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated Desc", result.getDescription());
            assertEquals("Design", result.getCategory());
            assertEquals("Advanced", result.getLevel());
            assertEquals(149.99, result.getPrice());
            assertEquals("http://img.com/thumb.png", result.getThumbnailUrl());
            assertEquals(600, result.getTotalDuration());
            assertEquals("Spanish", result.getLanguage());
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("Should skip null fields during update")
        void testUpdatePartialFields() {
            Course partial = new Course();
            partial.setTitle("New Title Only");
            // all other fields null

            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
            when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

            Course result = courseService.updateCourse(1, partial);

            assertEquals("New Title Only", result.getTitle());
            // original description should remain
            assertEquals("Test Description", result.getDescription());
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("Should throw when course not found")
        void testUpdateNotFound() {
            when(courseRepository.findById(999)).thenReturn(Optional.empty());
            Course update = new Course();

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> courseService.updateCourse(999, update));
            assertTrue(ex.getMessage().contains("999"));
            verify(courseRepository, never()).save(any());
        }
    }

    // ─── publishCourse ────────────────────────────────────────────────

    @Nested
    @DisplayName("publishCourse()")
    class PublishCourse {

        @Test
        @DisplayName("Should set status to PENDING_APPROVAL")
        void testPublishCourse() {
            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));

            courseService.publishCourse(1);

            assertFalse(testCourse.getIsPublished());
            assertEquals("PENDING_APPROVAL", testCourse.getApprovalStatus());
            assertNull(testCourse.getRejectionReason());
            verify(courseRepository).save(testCourse);
        }

        @Test
        @DisplayName("Should throw when course not found")
        void testPublishNotFound() {
            when(courseRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> courseService.publishCourse(999));
            verify(courseRepository, never()).save(any());
        }
    }

    // ─── approveCourse ────────────────────────────────────────────────

    @Nested
    @DisplayName("approveCourse()")
    class ApproveCourse {

        @Test
        @DisplayName("Should approve and publish course")
        void testApproveCourse() {
            testCourse.setApprovalStatus("PENDING_APPROVAL");
            testCourse.setIsPublished(false);

            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));

            courseService.approveCourse(1);

            assertEquals("APPROVED", testCourse.getApprovalStatus());
            assertTrue(testCourse.getIsPublished());
            assertNull(testCourse.getRejectionReason());
            verify(courseRepository).save(testCourse);
        }

        @Test
        @DisplayName("Should clear rejection reason on approval")
        void testApproveClearsRejection() {
            testCourse.setRejectionReason("Previous rejection");
            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));

            courseService.approveCourse(1);

            assertNull(testCourse.getRejectionReason());
        }

        @Test
        @DisplayName("Should throw when course not found")
        void testApproveNotFound() {
            when(courseRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> courseService.approveCourse(999));
        }
    }

    // ─── rejectCourse ─────────────────────────────────────────────────

    @Nested
    @DisplayName("rejectCourse()")
    class RejectCourse {

        @Test
        @DisplayName("Should reject course with reason")
        void testRejectCourse() {
            testCourse.setApprovalStatus("PENDING_APPROVAL");

            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));

            courseService.rejectCourse(1, "Poor content quality");

            assertEquals("REJECTED", testCourse.getApprovalStatus());
            assertFalse(testCourse.getIsPublished());
            assertEquals("Poor content quality", testCourse.getRejectionReason());
            verify(courseRepository).save(testCourse);
        }

        @Test
        @DisplayName("Should throw when course not found")
        void testRejectNotFound() {
            when(courseRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> courseService.rejectCourse(999, "reason"));
            verify(courseRepository, never()).save(any());
        }
    }

    // ─── getPendingCourses ────────────────────────────────────────────

    @Nested
    @DisplayName("getPendingCourses()")
    class GetPendingCourses {

        @Test
        @DisplayName("Should return pending courses")
        void testGetPending() {
            Course pending = new Course();
            pending.setApprovalStatus("PENDING_APPROVAL");

            when(courseRepository.findByApprovalStatus("PENDING_APPROVAL"))
                    .thenReturn(List.of(pending));

            List<Course> result = courseService.getPendingCourses();

            assertEquals(1, result.size());
            assertEquals("PENDING_APPROVAL", result.get(0).getApprovalStatus());
        }

        @Test
        @DisplayName("Should return empty when no pending courses")
        void testGetPendingEmpty() {
            when(courseRepository.findByApprovalStatus("PENDING_APPROVAL"))
                    .thenReturn(Collections.emptyList());

            assertTrue(courseService.getPendingCourses().isEmpty());
        }
    }

    // ─── deleteCourse ─────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteCourse()")
    class DeleteCourse {

        @Test
        @DisplayName("Should delete existing course")
        void testDeleteCourse() {
            when(courseRepository.existsById(1)).thenReturn(true);

            courseService.deleteCourse(1);

            verify(courseRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw when course not found")
        void testDeleteNotFound() {
            when(courseRepository.existsById(999)).thenReturn(false);

            assertThrows(RuntimeException.class, () -> courseService.deleteCourse(999));
            verify(courseRepository, never()).deleteById(999);
        }
    }

    // ─── getFeaturedCourses ───────────────────────────────────────────

    @Nested
    @DisplayName("getFeaturedCourses()")
    class GetFeaturedCourses {

        @Test
        @DisplayName("Should return featured courses")
        void testGetFeatured() {
            when(courseRepository.getFeaturedCourses()).thenReturn(List.of(testCourse));

            List<Course> result = courseService.getFeaturedCourses();

            assertEquals(1, result.size());
            verify(courseRepository).getFeaturedCourses();
        }

        @Test
        @DisplayName("Should return empty when no featured courses")
        void testGetFeaturedEmpty() {
            when(courseRepository.getFeaturedCourses()).thenReturn(Collections.emptyList());

            assertTrue(courseService.getFeaturedCourses().isEmpty());
        }
    }

    // ─── getAllCoursesIncludingUnpublished ─────────────────────────────

    @Nested
    @DisplayName("getAllCoursesIncludingUnpublished()")
    class GetAllCoursesIncludingUnpublished {

        @Test
        @DisplayName("Should return all courses")
        void testGetAllIncludingUnpublished() {
            Course unpub = new Course();
            unpub.setCourseId(2);
            unpub.setIsPublished(false);

            when(courseRepository.findAll()).thenReturn(List.of(testCourse, unpub));

            List<Course> result = courseService.getAllCoursesIncludingUnpublished();

            assertEquals(2, result.size());
            verify(courseRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty when no courses")
        void testGetAllEmpty() {
            when(courseRepository.findAll()).thenReturn(Collections.emptyList());

            assertTrue(courseService.getAllCoursesIncludingUnpublished().isEmpty());
        }
    }
}
