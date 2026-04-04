package com.edulearn.course.controller;

import com.edulearn.course.entity.Course;
import com.edulearn.course.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course testCourse;

    @BeforeEach
    public void setup() {
        testCourse = new Course();
        testCourse.setCourseId(1);
        testCourse.setTitle("Java Programming");
        testCourse.setDescription("Learn Java from basics to advanced");
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
     * Test 1: Get all courses (PUBLIC endpoint)
     */
    @Test
    public void testGetAllCourses() throws Exception {
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Courses retrieved successfully"))
                .andExpect(jsonPath("$.data[0].title").value("Java Programming"));
    }

    /**
     * Test 2: Get course by ID
     */
    @Test
    public void testGetCourseById() throws Exception {
        when(courseService.getCourseById(1)).thenReturn(Optional.of(testCourse));

        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Programming"));
    }

    /**
     * Test 3: Get course by ID - Not Found
     */
    @Test
    public void testGetCourseByIdNotFound() throws Exception {
        when(courseService.getCourseById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/courses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Course not found"));
    }

    /**
     * Test 4: Search courses by keyword
     */
    @Test
    public void testSearchCourses() throws Exception {
        when(courseService.searchCourses("Java")).thenReturn(Arrays.asList(testCourse));

        mockMvc.perform(get("/courses/search").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Java Programming"));
    }

    /**
     * Test 5: Get courses by category
     */
    @Test
    public void testGetCoursesByCategory() throws Exception {
        when(courseService.getCoursesByCategory("Programming")).thenReturn(Arrays.asList(testCourse));

        mockMvc.perform(get("/courses/category/Programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("Programming"));
    }

    /**
     * Test 6: Get featured courses
     */
    @Test
    public void testGetFeaturedCourses() throws Exception {
        when(courseService.getFeaturedCourses()).thenReturn(Arrays.asList(testCourse));

        mockMvc.perform(get("/courses/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Test 7: Get courses by instructor
     */
    @Test
    public void testGetCoursesByInstructor() throws Exception {
        when(courseService.getCoursesByInstructor(1)).thenReturn(Arrays.asList(testCourse));

        mockMvc.perform(get("/courses/instructor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Test 8: Create course (requires INSTRUCTOR role)
     */
    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testCreateCourse() throws Exception {
        when(courseService.createCourse(any(Course.class))).thenReturn(testCourse);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCourse)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course created successfully"));
    }

    /**
     * Test 9: Update course
     */
    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testUpdateCourse() throws Exception {
        when(courseService.updateCourse(anyInt(), any(Course.class))).thenReturn(testCourse);

        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course updated successfully"));
    }

    /**
     * Test 10: Publish course
     */
    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testPublishCourse() throws Exception {
        mockMvc.perform(put("/courses/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course published successfully"));
    }

    /**
     * Test 11: Delete course
     */
    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    public void testDeleteCourse() throws Exception {
        mockMvc.perform(delete("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course deleted successfully"));
    }
}
