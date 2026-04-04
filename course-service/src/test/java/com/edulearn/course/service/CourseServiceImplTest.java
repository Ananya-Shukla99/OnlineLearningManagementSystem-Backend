package com.edulearn.course.service;

import com.edulearn.course.entity.Course;
import com.edulearn.course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CourseServiceImplTest {

    @Autowired
    private CourseService courseService;

    @MockBean
    private CourseRepository courseRepository;

    private Course testCourse;

    @BeforeEach
    public void setup() {
        testCourse = new Course();
        testCourse.setCourseId(1);
        testCourse.setTitle("Java Programming");
        testCourse.setDescription("Learn Java");
        testCourse.setCategory("Programming");
        testCourse.setLevel("Beginner");
        testCourse.setPrice(49.99);
        testCourse.setInstructorId(1);
        testCourse.setTotalDuration(120);
        testCourse.setIsPublished(true);
        testCourse.setLanguage("English");
        testCourse.setCreatedAt(LocalDate.now());
    }

    /**
     * Test 1: Create course successfully
     */
    @Test
    public void testCreateCourse_Success() {
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        Course result = courseService.createCourse(testCourse);

        assertNotNull(result);
        assertEquals("Java Programming", result.getTitle());
        assertEquals(1, result.getInstructorId());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    /**
     * Test 2: Create course without title - should throw exception
     */
    @Test
    public void testCreateCourse_NoTitle_ThrowsException() {
        Course invalidCourse = new Course();
        invalidCourse.setInstructorId(1);
        invalidCourse.setPrice(49.99);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.createCourse(invalidCourse);
        });

        assertEquals("Course title is required", exception.getMessage());
    }

    /**
     * Test 3: Create course without instructor ID - should throw exception
     */
    @Test
    public void testCreateCourse_NoInstructor_ThrowsException() {
        Course invalidCourse = new Course();
        invalidCourse.setTitle("Java");
        invalidCourse.setPrice(49.99);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.createCourse(invalidCourse);
        });

        assertEquals("Instructor ID is required", exception.getMessage());
    }

    /**
     * Test 4: Get all courses (published only)
     */
    @Test
    public void testGetAllCourses() {
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByIsPublished(true)).thenReturn(courses);

        List<Course> result = courseService.getAllCourses();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsPublished());
        verify(courseRepository, times(1)).findByIsPublished(true);
    }

    /**
     * Test 5: Get course by ID
     */
    @Test
    public void testGetCourseById() {
        when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));

        Optional<Course> result = courseService.getCourseById(1);

        assertTrue(result.isPresent());
        assertEquals("Java Programming", result.get().getTitle());
    }

    /**
     * Test 6: Get course by ID - Not found
     */
    @Test
    public void testGetCourseById_NotFound() {
        when(courseRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Course> result = courseService.getCourseById(999);

        assertFalse(result.isPresent());
    }

    /**
     * Test 7: Get courses by category
     */
    @Test
    public void testGetCoursesByCategory() {
        when(courseRepository.findByCategory("Programming")).thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.getCoursesByCategory("Programming");

        assertEquals(1, result.size());
        assertEquals("Programming", result.get(0).getCategory());
    }

    /**
     * Test 8: Get courses by instructor
     */
    @Test
    public void testGetCoursesByInstructor() {
        when(courseRepository.findByInstructorId(1)).thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.getCoursesByInstructor(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getInstructorId());
    }

    /**
     * Test 9: Search courses by keyword
     */
    @Test
    public void testSearchCourses() {
        when(courseRepository.searchByKeyword("Java")).thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.searchCourses("Java");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains("Java"));
    }

    /**
     * Test 10: Update course successfully
     */
    @Test
    public void testUpdateCourse_Success() {
        when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        Course updatedCourse = new Course();
        updatedCourse.setTitle("Advanced Java Programming");

        Course result = courseService.updateCourse(1, updatedCourse);

        assertNotNull(result);
        assertEquals("Advanced Java Programming", result.getTitle());
    }

    /**
     * Test 11: Update course - Not found
     */
    @Test
    public void testUpdateCourse_NotFound() {
        when(courseRepository.findById(999)).thenReturn(Optional.empty());

        Course updateData = new Course();
        updateData.setTitle("Updated Title");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.updateCourse(999, updateData);
        });

        assertEquals("Course not found with ID: 999", exception.getMessage());
    }

    /**
     * Test 12: Publish course
     */
    @Test
    public void testPublishCourse() {
        testCourse.setIsPublished(false);
        when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        courseService.publishCourse(1);

        verify(courseRepository, times(1)).save(any(Course.class));
        assertTrue(testCourse.getIsPublished());
    }

    /**
     * Test 13: Delete course
     */
    @Test
    public void testDeleteCourse() {
        when(courseRepository.existsById(1)).thenReturn(true);

        courseService.deleteCourse(1);

        verify(courseRepository, times(1)).deleteById(1);
    }

    /**
     * Test 14: Delete course - Not found
     */
    @Test
    public void testDeleteCourse_NotFound() {
        when(courseRepository.existsById(999)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.deleteCourse(999);
        });

        assertEquals("Course not found with ID: 999", exception.getMessage());
    }

    /**
     * Test 15: Get featured courses
     */
    @Test
    public void testGetFeaturedCourses() {
        when(courseRepository.getFeaturedCourses()).thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.getFeaturedCourses();

        assertEquals(1, result.size());
    }

    /**
     * Test 16: Get all courses including unpublished (admin)
     */
    @Test
    public void testGetAllCoursesIncludingUnpublished() {
        when(courseRepository.findAll()).thenReturn(Arrays.asList(testCourse));

        List<Course> result = courseService.getAllCoursesIncludingUnpublished();

        assertEquals(1, result.size());
        verify(courseRepository, times(1)).findAll();
    }
}
