package org.jaw.qutetable.gentable.tabledata;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GenericClassData<T> implements Comparable<GenericClassData<T>> {

  public final int seqId;
  public final String typeName;
  public final Class<T> type;
  public final Supplier<Stream<T>> dataSource;
  private final List<FieldData> fields = new ArrayList<>();

  public GenericClassData(int seqId, String typeName, Class<T> type) {
    this.seqId = seqId;
    this.typeName = typeName;
    this.type = type;
    this.dataSource = null;
  }

  public GenericClassData(int seqId, String typeName, Class<T> type, Supplier<Stream<T>> dataSource) {
    this.seqId = seqId;
    this.typeName = typeName;
    this.type = type;
    this.dataSource = dataSource;
  }

  @Override
  public int compareTo(GenericClassData<T> that) {
    return Integer.compare(this.seqId, that.seqId);
  }

  public GenericClassData<T> remove(String... names) {
    Set<String> toRemove = Set.of(names);
    for (Iterator<FieldData> it = fields.iterator(); it.hasNext(); ) {
      FieldData fieldData = it.next();
      if (toRemove.contains(fieldData.name)) {
        it.remove();
      }
    }
    return this;
  }

  public GenericClassData<T> format(String fieldName, Function<Object, String> formatter) {
    getField(fieldName).formatter = formatter;
    return this;
  }

  private List<String> getFieldsToMatching(String[] fieldPatterns) {
    List<String> fieldsToAdapt = new ArrayList<>();
    for (String forField : fieldPatterns) {
      fields.stream().filter(f -> f.name.matches(forField)).forEach(f -> fieldsToAdapt.add(f.name));
    }
    return fieldsToAdapt;
  }

  public GenericClassData<T> dateFormat(String... fields) {
    return dateFormat(new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SS"), fields);
  }

  public GenericClassData<T> dateFormat(SimpleDateFormat format, String... forFields) {
    List<String> fieldsToAdapt = getFieldsToMatching(forFields);
    for (String fieldName : fieldsToAdapt) {
      Function<Object, String> formatter = o -> {
        try {
          return o == null ? "-" : o instanceof Long l && l == 0 ? "-" : format.format(o);
        } catch (IllegalArgumentException e) {
          return String.valueOf(o);
        }
      };
      format(fieldName, formatter);
    }
    return this;
  }

  public GenericClassData<T> durationFormat(String... forFields) {
    List<String> fieldsToAdapt = getFieldsToMatching(forFields);
    for (String fieldName : fieldsToAdapt) {
      Function<Object, String> formatter = o -> {
        try {
          if (o == null) {
            return "-";
          } else if (o instanceof Duration duration) {
            return DoFormat.duration(duration);
          } else if (o instanceof Long duration) {
            if (duration.longValue() <= 0) {
              return DoFormat.duration(Duration.ZERO);
            } else {
              return DoFormat.duration(Duration.ofMillis(duration));
            }
          } else {
            return String.valueOf(o);
          }
        } catch (IllegalArgumentException e) {
          return String.valueOf(o);
        }
      };
      format(fieldName, formatter);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public GenericClassData<T> addFields(String... names) {
    try {
      for (String name : names) {
        Field field = type.getField(name);
        addField(field.getName(), (Function<T, Object>) createFieldAccessor(field), createFieldComparator(field));
      }
      return this;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public GenericClassData<T> addField(String name, Function<T, Object> accessor, Comparator<Object> comparator) {
    return addField(name, name, accessor, comparator);
  }

  @SuppressWarnings("unchecked")
  public GenericClassData<T> addField(String name, String header, Function<T, Object> accessor, Comparator<Object> comparator) {
    fields.add(new FieldData(name, header, (o) -> accessor.apply((T) o), comparator));
    return this;
  }

  public GenericClassData<T> addFieldIfNew(String name, Function<Object, Object> accessor, Comparator<Object> comparator) {
    if (fields.stream().noneMatch(fd -> name.equals(fd.name))) {
      fields.add(new FieldData(name, name, accessor, comparator));
    }
    return this;
  }

  public GenericClassData<T> addAllFields() {
    Stream.of(type.getDeclaredFields()) //
        .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers())) //
        .forEach(field -> addFieldIfNew(field.getName(), createFieldAccessor(field), createFieldComparator(field)));
    return this;
  }

  public GenericClassData<T> addFieldsFor(String property, String headerPrefix) {
    try {
      Field declaredField = type.getDeclaredField(property);
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

  private Function<Object, Object> createFieldAccessor(Field... fieldPath) {
    return o -> {
      try {
        Object tmp = o;
        for (int i = 0; i < fieldPath.length; i++) {
          if (tmp == null) {
            return "";
          }
          fieldPath[i].setAccessible(true);
          tmp = fieldPath[i].get(tmp);
        }
        return tmp;
      } catch (Exception e) {
        //log.info("Exception accessing field path {}", List.of(fieldPath), e);
        return e.getMessage();
      }
    };
  }

  @SuppressWarnings("unchecked")
  private Comparator<Object> createFieldComparator(Field... fieldPath) {
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

  public List<FieldData> getFields() {
    return fields;
  }

  private FieldData getField(String fieldName) {
    return fields.stream().filter(fd -> fieldName.equals(fd.name)).findFirst().orElse(null);
  }

}
