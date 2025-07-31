package org.example.dto;

// DTO - контейнер для передачи данных между слоями приложения
// Не содержит логику, только данные

// dto DataTransferObject - содержит объекты с перечнем полей
// то же, что и класс, но автоматически добавляет геттеры, toString и тд.
public record TaskFilter (int limit,
                         int offset,
                          String description,
                          Boolean isCompleted) {
}
