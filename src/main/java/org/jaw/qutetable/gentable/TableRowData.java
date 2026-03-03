package org.jaw.qutetable.gentable;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TableRowData(TableDialogData tableDialogData, List<TableCellData> cells, Map<String, Object> details) {

  public TableRowData(TableDialogData tableDialogData, List<TableCellData> cells) {
    this(tableDialogData, cells, new HashMap<>());
  }

  public TableRowData detail(String key, Object value) {
    details.put(key, value);
    return this;
  }

  public String getDetailsJson() {
    try {
      return tableDialogData.objectMapper.writeValueAsString(details);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
