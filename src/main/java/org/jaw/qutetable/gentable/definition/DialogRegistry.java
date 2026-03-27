package org.jaw.qutetable.gentable.definition;

import org.jaw.qutetable.ApplicationMenu;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DialogRegistry {

  public static DialogRegistry.Builder create() {
    return new DialogRegistry.Builder();
  }

  private final Map<String, TableDialogDefinition<?>> registeredDialogs = new HashMap<>();

  private DialogRegistry(Map<String, TableDialogDefinitionBuilder<?>> builders) {
    builders.forEach((key, value) -> registeredDialogs.put(key, value.build()));
  }

  public Collection<TableDialogDefinition<?>> getDialogTableDefinitions() {
    return registeredDialogs.values();
  }

  public <T> TableDialogDefinition<T> getDialogTableDefinitions(String dialogName) {
    return (TableDialogDefinition<T>) registeredDialogs.get(dialogName);
  }

  public ApplicationMenu getApplicationMenu() {
    ApplicationMenu res = new ApplicationMenu();
    for (TableDialogDefinition<?> tdd : registeredDialogs.values()) {
      res.add(tdd);
    }
    return res;
  }

  public static class Builder {

    private final Map<String, TableDialogDefinitionBuilder<?>> registeredDialogs = new HashMap<>();
    private Predicate<Field> flatFieldDisplayPredicate = null;

    private Builder() {
      // should be called by create method
    }

    public void setFlatFieldDisplayPredicate(Predicate<Field> flatFieldDisplayPredicate) {
      this.flatFieldDisplayPredicate = flatFieldDisplayPredicate;
    }

    public <T> TableDialogDefinitionBuilder<T> add(String dialogMenuPath, Class<T> type) {
      TableDialogDefinitionBuilder<T> tdd = new TableDialogDefinitionBuilder<>(dialogMenuPath, type);
      tdd.resourcePath("/api/table?dialog=" + dialogMenuPath);
      tdd.resourceDataPath("/api/table/data?dialog=" + dialogMenuPath);
      tdd.setFlatFieldDisplayPredicate(flatFieldDisplayPredicate);
      registeredDialogs.put(dialogMenuPath, tdd);
      return tdd;
    }

    public DialogRegistry build() {
      return new DialogRegistry(registeredDialogs);
    }

  }
}
