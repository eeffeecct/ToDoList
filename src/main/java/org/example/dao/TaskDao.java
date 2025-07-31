package org.example.dao;

import org.example.dto.TaskFilter;
import org.example.exception.DaoException;
import org.example.model.Task;
import org.example.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;


// Класс для взаимодействия с базой данных

// must be singleton 
public class TaskDao {
    private static final TaskDao INSTANCE = new TaskDao();  // for Singleton
    // SQL requests
    private static final String FIND_BY_ID_SQL = """
            SELECT *
            FROM tasks
            WHERE id = ?
            """;
    private static final String SAVE_SQL = """
            INSERT INTO tasks (title, description, is_completed)
            VALUES (?, ?, ?)
            RETURNING id, created_at
            """;
    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM tasks
            """;
    private static final String UPDATE_SQL = """
            UPDATE tasks
            SET title = ?, description = ?, is_completed = ?
            WHERE id = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM tasks
            WHERE id = ?
            """;
    private static final String SAVE_WITH_TRANSACTION_SQL = """
            INSERT INTO tasks (title, description)
            VALUES (?, ?)
            RETURNING id
            """;

    // private constructor for Singleton
    private TaskDao() {}

    // for Singleton
    public static TaskDao getInstance() {
        return INSTANCE;
    }

    public Optional<Task> findById(Integer id) {
        try(var connection = ConnectionManager.get();
        var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, id);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapTask(result));
                }
            }
        } catch (SQLException | InterruptedException throwable) {
            throw new DaoException("Failed to find task by id: " + id, throwable);
        }

        return Optional.empty();
    }

    // Works like create and update
    public Task save(Task task) {
        try (var connection = ConnectionManager.get();
             var statement =  connection.prepareStatement(SAVE_SQL)) {
            boolean isCompleted = task.getIsCompleted() != null && task.getIsCompleted();
            // Установка параметров запроса
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setBoolean(3, isCompleted);

            var resultSet = statement.executeQuery();   // Выполнение запроса и получение ResultSet с данными
            // Если запись добавлена - обновляем поля task
            if (resultSet.next()) {
                task.setId(resultSet.getInt("id"));
                task.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
            }
            return task;
        } catch (SQLException | InterruptedException throwable) {
            throw new DaoException("Failed to save task: ", throwable);
        }
    }

    public List<Task> findAll(TaskFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();

        if (filter.description() != null) {
            whereSql.add("description LIKE ?");
            parameters.add(filter.description());
        }
        if (filter.isCompleted() != null) {
            whereSql.add("is_completed = ?");
            parameters.add(filter.isCompleted());
        }

        parameters.add(filter.limit());
        parameters.add(filter.offset());
        var where = whereSql.stream()
                .collect(joining(" AND ", " WHERE ", " LIMIT ? OFFSET ? "));

        
        var sql = FIND_ALL_SQL + where;

        try (var connection = ConnectionManager.get();
        var preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            System.out.println(preparedStatement);
            var resultSet = preparedStatement.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (resultSet.next()) {
                tasks.add(mapTask(resultSet));
            }
            return tasks;
        } catch (SQLException | InterruptedException throwable) {
            throw new DaoException(throwable);
        }
    }

    // такой метод используется для справочных таблиц
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        try (var connection = ConnectionManager.get();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                tasks.add(mapTask(resultSet));
            }
        } catch (SQLException | InterruptedException throwable) {
            throw new DaoException("Failed to read tasks: ", throwable);
        }
        return tasks;
    }

    public void update(Task task) {
        try (var connection = ConnectionManager.get();
        var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setBoolean(3, task.getIsCompleted());
            statement.setInt(4, task.getId());
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Задача с ID " + task.getId() + " не найдена");
            }
        } catch (SQLException | InterruptedException throwable) {
            throw new DaoException("Failed to update task: ", throwable);
        }
    }

    public boolean delete(Integer id) {
        try (var connection = ConnectionManager.get();
            var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException | InterruptedException throwable) {
            throw new DaoException("Failed to delete task: ", throwable);
        }
    }

    public Task saveWithTransaction(Task task) {
        Connection connection = null;
        try {
            connection = ConnectionManager.get();  // установка соединения
            ConnectionManager.beginTransaction(connection); // начинаем транзакцию

            try (PreparedStatement statement = connection.prepareStatement(SAVE_WITH_TRANSACTION_SQL)) {
                statement.setString(1, task.getTitle());
                statement.setString(2, task.getDescription());

                ResultSet rs = statement.executeQuery();
                if (rs.next()) {    // перемещение курсора
                    task.setId(rs.getInt("id"));    // из бд сохраняется id в task
                }
            }

            ConnectionManager.commitTransaction(connection);    // если все хорошо - коммит
            return task;
        } catch (Exception e) {  // Ловим все исключения
            if (connection != null) {
                ConnectionManager.rollbackTransaction(connection);  // ← ROLLBACK
            }
            throw new DaoException("Transaction error ", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();  // Закрываем соединение
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
        Task task = new Task();
        task.setId(resultSet.getInt("id"));
        task.setTitle(resultSet.getString("title"));
        task.setDescription(resultSet.getString("description"));
        task.setIsCompleted(resultSet.getBoolean("is_completed"));
        task.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return task;
    }
}
