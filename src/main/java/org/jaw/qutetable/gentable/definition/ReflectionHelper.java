package org.jaw.qutetable.gentable.definition;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

class ReflectionHelper {

  static <T> Function<T, Object> createFieldAccessor(Class<T> rootType, String fieldPath) {
    try {
      List<Field> fieldList = toFieldList(rootType, fieldPath);
      return createFieldAccessor(fieldList);
    } catch (NoSuchFieldException e) {
      return o -> "Unknown Field " + rootType.getName() + "." + fieldPath;
    }
  }

  static <T> Comparator<T> createFieldComparator(Class<T> rootType, String fieldPath) {
    try {
      List<Field> fieldList = toFieldList(rootType, fieldPath);
      return createFieldComparator(fieldList);
    } catch (NoSuchFieldException e) {
      return (_, _) -> 0;
    }
  }

  private static <T> List<Field> toFieldList(Class<T> rootType, String fieldPath) throws NoSuchFieldException {
    List<String> split = List.of(fieldPath.split("\\."));
    List<Field> fieldList = new ArrayList<>(split.size());
    Class<?> type = rootType;
    for (var fieldName : split) {
      var field = type.getDeclaredField(fieldName);
      fieldList.add(field);
      type = field.getType();
    }
    return fieldList;
  }


  static <T> Function<T, Object> createFieldAccessor(Field... fieldPath) {
    return createFieldAccessor(List.of(fieldPath));
  }

  static <T> Function<T, Object> createFieldAccessor(List<Field> fieldPath) {
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

  static <T> Comparator<T> createFieldComparator(Field... fieldPath) {
    return createFieldComparator(List.of(fieldPath));
  }

  static <T> Comparator<T> createFieldComparator(List<Field> fieldPath) {
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

}
