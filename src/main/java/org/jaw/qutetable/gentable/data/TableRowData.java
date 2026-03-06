package org.jaw.qutetable.gentable.data;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TableRowData(TableDialogData tableDialogData, List<TableCellData> cells, Map<String, Object> details,
                           String jsonDetails) {

  public TableRowData(TableDialogData tableDialogData, List<TableCellData> cells) {
    this(tableDialogData, cells, new HashMap<>(), """
                {
                "id": 101,
                "name": "Max Mustermann",
                "email": "max@example.com",
                "is_active": true,
                "roles": ["Admin", "User"],
                "address": {
                  "city": "Berlin",
                  "TROLLE": ["Admin", "User", "MUTT"],
                  "zip": "10115",
                  "addresssub": {
                    "city": "Berlin",
                    "zip": "10115"
                  }
                },
                "phone": null
              }
        """);
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
