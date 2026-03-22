package org.jaw.qutetable.gentable.definition;

import org.jaw.qutetable.ApplicationMenu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TableRegistry {

  private final Map<String, TableDialogDefinition<?>> registeredDialogs = new HashMap<>();

  public TableRegistry(Map<String, TableDialogDefinitionBuilder<?>> builders) {
    builders.entrySet().forEach(entry -> {
      registeredDialogs.put(entry.getKey(), entry.getValue().build());
    });
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
