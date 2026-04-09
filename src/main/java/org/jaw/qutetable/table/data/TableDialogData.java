/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.table.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class  TableDialogData {

  public final String dialogTitle;
  public final String tableDataPath;
  public String initialFilter;
  public List<TableColumnData> columns = new ArrayList<>();
  public List<TableRowData> rows = new ArrayList<>();
  final ObjectMapper objectMapper;

  public TableDialogData(String dialogTitle, String tableDataPath, ObjectMapper objectMapper) {
    this.dialogTitle = dialogTitle;
    this.tableDataPath = tableDataPath;
    this.objectMapper = objectMapper;
  }

  public TableDialogData initialFilter(String initialFilter) {
    this.initialFilter = initialFilter;
    return this;
  }

  public TableDialogData col(String name, String toolTip, boolean numeric) {
    columns.add(new TableColumnData(name, toolTip, numeric));
    return this;
  }

  public TableRowData row(TableCellData... cellsInRow) {
    return row(List.of(cellsInRow));
  }

  public TableRowData row(List<TableCellData> cellsInRow) {
    TableRowData row = new TableRowData(this, cellsInRow);
    rows.add(row);
    return row;
  }

  public TableColumnData getColumn(String columnName) {
    return columns.stream().filter(c -> c.name().equals(columnName)).findFirst().orElse(null);
  }

}
