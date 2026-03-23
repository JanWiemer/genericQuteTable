package org.jaw.qutetable.gentable.definition;

import io.quarkus.logging.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TableDialogDefinitionBuilder<T> {

  final String dialogMenuPath;
  final Class<T> dialogObjectType;
  String dialogResourcePath;
  String dialogResourceDataPath;
  Supplier<Stream<T>> dataSource;
  final List<TableDialogColumnDefinition<T>> columns = new ArrayList<>();
  final List<TableDialogColumnDefinition<T>> details = new ArrayList<>();
  Predicate<Field> flatFieldDisplayPredicate = null;

  public TableDialogDefinitionBuilder(String dialogMenuPath, Class<T> dialogObjectType) {
    this.dialogMenuPath = dialogMenuPath;
    this.dialogObjectType = dialogObjectType;
  }

  //=======================================================================================================
  // Build
  //=======================================================================================================

  public TableDialogDefinition<T> build() {
    Stream.concat(columns.stream(), details.stream()).forEach(column -> {
      if (column.accessor() == null) {
        column.accessor(ReflectionHelper.createFieldAccessor(dialogObjectType, column.id()));
      }
      if (column.comparator() == null) {
        column.comparator(ReflectionHelper.createFieldComparator(dialogObjectType, column.id()));
      }
    });
    return new TableDialogDefinition<>(this);
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

  //=======================================================================================================
  // DATA SOURCE
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> from(Supplier<Stream<T>> dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public TableDialogDefinitionBuilder<T> fromList(Supplier<List<T>> dataSource) {
    return this.from(() -> dataSource.get().stream());
  }

  //=======================================================================================================
  // COLUMNS
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> columns(String... columnIds) {
    Arrays.stream(columnIds).map(id -> new TableDialogColumnDefinition(id)).forEach(columns::add);
    return this;
  }


  //=======================================================================================================
  // DETAILS
  //=======================================================================================================

  public TableDialogDefinitionBuilder<T> details(String... detailIds) {
    Arrays.stream(detailIds).map(id -> new TableDialogColumnDefinition(id)).forEach(details::add);
    return this;
  }

  public void setFlatFieldDisplayPredicate(Predicate<Field> flatFieldDisplayPredicate) {
    this.flatFieldDisplayPredicate = flatFieldDisplayPredicate;
  }

  public TableDialogDefinitionBuilder<T> addAllDetails() {
    return addAllDetails("", dialogObjectType, new HashSet<>());
  }


  private TableDialogDefinitionBuilder<T> addAllDetails(String prefix, Class<?> clazz, Set<Class<?>> visited) {
    Log.info(" - " + prefix + "  / " + clazz.getSimpleName() + " / " + visited);
    visited.add(clazz);
    for (Field f : clazz.getDeclaredFields()) {
      if (flatFieldDisplayPredicate!=null && flatFieldDisplayPredicate.test(f)) {
        if (!visited.contains(f.getType())) { // no backward reference
          addAllDetails(f.getName() + ".", f.getType(), visited);
        }
      } else {
        checkAddDetailField(prefix + f.getName());
      }
    }
    return this;
  }

  private void checkAddDetailField(String id) {
    Log.info(" --> add " + id);
    if (columns.stream().noneMatch(c -> id.equals(c.id()))
        && details.stream().noneMatch(c -> id.equals(c.id()))
    ) {
      details.add(new TableDialogColumnDefinition(id));
    }
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
        if (o == null) {
          return "-";
        } else if (o instanceof Duration duration) {
          return duration(duration);
        } else if (o instanceof Long duration) {
          if (duration.longValue() <= 0) {
            return duration(Duration.ZERO);
          } else {
            return duration(Duration.ofMillis(duration));
          }
        } else {
          return String.valueOf(o);
        }
      } catch (IllegalArgumentException e) {
        return String.valueOf(o);
      }
    };
    return forField(List.of(idPatterns), c -> c.formatter(formatter));
  }

  private String duration(Duration duration) {
    return duration.toString();
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
