package org.jaw.qutetable.gentable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TableDialogData {

  public final String dialogTitle;
  public final String tableDataPath;
  public String initialFilter;
  public List<TableColumnDefinition> columns = new ArrayList<>();
  public List<TableRowData> rows = new ArrayList<>();

  public TableDialogData(String dialogTitle, String tableDataPath) {
    this.dialogTitle = dialogTitle;
    this.tableDataPath = tableDataPath;
  }

  public TableDialogData initialFilter(String initialFilter) {
    this.initialFilter = initialFilter;
    return this;
  }

  public TableDialogData col(String name, String description) {
    columns.add(new TableColumnDefinition(name, description));
    return this;
  }

  public TableDialogData col(String name) {
    return col(name, null);
  }

  public TableDialogData row(String... cellsInRow) {
    List<TableCellData> cellList = Stream.of(cellsInRow).map(s -> new TableCellData(s, null)).toList();
    rows.add(new TableRowData(cellList));
    return this;
  }

}
