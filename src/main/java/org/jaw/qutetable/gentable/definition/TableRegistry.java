package org.jaw.qutetable.gentable.definition;

import org.jaw.qutetable.ApplicationMenu;
import org.jaw.qutetable.DialogDefinition;

import java.util.*;

public class TableRegistry {

  private final Map<String, TableDialogDefinition<?>> registeredDialogs = new HashMap<>();

  public <T> TableDialogDefinition<T> add(String name, Class<T> type) {
    TableDialogDefinition<T> tdd = new TableDialogDefinition<>(name, type);
    tdd.resourcePath("/api/table?dialog=" + name);
    registeredDialogs.put(name, tdd);
    return tdd;
  }

  public Collection<TableDialogDefinition<?>> getDialogTableDefinitions() {
    return registeredDialogs.values();
  }

  public TableDialogDefinition getDialogTableDefinitions(String dialogName) {
    return registeredDialogs.get(dialogName);
  }

  public ApplicationMenu getApplicationMenu() {
    ApplicationMenu res = new ApplicationMenu();
    for (TableDialogDefinition<?> tdd : registeredDialogs.values()) {
      res.add(tdd);
    }
    return res;
  }
}
