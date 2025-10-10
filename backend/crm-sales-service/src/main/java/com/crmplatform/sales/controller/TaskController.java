package com.crmplatform.sales.controller;

import com.crmplatform.sales.dto.CreateTaskRequest;
import com.crmplatform.sales.dto.TaskResponse;
import com.crmplatform.sales.dto.UpdateTaskRequest;
import com.crmplatform.sales.entity.TaskStatus;
import com.crmplatform.sales.entity.TaskPriority;
import com.crmplatform.sales.entity.TaskType;
import com.crmplatform.sales.service.TaskService;
import com.crmplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Slf4j
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType type,
            @RequestParam(required = false) Long assignedToUserId) {
        try {
            Page<TaskResponse> tasks = taskService.getAllTasks(page, size, searchTerm, status, priority, type, assignedToUserId);
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tasks: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority) {
        try {
            Page<TaskResponse> tasks = taskService.getMyTasks(page, size, searchTerm, status, priority);
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching my tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch my tasks: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long taskId) {
        try {
            TaskResponse task = taskService.getTaskById(taskId);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (RuntimeException e) {
            log.error("Task not found: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
        } catch (Exception e) {
            log.error("Error fetching task: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch task: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        try {
            TaskResponse task = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(task));
        } catch (Exception e) {
            log.error("Error creating task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create task: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        try {
            TaskResponse task = taskService.updateTask(taskId, request);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (RuntimeException e) {
            log.error("Task not found for update: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
        } catch (Exception e) {
            log.error("Error updating task: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update task: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Task not found for deletion: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
        } catch (Exception e) {
            log.error("Error deleting task: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete task: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(@PathVariable Long taskId) {
        try {
            TaskResponse task = taskService.completeTask(taskId);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (RuntimeException e) {
            log.error("Task not found for completion: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
        } catch (Exception e) {
            log.error("Error completing task: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }
    
    @GetMapping("/due-today")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksDueToday() {
        try {
            List<TaskResponse> tasks = taskService.getTasksDueToday();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching tasks due today", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tasks due today: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-tasks/due-today")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMyTasksDueToday() {
        try {
            List<TaskResponse> tasks = taskService.getMyTasksDueToday();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching my tasks due today", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch my tasks due today: " + e.getMessage()));
        }
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks() {
        try {
            List<TaskResponse> tasks = taskService.getOverdueTasks();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching overdue tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch overdue tasks: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-tasks/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMyOverdueTasks() {
        try {
            List<TaskResponse> tasks = taskService.getMyOverdueTasks();
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error fetching my overdue tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch my overdue tasks: " + e.getMessage()));
        }
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            List<TaskResponse> tasks = taskService.getTasksForEntity(entityType, entityId);
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (IllegalArgumentException e) {
            log.error("Invalid entity type: {}", entityType, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid entity type: " + entityType));
        } catch (Exception e) {
            log.error("Error fetching tasks for entity: {} {}", entityType, entityId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tasks for entity: " + e.getMessage()));
        }
    }
}
