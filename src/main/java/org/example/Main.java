package org.example;

import org.example.util.ConnectionManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

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