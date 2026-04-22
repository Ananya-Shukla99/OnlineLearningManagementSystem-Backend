package com.edulearn.progress.controller;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Progress Controller Tests")
class ProgressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private ProgressController progressController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(progressController)
                .build();
    }

    @Test
    @DisplayName("GET /course - Success")
    void testGetProgress() throws Exception {

        when(progressService.getCourseProgress(101L, 5L)).thenReturn(75);

        mockMvc.perform(get("/api/v1/progress/course")
                        .param("studentId", "101")
                        .param("courseId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("75"));
    }

    @Test
    @DisplayName("GET /all/{studentId} - Success")
    void testGetStudentProgress() throws Exception {

        Progress p = new Progress();
        p.setStudentId(101L);

        when(progressService.getAllProgressByStudent(101L))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/progress/all/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(101L));
    }
}