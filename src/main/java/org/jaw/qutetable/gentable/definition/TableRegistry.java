package org.jaw.qutetable.gentable.definition;

import org.jaw.qutetable.DialogDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TableRegistry {

  private final Map<String, TableDialogDefinition<?>> registeredDialogs = new HashMap<>();
  private final List<DialogDefinition> dialogDefinitions = new ArrayList<>();

  public <T> TableDialogDefinition<T> add(String name, Class<T> type) {
    TableDialogDefinition<T> tdd = new TableDialogDefinition<>(name, type);
    registeredDialogs.put(name, tdd);
    dialogDefinitions.add(new DialogDefinition(name, "/api/table?dialog=" + name));
    return tdd;
  }

  public TableDialogDefinition getDialogTableDefinitions(String dialogName) {
    return registeredDialogs.get(dialogName);
  }

  public List<DialogDefinition> getDialogDefinitions() {
    return new ArrayList<>(dialogDefinitions);
  }

}
