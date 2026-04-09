/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.dialogdef;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

class ReflectionHelper {

  record FieldAccessData<T>(Class<?> fieldType, Function<T, Object> fieldAccessor) {}

  static <T> FieldAccessData<T> createFieldAccessor(Class<T> rootType, String fieldPath) {
    try {
      List<Field> fieldList = toFieldList(rootType, fieldPath);
      return new FieldAccessData<>(fieldList.getLast().getType(), createFieldAccessor(fieldList));
    } catch (ReflectionException e) {
      return new FieldAccessData<>(String.class, _ -> "Unknown Field " + rootType.getName() + "." + fieldPath + ": " + e.getMessage());
    } catch (Exception e) {
      return new FieldAccessData<>(String.class, _ -> String.valueOf(e));
    }
  }

  static <T> Comparator<T> createFieldComparator(Class<T> rootType, String fieldPath) {
    try {
      List<Field> fieldList = toFieldList(rootType, fieldPath);
      return createFieldComparator(fieldList);
    } catch (Exception e) {
      return (_, _) -> 0;
    }
  }

  private static <T> List<Field> toFieldList(Class<T> rootType, String fieldPath) {
    List<String> split = List.of(fieldPath.split("\\."));
    List<Field> fieldList = new ArrayList<>(split.size());
    Class<?> type = rootType;
    for (var fieldName : split) {
      var field = getDeclaredField(fieldName, type);
      fieldList.add(field);
      type = field.getType();
    }
    return fieldList;
  }

  private static Field getDeclaredField(String fieldName, Class<?> type) {
    try {
      return type.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new ReflectionException("No field " + type.getName() + "." + fieldName);
    }
  }

  private static <T> Function<T, Object> createFieldAccessor(List<Field> fieldPath) {
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
        return e.getMessage();
      }
    };
  }

  static <T> Comparator<T> createFieldComparator(Field... fieldPath) {
    return createFieldComparator(List.of(fieldPath));
  }

  @SuppressWarnings("unchecked")
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
          return ((Comparable) ta).compareTo(tb);
        } else {
          return String.valueOf(ta).compareTo(String.valueOf(tb));
        }
      } catch (Exception e) {
        return 0;
      }
    };
  }

  private static final class ReflectionException extends RuntimeException {
    public ReflectionException(String message) {
      super(message);
    }

    public ReflectionException(String message, Throwable cause) {
      super(message, cause);
    }
  }

}
