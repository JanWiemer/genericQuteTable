/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.dialogdef;


import org.jaw.qutetable.app.AppData;
import org.jaw.qutetable.app.AppMenuData;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DialogRegistry {

  public static Builder create(String appTitle) {
    return new Builder(appTitle);
  }

  private final String appTitle;
  private final Map<String, TableDialogDefinition<?>> registeredDialogs = new HashMap<>();

  private DialogRegistry(String appTitle, Map<String, TableDialogDefinitionBuilder<?>> builders) {
    this.appTitle = appTitle;
    builders.forEach((key, value) -> registeredDialogs.put(key, value.build()));
  }

  public Collection<TableDialogDefinition<?>> getDialogTableDefinitions() {
    return registeredDialogs.values();
  }

  @SuppressWarnings("unchecked")
  public <T> TableDialogDefinition<T> getDialogTableDefinitions(String dialogName) {
    return (TableDialogDefinition<T>) registeredDialogs.get(dialogName);
  }

  public AppData getApplicationData() {
    return new AppData(appTitle, getApplicationMenu());
  }

  public AppMenuData getApplicationMenu() {
    AppMenuData res = new AppMenuData();
    for (TableDialogDefinition<?> tdd : registeredDialogs.values()) {
      res.add(tdd);
    }
    return res;
  }

  public static class Builder {

    private final String appTitle;
    private final Map<String, TableDialogDefinitionBuilder<?>> registeredDialogs = new HashMap<>();
    private Predicate<Field> globalFieldFilter = _ -> true;
    private Predicate<Field> flatFieldDisplayPredicate = _ -> false;
    private int addSequence = 0;

    private Builder(String appTitle) {
      this.appTitle = appTitle; // should be called by create method
    }

    public void setGlobalFieldFilter(Predicate<Field> globalFieldFilter) {
      this.globalFieldFilter = globalFieldFilter;
    }

    public void setFlatFieldDisplayPredicate(Predicate<Field> flatFieldDisplayPredicate) {
      this.flatFieldDisplayPredicate = flatFieldDisplayPredicate;
    }

    public <T> TableDialogDefinitionBuilder<T> add(String path, Class<T> type) {
      TableDialogDefinitionBuilder<T> tdd = new TableDialogDefinitionBuilder<>(path, type, ++addSequence);
      tdd.resourcePath("/api/table?dialog=" + path);
      tdd.resourceDataPath("/api/table/data?dialog=" + path);
      tdd.setFieldFilter(globalFieldFilter);
      tdd.setFlatFieldDisplayPredicate(flatFieldDisplayPredicate);
      registeredDialogs.put(path, tdd);
      return tdd;
    }

    public <T> TableDialogDefinitionBuilder<T> add(String path, Class<T> type, Supplier<Collection<T>> dataSource) {
      return add(path, type).fromList(dataSource);
    }

    public <T> TableDialogDefinitionBuilder<T> addS(String path, Class<T> type, Supplier<Stream<T>> dataSource) {
      return add(path, type).from(dataSource);
    }

    public DialogRegistry build() {
      return new DialogRegistry(appTitle, registeredDialogs);
    }

  }
}
