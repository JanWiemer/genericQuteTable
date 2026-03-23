package org.jaw.qutetable.gentable.definition;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TableRegistryBuilder {

  private final Map<String, TableDialogDefinitionBuilder<?>> registeredDialogs = new HashMap<>();
  Predicate<Field> flatFieldDisplayPredicate = null;

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

  public TableRegistry build() {
    return new TableRegistry(registeredDialogs);
  }

}
