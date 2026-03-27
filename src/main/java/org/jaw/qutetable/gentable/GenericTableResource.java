package org.jaw.qutetable.gentable;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jaw.qutetable.gentable.templatedata.TableDialogData;
import org.jaw.qutetable.gentable.templatedata.TableRowData;
import org.jaw.qutetable.gentable.definition.TableDialogDefinition;
import org.jaw.qutetable.gentable.definition.DialogRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Path("/api/table")
@Singleton
public class GenericTableResource {

  public static final String JSON_DETAILS_COL_ID = "@@@JSON-DETAILS@@@";

  @CheckedTemplate(basePath = "generictable")
  public static class Templates {
    public static native TemplateInstance tableDialog(TableDialogData data);

    public static native TemplateInstance tableGrid(TableDialogData data);
  }

  @Inject
  ObjectMapper objectMapper;

  DialogRegistry tableRegistry = null;

  public void setTableRegistry(DialogRegistry tableRegistry) {
    this.tableRegistry = tableRegistry;
  }


  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getGenericTable(@QueryParam("dialog") String dialog) {
    Log.info("select generic table dialog: " + dialog);
    return Templates.tableDialog(createTableData(dialog, null, null, null, 20));
  }

  @GET
  @Path("/data")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getTableData( //
                                        @QueryParam("dialog") String dialog, //
                                        @QueryParam("filter") String filter, //
                                        @QueryParam("sortCol") String sortCol, //
                                        @QueryParam("sortDir") String sortCDir, //
                                        @QueryParam("maxRows") Integer maxRows //
  ) {
    Log.info("get table data for " + dialog + " with filter: " + filter + ", sortCol: " + sortCol + ", sortCDir: " + sortCDir + ", maxRows: " + maxRows);
    return Templates.tableGrid(createTableData(dialog, filter, sortCol, sortCDir, maxRows));
  }

  private <T> TableDialogData createTableData(String dialogName, String filter, String sortCol, Object sortDir, Integer maxRows) {
    TableDialogDefinition<T> dialog = tableRegistry.getDialogTableDefinitions(dialogName);
    if (dialog == null) {
      Log.error("Dialog " + dialogName + " not found");
      tableRegistry.getDialogTableDefinitions().forEach(tdd -> Log.info(" - found: " + tdd.dialogMenuPath()));
      throw new IllegalArgumentException("Dialog " + dialogName + " not found");
    }
    TableDialogData tdd = new TableDialogData(dialogName, dialog.dialogResourceDataPath(), objectMapper);
    for (var col : dialog.columns()) {
      tdd.col(col.label(), col.id());
    }
    FilterData filterData = new FilterData(filter);
    Stream<T> dataStream = dialog.dataSource().get();
    dataStream.map(obj -> asMap(obj, dialog)) //
        .filter(obj -> checkFilter(obj, filterData)) //
        .limit(maxRows == null ? 20 : maxRows) //
        .forEach(obj -> addRow(obj, dialog, tdd));
    return tdd;
  }

  record FilterData(String fullTextFilter) {
  }

  private boolean checkFilter(Map<String, String> obj, FilterData filter) {
    return filter.fullTextFilter == null || obj.values().stream().anyMatch(v -> v != null && v.contains(filter.fullTextFilter()));
  }

  private void addRow(Map<String, String> obj, TableDialogDefinition<?> dialog, TableDialogData tdd) {
    List<String> row = new ArrayList<>();
    for (var col : dialog.columns()) {
      row.add(obj.get(col.id()));
    }
    TableRowData rowDef = tdd.row(row);
    for (var col : dialog.details()) {
      rowDef.detail(col.id(), obj.get(col.id()));
    }
    rowDef.jsonDetail(obj.get(JSON_DETAILS_COL_ID));
  }

  private <T> Map<String, String> asMap(T obj, TableDialogDefinition<T> dialog) {
    Map<String, String> res = new HashMap<>();
    for (var col : dialog.columns()) {
      res.put(col.id(), col.getStringAccessor().apply(obj));
    }
    for (var col : dialog.details()) {
      res.put(col.id(), col.getStringAccessor().apply(obj));
    }
    res.put(JSON_DETAILS_COL_ID, dialog.computeJSonDetails(obj, objectMapper));
    return res;
  }
}

