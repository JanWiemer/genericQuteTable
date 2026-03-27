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
import java.util.List;
import java.util.stream.Stream;

@Path("/api/table")
@Singleton
public class GenericTableResource {

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
    Stream<T> dataStream = dialog.dataSource().get();
    dataStream.limit(maxRows == null ? 20 : maxRows).forEach(rowData -> {
      List<String> row = new ArrayList<>();
      for (var col : dialog.columns()) {
        row.add(col.getStringAccessor().apply(rowData));
      }
      TableRowData rowDef = tdd.row(row);
      for (var col : dialog.details()) {
        rowDef.detail(col.id(), col.getStringAccessor().apply(rowData));
      }
      rowDef.jsonDetail(dialog.computeJSonDetails(rowData, objectMapper));
    });
    return tdd;
  }


}
