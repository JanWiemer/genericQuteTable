package org.jaw.qutetable.gentable.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TableDialogData {

  final ObjectMapper objectMapper;
  public final String dialogTitle;
  public final String tableDataPath;
  public String initialFilter;
  public List<TableColumnDefinition> columns = new ArrayList<>();
  public List<TableRowData> rows = new ArrayList<>();

  public TableDialogData(String dialogTitle, String tableDataPath, ObjectMapper objectMapper) {
    this.dialogTitle = dialogTitle;
    this.tableDataPath = tableDataPath;
    this.objectMapper = objectMapper;
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

  public TableRowData row(String... cellsInRow) {
    return row(List.of(cellsInRow));
  }

  public TableRowData row(List<String> cellsInRow) {
    List<TableCellData> cellList = cellsInRow.stream().map(s -> new TableCellData(s, null)).toList();
    TableRowData row = new TableRowData(this, cellList);
    rows.add(row);
    return row;
  }

}
