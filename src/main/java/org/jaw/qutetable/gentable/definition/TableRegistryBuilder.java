package org.jaw.qutetable.gentable.definition;

import java.util.HashMap;
import java.util.Map;

public class TableRegistryBuilder {

  private final Map<String, TableDialogDefinitionBuilder<?>> registeredDialogs = new HashMap<>();

  public <T> TableDialogDefinitionBuilder<T> add(String dialogMenuPath, Class<T> type) {
    TableDialogDefinitionBuilder<T> tdd = new TableDialogDefinitionBuilder<>(dialogMenuPath, type);
    tdd.resourcePath("/api/table?dialog=" + dialogMenuPath);
    tdd.resourceDataPath("/api/table/data?dialog=" + dialogMenuPath);
    registeredDialogs.put(dialogMenuPath, tdd);
    return tdd;
  }

  public TableRegistry build() {
    return new TableRegistry(registeredDialogs);
  }

}
