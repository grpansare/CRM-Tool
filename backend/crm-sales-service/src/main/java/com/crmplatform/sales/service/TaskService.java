package com.crmplatform.sales.service;

import com.crmplatform.sales.dto.CreateTaskRequest;
import com.crmplatform.sales.dto.TaskResponse;
import com.crmplatform.sales.dto.UpdateTaskRequest;
import com.crmplatform.sales.entity.Task;
import com.crmplatform.sales.entity.TaskStatus;
import com.crmplatform.sales.entity.TaskPriority;
import com.crmplatform.sales.entity.TaskType;
import com.crmplatform.sales.repository.TaskRepository;
import com.crmplatform.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public Page<TaskResponse> getAllTasks(int page, int size, String searchTerm, TaskStatus status, TaskPriority priority, TaskType type, Long assignedToUserId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Task> tasks;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            if (assignedToUserId != null) {
                tasks = taskRepository.searchUserTasks(tenantId, assignedToUserId, searchTerm.trim(), pageable);
            } else {
                tasks = taskRepository.searchTasks(tenantId, searchTerm.trim(), pageable);
            }
        } else if (status != null) {
            if (assignedToUserId != null) {
                tasks = taskRepository.findByTenantIdAndAssignedToUserIdAndStatus(tenantId, assignedToUserId, status, pageable);
            } else {
                tasks = taskRepository.findByTenantIdAndStatus(tenantId, status, pageable);
            }
        } else if (priority != null) {
            if (assignedToUserId != null) {
                tasks = taskRepository.findByTenantIdAndAssignedToUserIdAndPriority(tenantId, assignedToUserId, priority, pageable);
            } else {
                tasks = taskRepository.findByTenantIdAndPriority(tenantId, priority, pageable);
            }
        } else if (type != null) {
            tasks = taskRepository.findByTenantIdAndType(tenantId, type, pageable);
        } else if (assignedToUserId != null) {
            tasks = taskRepository.findByTenantIdAndAssignedToUserId(tenantId, assignedToUserId, pageable);
        } else {
            tasks = taskRepository.findByTenantId(tenantId, pageable);
        }
        
        return tasks.map(this::convertToTaskResponse);
    }
    
    public Page<TaskResponse> getMyTasks(int page, int size, String searchTerm, TaskStatus status, TaskPriority priority) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        
        return getAllTasks(page, size, searchTerm, status, priority, null, userId);
    }
    
    public TaskResponse getTaskById(Long taskId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Task task = taskRepository.findByTaskIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        
        return convertToTaskResponse(task);
    }
    
    public TaskResponse createTask(CreateTaskRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long currentUserId = UserContext.getCurrentUserId();
        
        Task task = new Task();
        task.setTenantId(tenantId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setType(request.getType());
        task.setAssignedToUserId(request.getAssignedToUserId());
        task.setCreatedByUserId(currentUserId);
        task.setDueDate(request.getDueDate());
        task.setStatus(TaskStatus.PENDING);
        
        // Set related entity information
        task.setRelatedContactId(request.getRelatedContactId());
        task.setRelatedAccountId(request.getRelatedAccountId());
        task.setRelatedDealId(request.getRelatedDealId());
        task.setRelatedLeadId(request.getRelatedLeadId());
        task.setRelatedEntityType(request.getRelatedEntityType());
        
        Task savedTask = taskRepository.save(task);
        log.info("Created new task with ID: {} for tenant: {}", savedTask.getTaskId(), tenantId);
        
        return convertToTaskResponse(savedTask);
    }
    
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Task task = taskRepository.findByTaskIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        
        // Update fields if provided
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            // Set completion time if marking as completed
            if (request.getStatus() == TaskStatus.COMPLETED && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() != TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getType() != null) {
            task.setType(request.getType());
        }
        if (request.getAssignedToUserId() != null) {
            task.setAssignedToUserId(request.getAssignedToUserId());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        
        // Update related entity information
        if (request.getRelatedContactId() != null) {
            task.setRelatedContactId(request.getRelatedContactId());
        }
        if (request.getRelatedAccountId() != null) {
            task.setRelatedAccountId(request.getRelatedAccountId());
        }
        if (request.getRelatedDealId() != null) {
            task.setRelatedDealId(request.getRelatedDealId());
        }
        if (request.getRelatedLeadId() != null) {
            task.setRelatedLeadId(request.getRelatedLeadId());
        }
        if (request.getRelatedEntityType() != null) {
            task.setRelatedEntityType(request.getRelatedEntityType());
        }
        
        Task updatedTask = taskRepository.save(task);
        log.info("Updated task with ID: {} for tenant: {}", taskId, tenantId);
        
        return convertToTaskResponse(updatedTask);
    }
    
    public void deleteTask(Long taskId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Task task = taskRepository.findByTaskIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        
        taskRepository.delete(task);
        log.info("Deleted task with ID: {} for tenant: {}", taskId, tenantId);
    }
    
    public TaskResponse completeTask(Long taskId) {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.COMPLETED);
        return updateTask(taskId, request);
    }
    
    public List<TaskResponse> getTasksDueToday() {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Task> tasks = taskRepository.findTasksDueOnDate(tenantId, LocalDateTime.now());
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }
    
    public List<TaskResponse> getMyTasksDueToday() {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        List<Task> tasks = taskRepository.findUserTasksDueOnDate(tenantId, userId, LocalDateTime.now());
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }
    
    public List<TaskResponse> getOverdueTasks() {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Task> tasks = taskRepository.findOverdueTasks(tenantId, LocalDateTime.now());
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }
    
    public List<TaskResponse> getMyOverdueTasks() {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        List<Task> tasks = taskRepository.findUserOverdueTasks(tenantId, userId, LocalDateTime.now());
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }
    
    public List<TaskResponse> getTasksForEntity(String entityType, Long entityId) {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Task> tasks;
        
        switch (entityType.toUpperCase()) {
            case "CONTACT":
                tasks = taskRepository.findByTenantIdAndRelatedContactId(tenantId, entityId);
                break;
            case "ACCOUNT":
                tasks = taskRepository.findByTenantIdAndRelatedAccountId(tenantId, entityId);
                break;
            case "DEAL":
                tasks = taskRepository.findByTenantIdAndRelatedDealId(tenantId, entityId);
                break;
            case "LEAD":
                tasks = taskRepository.findByTenantIdAndRelatedLeadId(tenantId, entityId);
                break;
            default:
                throw new IllegalArgumentException("Invalid entity type: " + entityType);
        }
        
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }
    
    private TaskResponse convertToTaskResponse(Task task) {
        TaskResponse response = new TaskResponse(task);
        
        // TODO: Fetch and set user names from auth service
        // response.setAssignedToUserName(getUserName(task.getAssignedToUserId()));
        // response.setCreatedByUserName(getUserName(task.getCreatedByUserId()));
        
        // TODO: Fetch and set related entity names from respective services
        // if (task.getRelatedContactId() != null) {
        //     response.setRelatedContactName(getContactName(task.getRelatedContactId()));
        // }
        // if (task.getRelatedAccountId() != null) {
        //     response.setRelatedAccountName(getAccountName(task.getRelatedAccountId()));
        // }
        // if (task.getRelatedDealId() != null) {
        //     response.setRelatedDealTitle(getDealTitle(task.getRelatedDealId()));
        // }
        // if (task.getRelatedLeadId() != null) {
        //     response.setRelatedLeadName(getLeadName(task.getRelatedLeadId()));
        // }
        
        return response;
    }
}
