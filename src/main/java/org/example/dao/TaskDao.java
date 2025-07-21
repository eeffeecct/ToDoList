package org.example.dao;

import org.example.model.Task;
import org.example.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDao {

    // Получение задачи по ID
    public Optional<Task> findById(Integer id) throws SQLException {
        String sql = """
                SELECT * FROM tasks WHERE id = ?\s
               \s""";

        try(var connection = ConnectionManager.open();
        var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    Task task = new Task();
                    task.setId(result.getInt("id"));
                    task.setTitle(result.getString("title"));
                    task.setDescription(result.getString("description"));
                    task.setIsCompleted(result.getBoolean("is_completed"));

                    return Optional.of(task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске задачи ", e);
        }

        return Optional.empty();
    }

    // CRUD - Create
    public Task save(Task task) {
        String sql = "INSERT INTO tasks (title, description, is_completed) " +
                "VALUES (?, ?, ?) " +
                "RETURNING id, created_at";

        try (var connection = ConnectionManager.open();
             var statement =  connection.prepareStatement(sql);) {

            // Установка параметров запроса
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setBoolean(3, task.getIsCompleted() != null && task.getIsCompleted());


            var resultSet = statement.executeQuery();   // Выполнение запроса и получение ResultSet с данными

            // Если запись добавлена - обновляем поля task
            if (resultSet.next()) {
                task.setId(resultSet.getInt("id"));
                task.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
            }

            return task;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // CRUD - Read
    public List<Task> findAll() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = """
            SELECT * FROM tasks;
        """;

        try (var connection = ConnectionManager.open();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Task task = new Task();
                task.setId(resultSet.getInt("id"));
                task.setTitle(resultSet.getString("title"));
                task.setDescription(resultSet.getString("description"));
                task.setIsCompleted(resultSet.getBoolean("is_completed"));
                task.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    // CRUD - Update
    public void update(Task task) throws SQLException {
        String sql = """
                UPDATE tasks SET title = ?, description = ?, is_completed = ? WHERE id = ?
                """;
        try (var connection = ConnectionManager.open();
        var statement = connection.prepareStatement(sql)) {
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setBoolean(3, task.getIsCompleted());
            statement.setInt(4, task.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Задача с ID " + task.getId() + " не найдена");
            }
        }
    }

    // CRUD - Delete
    public boolean delete(Integer id) throws SQLException {
        String sql = """
                DELETE FROM tasks WHERE id = ?
                """;

        try(var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();

            return rowsDeleted > 0;
        }
    }

    // Атомарность
    public Task saveWithTransaction(Task task) {
        Connection connection = null;
        try {
            connection = ConnectionManager.open();  // установка соединения
            ConnectionManager.beginTransaction(connection); // начинаем транзакцию

            String sql = """
                        INSERT INTO tasks (title, description) VALUES (?, ?) RETURNING id
                        """;  // выполняем SQL запрос
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, task.getTitle());
                statement.setString(2, task.getDescription());

                ResultSet rs = statement.executeQuery();
                if (rs.next()) {    // перемещение курсора
                    task.setId(rs.getInt("id"));    // из бд сохраняется id в task
                }
            }

            ConnectionManager.commitTransaction(connection);    // если все хорошо - коммитим
            return task;
        } catch (SQLException e) {
            ConnectionManager.rollbackTransaction(connection);
            throw new RuntimeException("Ошибка при сохранении задачи", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }

        }
    }
}
