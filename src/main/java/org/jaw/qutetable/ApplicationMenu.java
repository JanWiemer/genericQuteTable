package org.jaw.qutetable;

import org.jaw.qutetable.gentable.definition.TableDialogDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationMenu {

  public final List<TableDialogDefinition<?>> dialogDefinitions = new ArrayList<>();
  public List<FlatMenuItem> flatMenuItems = null;


  public void add(TableDialogDefinition<?> tdd) {
    dialogDefinitions.add(tdd);
  }

  public ApplicationMenu build() {
    flatMenuItems = buildFlatMenu();
    return this;
  }


  public record FlatMenuItem( //
                              String label, //
                              String fullPath, //
                              String parentPath, //
                              String dialogResourcePath, //
                              int level, //
                              boolean isLeaf) {
    public String padding() {
      return "" + level * 15 + "px";
    }
  }

  public List<FlatMenuItem> buildFlatMenu() {
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
          menuMap.put(fullPath, new FlatMenuItem(label, fullPath, parentPath, dialogDef.dialogResourcePath(), i, isLeaf));
        }
      }
    }
    return menuMap.values().stream() //
        .sorted(Comparator.comparing(item -> item.fullPath)) //s
        .collect(Collectors.toList());
  }

}
