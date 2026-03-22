package org.jaw.qutetable.gentable.definition;

import java.util.Comparator;
import java.util.function.Function;

public final class TableDialogColumnDefinition<T> {

  private final String id;
  private String label;
  private Function<T, Object> accessor;
  private Function<Object, String> formatter = o -> o == null ? "-" : String.valueOf(o);
  private Comparator<T> comparator;

  public TableDialogColumnDefinition(String id) {
    this.id = id;
    this.label = id;
  }

  public TableDialogColumnDefinition(String id, String label, Function<T, Object> accessor) {
    this.id = id;
    this.label = label;
    this.accessor = accessor;
  }

  public TableDialogColumnDefinition<T> label(String label) {
    this.label = label;
    return this;
  }

  public TableDialogColumnDefinition<T> accessor(Function<T, Object> accessor) {
    this.accessor = accessor;
    return this;
  }

  public TableDialogColumnDefinition<T> formatter(Function<Object, String> formatter) {
    this.formatter = formatter;
    return this;
  }

  public TableDialogColumnDefinition<T> comparator(Comparator<T> comparator) {
    this.comparator = comparator;
    return this;
  }

  public Function<T, String> getStringAccessor() {
    return accessor.andThen(formatter);
  }

  public String id() {
    return id;
  }

  public String label() {
    return label;
  }

  public Function<T, Object> accessor() {
    return accessor;
  }

  public Function<Object, String> formatter() {
    return formatter;
  }

  public Comparator<T> comparator() {
    return comparator;
  }

  @Override
  public String toString() {
    return "TableColumnDefinition[id=" + id + ", " + "header=" + label + "]";
  }

}
