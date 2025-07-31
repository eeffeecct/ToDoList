package org.example;

import org.example.dao.TaskDao;
import org.example.dto.TaskFilter;
import org.example.model.Task;
import org.example.util.ConnectionManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws SQLException {
        TaskDao dao = TaskDao.getInstance(); // обращение к единственному экземпляру

        var taskFilter = new TaskFilter(4, 0, null, false);
        var tasks = TaskDao.getInstance().findAll(taskFilter);
        System.out.println(tasks);


//        System.out.println("=== INSERT ==="); // вставляем новую задачу
//        Task newTask = new Task();
//        newTask.setTitle("Проверка save");
//        newTask.setDescription("Опишем сохранение в main");
//        newTask.setIsCompleted(false);
//        Task saved = dao.save(newTask); // сохраняем в saved задачу которую только что создали
//        System.out.printf("Сохранена задача: %s (id=%d, createdAt=%s)%n",
//                saved.getTitle(), saved.getId(), saved.getCreatedAt());
//
//        System.out.println("\n=== FIND BY ID ===");
//        // Optional - контейнер, который может содержать объекты или быть пустым
//        Optional<Task> maybe = dao.findById(saved.getId()); // saved - пермеменная, которую до этого сохранили
//        maybe.ifPresentOrElse(  // если что-то есть, то выполняется первый блок, иначе второй.
//            t -> System.out.printf("Найдена: %s (id=%d, done=%s)%n",
//                    t.getTitle(), t.getId(), t.getIsCompleted()),
//            () -> System.out.println("Не найдено!")
//        );
//
//        System.out.println("\n=== FIND ALL ===");
//        List<Task> all = dao.findAll(); // Список для всех задач типа Task
//        all.forEach(System.out::println);
//
//        System.out.println("\n=== DELETE ===");
//        boolean deleted = dao.delete(saved.getId());
//        System.out.println("Удаление id=" + saved.getId() + ": " + (deleted ? "ok" : "failed"));
//
//        System.out.println("\n=== TRANSACTIONAL SAVE ==="); // то же самое, что и save просто тут работает транзакция (Атомарность)
//        Task txnTask = new Task();
//        txnTask.setTitle("В транзакции");
//        txnTask.setDescription("Проверим saveWithTransaction");
//        Task txnSaved = dao.saveWithTransaction(txnTask);
//        System.out.printf("Сохранено в транзакции: id=%d%n", txnSaved.getId());
//
//        System.out.println("\n=== Готово. ===");
    }

    private static void checkMetaData() throws SQLException {
      try (var connection = ConnectionManager.get()) {
          var metaData = connection.getMetaData();
          var catalogs = metaData.getCatalogs();
          while (catalogs.next()) {
              var catalog = catalogs.getString(1);
              var schemas = metaData.getSchemas();
              while (schemas.next()) {
                  var schema = schemas.getString("title");
                  var tables = metaData.getTables(catalog, schema, "%", new String[] {"TABLE"});
                  if (schema.equals("public")) {
                      while (tables.next()) {
                          System.out.println(tables.getString("description"));
                      }
                  }
              }
          }
      } catch (InterruptedException e) {
          throw new RuntimeException(e);
      }
    }

}