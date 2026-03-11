package org.jaw.qutetable.gentable.definition;

import io.quarkus.logging.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TableDialogDefinition<T> {

  public final String dialogId;
  public final Class<T> dialogObjectType;
  public final Supplier<Stream<T>> dataSource;
  private final List<TableDialogColumnDefinition<T>> columns = new ArrayList<>();

  public TableDialogDefinition(String dialogId, Class<T> type) {
    this.dialogId = dialogId;
    this.dialogObjectType = type;
    this.dataSource = null;
  }

  public TableDialogDefinition(String dialogId, Class<T> type, Supplier<Stream<T>> dataSource) {
    this.dialogId = dialogId;
    this.dialogObjectType = type;
    this.dataSource = dataSource;
  }

  //=======================================================================================================
  // COLUMNS
  //=======================================================================================================

  private List<String> getColumnsMatching(String[] columnIdPatterns) {
    List<String> res = new ArrayList<>();
    for (String colIdPattern : columnIdPatterns) {
      columns.stream().filter(f -> f.id().matches(colIdPattern)).forEach(f -> res.add(f.id()));
    }
    return res;
  }

  public TableDialogDefinition<T> remove(String... columnIds) {
    Set<String> toRemove = Set.of(columnIds);
    columns.removeIf(fieldData -> toRemove.contains(fieldData.id()));
    return this;
  }

  public TableDialogDefinition<T> addFields(String... names) {
    try {
      for (String name : names) {
        Field field = dialogObjectType.getField(name);
        addField(field.getName(), (Function<T, Object>) createFieldAccessor(field), createFieldComparator(field));
      }
      return this;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public TableDialogDefinition<T> addField(String name, Function<T, Object> accessor, Comparator<T> comparator) {
    return addField(name, name, accessor, comparator);
  }

  public TableDialogDefinition<T> addField(String name, String header, Function<T, Object> accessor, Comparator<T> comparator) {
    columns.add(new TableDialogColumnDefinition<T>(name, header, (o) -> accessor.apply((T) o)).comparator(comparator));
    return this;
  }

  public TableDialogDefinition<T> addFieldIfNew(String name, Function<T, Object> accessor, Comparator<T> comparator) {
    if (columns.stream().noneMatch(fd -> name.equals(fd.id()))) {
      columns.add(new TableDialogColumnDefinition<T>(name, name, accessor).comparator(comparator));
    }
    return this;
  }

  public TableDialogDefinition<T> addAllFields() {
    Stream.of(dialogObjectType.getDeclaredFields()) //
        .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers())) //
        .forEach(field -> addFieldIfNew(field.getName(), createFieldAccessor(field), createFieldComparator(field)));
    return this;
  }

  public TableDialogDefinition<T> addFieldsFor(String property, String headerPrefix) {
    try {
      Field declaredField = dialogObjectType.getDeclaredField(property);
      Stream.of(declaredField.getType().getDeclaredFields()) //
          .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers())) //
          .forEach(field -> addFieldIfNew(property + "_" + field.getName(), //
              createFieldAccessor(declaredField, field), //
              createFieldComparator(declaredField, field)));
      remove(property);
    } catch (Exception e) {
      // ignore
    }
    return this;
  }

  private Function<T, Object> createFieldAccessor(Field... fieldPath) {
    return o -> {
      try {
        Object tmp = o;
        for (Field field : fieldPath) {
          if (tmp == null) {
            return "";
          }
          field.setAccessible(true);
          tmp = field.get(tmp);
        }
        return tmp;
      } catch (Exception e) {
        Log.info("Exception accessing field path " + List.of(fieldPath), e);
        return e.getMessage();
      }
    };
  }

  //=======================================================================================================
  // COMPARATOR
  //=======================================================================================================

  @SuppressWarnings("unchecked")
  private Comparator<T> createFieldComparator(Field... fieldPath) {
    return (a, b) -> {
      try {
        Object ta = a;
        Object tb = b;
        for (Field field : fieldPath) {
          if (ta == null && tb == null) {
            return 0;
          } else if (ta == null) {
            return -1;
          } else if (tb == null) {
            return 1;
          }
          ta = field.get(ta);
          tb = field.get(tb);
        }
        if (ta == null && tb == null) {
          return 0;
        } else if (ta == null) {
          return -1;
        } else if (tb == null) {
          return 1;
        } else if (ta instanceof Comparable<?> && tb instanceof Comparable<?>) {
          return ((Comparable<Object>) ta).compareTo(tb);
        } else {
          return String.valueOf(ta).compareTo(String.valueOf(tb));
        }
      } catch (Exception e) {
        return 0;
      }
    };
  }

  //=======================================================================================================
  // FORMAT
  //=======================================================================================================

  public TableDialogDefinition<T> format(String fieldName, Function<Object, String> formatter) {
    columns.stream().filter(fd -> fieldName.equals(fd.id())).findFirst().orElse(null).formatter(formatter);
    return this;
  }

  public TableDialogDefinition<T> dateFormat(String... forColumns) {
    return dateFormat(new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SS"), forColumns);
  }

  public TableDialogDefinition<T> dateFormat(SimpleDateFormat format, String... forColumns) {
    Function<Object, String> formatter = o -> {
      try {
        return o == null ? "-" : o instanceof Long l && l == 0 ? "-" : format.format(o);
      } catch (IllegalArgumentException e) {
        return String.valueOf(o);
      }
    };
    getColumnsMatching(forColumns).forEach(col-> format(col, formatter));
    return this;
  }

  public TableDialogDefinition<T> durationFormat(String... forColumns) {
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
    getColumnsMatching(forColumns).forEach(col-> format(col, formatter));
    return this;
  }

  private String duration(Duration duration) {
    return duration.toString();
  }

}
