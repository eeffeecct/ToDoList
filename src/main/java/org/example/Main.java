package org.example;


import org.example.dao.TaskDao;
import org.example.model.Task;

import java.sql.SQLException;
import java.util.List;


public class Main {
    public static void main(String[] args) throws SQLException {

        TaskDao taskDao = new TaskDao();
//        List<Task> tasks = taskDao.findAll();
//
//        tasks.forEach(task -> System.out.println(
//                "Задача: " + task.getTitle() +
//                        " (Выполнена: " + task.getIsCompleted() + ")"
//        ));

//        Task task = new Task();
//        task.setTitle("Погулять");
//        task.setDescription("Проверка работы");
//        task.setIsCompleted(false);
//        Task savedTasks = taskDao.save(task);
//        System.out.println(savedTasks.getId());

//        Task taskToUpdate = new Task();
//        taskToUpdate.setId(3);
//        taskToUpdate.setTitle("Покушать");
//        taskToUpdate.setDescription("Новое описание");
//        taskToUpdate.setIsCompleted(true);
//
//        taskDao.update(taskToUpdate);
//        System.out.println("Задача обновлена");

        Task newTask = new Task();
        newTask.setTitle("Важная задача");
        newTask.setDescription("Срочно выполнить");

        Task savedTask = taskDao.saveWithTransaction(newTask);
        System.out.println("Сохранено с ID: " + savedTask.getId());
    }
}