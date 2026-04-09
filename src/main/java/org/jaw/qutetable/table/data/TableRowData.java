/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.table.data;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TableRowData {

  public final TableDialogData tableDialogData;
  public final List<TableCellData> cells;
  public final Map<String, String> details;
  public String jsonDetails;

  public TableRowData(TableDialogData tableDialogData, List<TableCellData> cells, Map<String, String> details) {
    this.tableDialogData = tableDialogData;
    this.cells = cells;
    this.details = details;
    this.jsonDetails = "{\"message\": \"No JSON available!\"}";
  }

  public TableRowData(TableDialogData tableDialogData, List<TableCellData> cells) {
    this(tableDialogData, cells, new LinkedHashMap<>()); // LinkedHashMap to preserve the order the entries are added
  }

  public TableRowData detail(String key, TableCellData value) {
    details.put(key, value.displayText());
    return this;
  }

  public TableRowData jsonDetail(String jsonDetails) {
    this.jsonDetails = jsonDetails;
    return this;
  }

  public String getJsonRepresentation() { // used in templates for the JSON detail tree view
    return jsonDetails;
  }

  public String getDetailsAsJsonString() { // used in templates for the detail key-value-table
    try {
      return tableDialogData.objectMapper.writeValueAsString(details);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to get JSON for details area: " + e.getMessage(), e);
    }
  }

}
