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
import org.jaw.qutetable.gentable.data.TableDialogData;
import org.jaw.qutetable.gentable.data.TableRowData;
import org.jaw.qutetable.gentable.definition.TableDialogDefinition;
import org.jaw.qutetable.gentable.definition.TableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Path("/api/table")
@Singleton
public class GenericTableResource {

  @CheckedTemplate(basePath = "generictable")
  public static class Templates {
    public static native TemplateInstance tableDialog(TableDialogData data);

    public static native TemplateInstance tableGrid(TableDialogData data);
  }

  public final TableRegistry tableRegistry = new TableRegistry();

  @Inject
  ObjectMapper objectMapper;

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
    @SuppressWarnings("unchecked")
    TableDialogDefinition<T> dialog = tableRegistry.getDialogTableDefinitions(dialogName);
    if (dialog == null) {
      Log.error("Dialog " + dialogName + " not found");
      tableRegistry.getDialogTableDefinitions().forEach(tdd -> Log.info(" - found: " + tdd.getDialogMenuPath()));
      throw new IllegalArgumentException("Dialog " + dialogName + " not found");
    }
    TableDialogData tdd = new TableDialogData(dialogName, dialog.getDialogResourceDataPath(), objectMapper);
    for (var col : dialog.getColumns()) {
      tdd.col(col.header(), col.id());
    }
    Stream<T> dataStream = dialog.getDataSource().get();
    dataStream.limit(maxRows== null ? 20 : maxRows).forEach(rowData -> {
      List<String> row = new ArrayList<>();
      for (var col : dialog.getColumns()) {
        row.add(col.getStringAccessor().apply(rowData));
      }
      TableRowData rowDef = tdd.row(row);
      for (var col : dialog.getColumns()) {
        rowDef.detail(col.id(), col.getStringAccessor().apply(rowData));
      }
    });
    return tdd;
  }


}
