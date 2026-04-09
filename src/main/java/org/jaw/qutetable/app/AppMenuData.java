/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.app;

import org.jaw.qutetable.dialogdef.TableDialogDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AppMenuData {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Comparator<FlatMenuItem> DIALOG_SORT_ORDER = Comparator.comparing(item -> 10 * item.sequence().get() + item.level());

  private final List<TableDialogDefinition<?>> dialogDefinitions = new ArrayList<>();
  private List<FlatMenuItem> flatMenuItemsCache = null;

  public void add(TableDialogDefinition<?> tdd) {
    flatMenuItemsCache = null; // clear cached menu items
    dialogDefinitions.add(tdd);
  }

  public List<FlatMenuItem> getFlatMenuItems() {
    if (flatMenuItemsCache == null) {
      Map<String, FlatMenuItem> menuMap = new HashMap<>();
      for (var dialogDef : dialogDefinitions) {
        String path = dialogDef.dialogMenuPath();
        String[] parts = path.split("\\.");
        StringBuilder currentPath = new StringBuilder();
        String parentPath = "";
        for (int i = 0; i < parts.length; i++) {
          String label = parts[i];
          if (!currentPath.isEmpty()) {
            parentPath = currentPath.toString();
            currentPath.append(".");
          }
          currentPath.append(label);
          String fullPath = currentPath.toString();
          boolean isLeaf = (i == parts.length - 1);
          if (!menuMap.containsKey(fullPath)) {
            menuMap.put(fullPath, new FlatMenuItem(label, fullPath, parentPath, isLeaf ? dialogDef.dialogResourcePath() : null, i, isLeaf, new AtomicInteger(dialogDef.dialogAddSequence())));
          } else {
            FlatMenuItem mi = menuMap.get(fullPath);
            mi.sequence.set(Math.min(mi.sequence.get(), dialogDef.dialogAddSequence()));
          }
        }
      }
      flatMenuItemsCache = menuMap.values().stream().sorted(DIALOG_SORT_ORDER).collect(Collectors.toList());
    }
    return flatMenuItemsCache;
  }

  public record FlatMenuItem( //
                             String label, //
                             String fullPath, //
                             String parentPath, //
                             String dialogResourcePath, //
                             int level, //
                             boolean isLeaf, //
                             AtomicInteger sequence) {
    public String padding() {
      return "" + level * 15 + "px";
    }
  }

}
