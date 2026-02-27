package org.jaw.qutetable.gentable.tabledata;

import java.util.Comparator;
import java.util.function.Function;

public class FieldData {
    final String name;
    final String header;
    final Function<Object, Object> accessor;
    Function<Object, String> formatter = o -> o == null ? "-" : String.valueOf(o);
    final Comparator<Object> comparator;

    public FieldData(String name, String header, Function<Object, Object> accessor, Comparator<Object> comparator) {
      this.name = name;
      this.header = header;
      this.accessor = accessor;
      this.comparator = comparator;
    }

    Function<Object, String> getStringAccessor() {
      return accessor.andThen(formatter);
    }

  }