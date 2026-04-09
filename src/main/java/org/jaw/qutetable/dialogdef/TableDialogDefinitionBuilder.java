/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.dialogdef;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class TableDialogDefinitionBuilder<T> {

  public static final int DEFAULT_MAX_NUMBER_OF_COLUMNS = 10;

  final String dialogMenuPath;
  final Class<T> dialogObjectType;
  final int addSequence;
  String dialogResourcePath;
  String dialogResourceDataPath;
  Supplier<Stream<T>> dataSource;
  final List<TableDialogColumnDefinition<T>> columns = new ArrayList<>();
  final List<TableDialogColumnDefinition<T>> details = new ArrayList<>();
  BiFunction<T, ObjectMapper, String> jsonDetailFunction;
  Predicate<Field> columnFieldFilter = _ -> true;
  Predicate<Field> detailsFieldFilter = _ -> true;
  Predicate<Field> flatFieldDisplayPredicate = _ -> false;

  public TableDialogDefinitionBuilder(String dialogMenuPath, Class<T> dialogObjectType, int addSequence) {
    this.dialogMenuPath = dialogMenuPath;
    this.dialogObjectType = dialogObjectType;
    this.addSequence = addSequence;
  }

  //=======================================================================================================
  // Build
  //=======================================================================================================

  public TableDialogDefinition<T> build() {
    if (columns.isEmpty()) {
      addField("object", o -> o == null ? "-" : String.valueOf(o), (_, _) -> 0);
    }
    Stream.concat(columns.stream(), details.stream()).forEach(column -> {
      if (column.accessor() == null) {
        ReflectionHelper.FieldAccessData<T> fieldAccessData = ReflectionHelper.createFieldAccessor(dialogObjectType, column.id());
        column.columnType(fieldAccessData.fieldType());
        column.accessor(fieldAccessData.fieldAccessor());
      }
      if (column.comparator() == null) {
        column.comparator(ReflectionHelper.createFieldComparator(dialogObjectType, column.id()));
      }
    });
    if (jsonDetailFunction == null) {
      jsonDetailFunction = (obj, objectMapper) -> computeJson(obj, objectMapper);
    }
    return new TableDialogDefinition<>(this);
  }

  private static <T> String computeJson(T obj, ObjectMapper objectMapper) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      return "{\"Exception\": \"" + jsonSafe(String.valueOf(e)) + " computing JSON for " + jsonSafe(String.valueOf(obj)) + "\"}";
    }
  }

  private static String jsonSafe(String msg) {
    return msg.replaceAll("\"", "''");
  }

  //=======================================================================================================
  // GENERAL DIALOG DATA
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> resourcePath(String dialogResourcePath) {
    this.dialogResourcePath = dialogResourcePath;
    return this;
  }

  public TableDialogDefinitionBuilder<T> resourceDataPath(String dialogResourceDataPath) {
    this.dialogResourceDataPath = dialogResourceDataPath;
    return this;
  }

  public void setFieldFilter(Predicate<Field> fieldFilter) {
    this.columnFieldFilter = fieldFilter;
    this.detailsFieldFilter = fieldFilter;
  }

  public void setFlatFieldDisplayPredicate(Predicate<Field> flatFieldDisplayPredicate) {
    this.flatFieldDisplayPredicate = flatFieldDisplayPredicate;
  }

  //=======================================================================================================
  // DATA SOURCE
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> from(Supplier<Stream<T>> dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public TableDialogDefinitionBuilder<T> fromList(Supplier<Collection<T>> dataSource) {
    return this.from(() -> dataSource.get().stream());
  }

  //=======================================================================================================
  // COLUMNS
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> addAllFields() { // FIXME: check this
    addAllColumns("", dialogObjectType, new HashSet<>(), DEFAULT_MAX_NUMBER_OF_COLUMNS, null, null);
    return addAllDetails();
  }

  public TableDialogDefinitionBuilder<T> columns(String... ids) {
    Arrays.stream(ids).map(id -> new TableDialogColumnDefinition<T>(id)).forEach(columns::add);
    return this;
  }

  public TableDialogDefinitionBuilder<T> addField(String name, Function<T, Object> accessor, Comparator<T> comparator) {
    return columns(name).accessor(name, accessor).comparator(name, comparator);
  }

  public TableDialogDefinitionBuilder<T> columnsMatching(String... patterns) {
    return addAllColumns("", dialogObjectType, new HashSet<>(), DEFAULT_MAX_NUMBER_OF_COLUMNS, List.of(patterns), null);
  }

  public TableDialogDefinitionBuilder<T> columnsNotMatching(String... patterns) {
    return addAllColumns("", dialogObjectType, new HashSet<>(), DEFAULT_MAX_NUMBER_OF_COLUMNS, null, List.of(patterns));
  }

  public TableDialogDefinitionBuilder<T> addAllColumns() {
    return addAllColumns("", dialogObjectType, new HashSet<>(), DEFAULT_MAX_NUMBER_OF_COLUMNS, null, null);
  }

  private TableDialogDefinitionBuilder<T> addAllColumns(String prefix, Class<?> clazz, Set<Class<?>> visited, int maxNumberOfColumns, List<String> columnMatchingPatterns, List<String> patterns) {
    visited.add(clazz);
    for (Field f : clazz.getDeclaredFields()) {
      if (!includeFieldToColumns(f)) {
        continue;
      } else {
        if (columns.size() >= maxNumberOfColumns) {
          break;
        }
      }
      if (flattenFieldDisplay(f)) {
        if (!visited.contains(f.getType())) { // no backward reference
          addAllColumns(prefix + f.getName() + ".", f.getType(), visited, maxNumberOfColumns, columnMatchingPatterns, patterns);
        }
      } else {
        String colId = prefix + f.getName();
        boolean includeCol = columnMatchingPatterns == null || columnMatchingPatterns.stream().anyMatch(p -> colId.matches(p));
        includeCol = includeCol && (patterns == null || patterns.stream().noneMatch(p -> colId.matches(p)));
        if (includeCol) {
          columns(colId);
        }
      }
    }
    return this;
  }

  private boolean flattenFieldDisplay(Field f) {
    return !f.getType().isPrimitive() && !f.getType().isEnum() && flatFieldDisplayPredicate.test(f);
  }

  private boolean includeFieldToColumns(Field field) {
    return !java.lang.reflect.Modifier.isStatic(field.getModifiers()) && columnFieldFilter.test(field);
  }

  //=======================================================================================================
  // DETAILS
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> details(String... ids) {
    Arrays.stream(ids).map(id -> new TableDialogColumnDefinition<T>(id)).forEach(details::add);
    return this;
  }

  public TableDialogDefinitionBuilder<T> addAllDetails() {
    return addAllDetails("", dialogObjectType, new HashSet<>());
  }

  private TableDialogDefinitionBuilder<T> addAllDetails(String prefix, Class<?> clazz, Set<Class<?>> visited) {
    visited.add(clazz);
    for (Field f : clazz.getDeclaredFields()) {
      if (!includeFieldToDetails(f)) {
        continue;
      }
      if (flattenFieldDisplay(f)) {
        if (!visited.contains(f.getType())) { // no backward reference
          addAllDetails(prefix + f.getName() + ".", f.getType(), visited);
        }
      } else {
        checkAddDetailField(prefix + f.getName());
      }
    }
    return this;
  }

  private void checkAddDetailField(String id) {
    if (columns.stream().noneMatch(c -> id.equals(c.id())) && details.stream().noneMatch(c -> id.equals(c.id()))) {
      details.add(new TableDialogColumnDefinition<>(id));
    }
  }

  private boolean includeFieldToDetails(Field field) {
    return !java.lang.reflect.Modifier.isStatic(field.getModifiers()) && detailsFieldFilter.test(field);
  }

  //=======================================================================================================
  // JSON DETAILS
  //=======================================================================================================

  public void computeJsonDetailsBy(BiFunction<T, ObjectMapper, String> jsonDetailFunction) {
    this.jsonDetailFunction = jsonDetailFunction;
  }

  //=======================================================================================================
  // Specify Columns / Details
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> accessor(String id, Function<T, Object> accessor) {
    return forField(id, c -> c.accessor(accessor));
  }

  public TableDialogDefinitionBuilder<T> comparator(String id, Comparator<T> comparator) {
    return forField(id, c -> c.comparator(comparator));
  }

  public TableDialogDefinitionBuilder<T> formatter(String id, Function<Object, String> formatter) {
    return forField(id, c -> c.formatter(formatter));
  }

  public TableDialogDefinitionBuilder<T> label(String id, String label) {
    return forField(id, c -> c.label(label));
  }

  //=======================================================================================================
  // FORMAT
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> dateFormat(String... idPatterns) {
    return dateFormat(new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SS"), idPatterns);
  }

  public TableDialogDefinitionBuilder<T> dateFormat(SimpleDateFormat format, String... idPatterns) {
    Function<Object, String> formatter = o -> {
      try {
        return o == null ? "-" : o instanceof Long l && l == 0 ? "-" : format.format(o);
      } catch (IllegalArgumentException e) {
        return String.valueOf(o);
      }
    };
    return forField(List.of(idPatterns), c -> c.formatter(formatter));
  }

  public TableDialogDefinitionBuilder<T> durationFormat(String... idPatterns) {
    Function<Object, String> formatter = o -> {
      try {
        return switch (o) {
          case null -> "-";
          case Duration duration -> duration(duration);
          case Long duration when duration <= 0 -> duration(Duration.ZERO);
          case Long duration -> duration(Duration.ofMillis(duration));
          default -> String.valueOf(o);
        };
      } catch (IllegalArgumentException e) {
        return String.valueOf(o);
      }
    };
    return forField(List.of(idPatterns), c -> c.formatter(formatter));
  }

  public static String duration(Duration duration) {
    if (duration == null) {
      return "-";
    } else if (duration.getSeconds() >= 1) {
      long seconds = duration.getSeconds();
      long millis = duration.getNano() / 1000_000;
      String secondsStr = String.valueOf(seconds);
      String millisStr = String.valueOf(millis);
      return secondsStr + "." + "000".substring(millisStr.length()) + millisStr + " s";
    } else if (duration.getNano() >= 10_000) {
      long millis = duration.getNano() / 1000_000;
      long nanos = duration.getNano() % 1000_000;
      long micros = nanos / 1000;
      String millisStr = String.valueOf(millis);
      String microStr = String.valueOf(micros);
      return millisStr + "." + "000".substring(microStr.length()) + microStr + " ms";
    } else {
      long nanos = duration.getNano();
      String nanoStr = String.valueOf(nanos);
      return nanoStr + " ns";
    }
  }

  //=======================================================================================================
  // Helper
  //=======================================================================================================

  private TableDialogDefinitionBuilder<T> forField(List<String> idPatterns, Consumer<TableDialogColumnDefinition<T>> handler) {
    return forField(c -> idPatterns.stream().anyMatch(idPattern -> c.id().matches(idPattern)), handler);
  }

  private TableDialogDefinitionBuilder<T> forField(String id, Consumer<TableDialogColumnDefinition<T>> handler) {
    return forField(c -> c.id().matches(id), handler);
  }

  private TableDialogDefinitionBuilder<T> forField(Predicate<TableDialogColumnDefinition<T>> filter, Consumer<TableDialogColumnDefinition<T>> handler) {
    for (TableDialogColumnDefinition<T> column : columns) {
      if (filter.test(column)) {
        handler.accept(column);
      }
    }
    for (TableDialogColumnDefinition<T> detail : details) {
      if (filter.test(detail)) {
        handler.accept(detail);
      }
    }
    return this;
  }

}
