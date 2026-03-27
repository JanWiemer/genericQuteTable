package org.jaw.qutetable.gentable.templatedata;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
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
    this(tableDialogData, cells, new HashMap<>());
  }

  public TableRowData detail(String key, String value) {
    details.put(key, value);
    return this;
  }

  public TableRowData jsonDetail(String jsonDetails) {
    this.jsonDetails = jsonDetails;
    return this;
  }

  public String getJsonRepresentation() { // used in templates
    return jsonDetails;
  }

  public String getDetailsJson() { // used in templates
    try {
      return tableDialogData.objectMapper.writeValueAsString(details);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
