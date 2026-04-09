/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jaw.qutetable.dialogdef.TableDialogDefinition;
import org.jaw.qutetable.table.data.TableCellData;
import org.jaw.qutetable.table.data.TableDialogData;
import org.jaw.qutetable.table.data.TableRowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

public class GenericTableDataAccess<T> {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final int DEFAULT_MAX_ROWS = 50;
  public static final String JSON_DETAILS_COL_ID = "@@@JSON-DETAILS@@@";

  public record SearchDefinition(String filter, String sortCol, Object sortDir, Integer maxRows) {}

  public TableDialogData createTableData(TableDialogDefinition<T> dialog, SearchDefinition searchDef, ObjectMapper objectMapper) {
    TableDialogData tdd = new TableDialogData(dialog.dialogMenuPath(), dialog.dialogResourceDataPath(), objectMapper);
    for (var col : dialog.columns()) {
      tdd.col(col.label(), col.id(), col.isNumericDataColumn());
    }
    List<FilterCondition> filterConditions = searchDef.filter == null ? Collections.emptyList() : computeFilterConditions(searchDef.filter);
    log.debug("...filter conditions: {}", filterConditions);
    Stream<T> dataStream = dialog.dataSource().get();
    dataStream.map(obj -> asMap(obj, dialog, objectMapper)) //
        .filter(obj -> checkFilter(obj, filterConditions)) //
        .limit(searchDef.maxRows == null ? DEFAULT_MAX_ROWS : searchDef.maxRows) //
        .forEach(obj -> addRow(obj, dialog, tdd));
    return tdd;
  }

  private List<FilterCondition> computeFilterConditions(String filterTxt) {
    List<FilterCondition> res = new ArrayList<>();
    for (String condition : filterTxt.split(" & ")) {
      if (condition.startsWith(":") && condition.indexOf("=") > 0) {
        String column = condition.substring(1, condition.indexOf("=")).trim();
        String pattern = condition.substring(condition.indexOf("=") + 1).trim();
        res.add(new FilterCondition(column, pattern));
      } else {
        res.add(new FilterCondition(null, condition));
      }
    }
    return res;
  }

  record FilterCondition(String column, String pattern) {
    boolean matches(String value) {
      return value != null && value.matches(".*" + pattern + ".*");
    }
  }

  private boolean checkFilter(Map<String, TableCellData> obj, List<FilterCondition> conditions) {
    for (FilterCondition condition : conditions) {
      if (condition.column() == null) { // full text search in all fields
        if (obj.values().stream().noneMatch(v -> v != null && condition.matches(v.displayText()))) {
          return false;
        }
      } else {
        String v = obj.get(condition.column).displayText();
        if (v != null && !condition.matches(v)) {
          return false;
        }
      }
    }
    return true;
  }

  private void addRow(Map<String, TableCellData> obj, TableDialogDefinition<?> dialog, TableDialogData tdd) {
    List<TableCellData> row = new ArrayList<>();
    for (var col : dialog.columns()) {
      row.add(obj.get(col.id()));
    }
    TableRowData rowDef = tdd.row(row);
    for (var col : dialog.details()) {
      rowDef.detail(col.id(), obj.get(col.id()));
    }
    rowDef.jsonDetail(obj.get(JSON_DETAILS_COL_ID).displayText());
  }

  private Map<String, TableCellData> asMap(T obj, TableDialogDefinition<T> dialog, ObjectMapper objectMapper) {
    Map<String, TableCellData> res = new HashMap<>();
    for (var col : dialog.columns()) {
      Object rawData = col.accessor().apply(obj);
      String displayValue = col.formatter().apply(rawData);
      res.put(col.id(), new TableCellData(displayValue, displayValue, convertRawData(rawData)));
    }
    for (var col : dialog.details()) {
      Object rawData = col.accessor().apply(obj);
      String displayValue = col.formatter().apply(rawData);
      res.put(col.id(), new TableCellData(displayValue, displayValue, convertRawData(rawData)));
    }
    String jsonVal = dialog.computeJSonDetails(obj, objectMapper);
    res.put(JSON_DETAILS_COL_ID, new TableCellData(jsonVal, jsonVal, jsonVal));
    return res;
  }

  private Object convertRawData(Object data) {
    return switch (data) {
      case null -> "-";
      case Duration d -> d.toNanos();
      default -> data;
    };
  }

}
