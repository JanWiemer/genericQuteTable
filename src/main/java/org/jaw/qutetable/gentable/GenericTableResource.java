package org.jaw.qutetable.gentable;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jaw.qutetable.ExampleDataSource;
import org.jaw.qutetable.gentable.data.TableDialogData;
import org.jaw.qutetable.gentable.data.TableRowData;
import org.jaw.qutetable.gentable.definition.TableDialogDefinition;
import org.jaw.qutetable.gentable.definition.TableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Path("/api/table")
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
    return Templates.tableDialog(createTableData(dialog, null, null, null));
  }

  @GET
  @Path("/data")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getTableData( //
                                        @QueryParam("dialog") String dialog, //
                                        @QueryParam("filter") String filter, //
                                        @QueryParam("tableSortColumnName") String sortCol, //
                                        @QueryParam("tableSortDir") String sortCDir) {
    Log.info("get table data for " + dialog + " with filter: " + filter + ", sortCol: " + sortCol + ", sortCDir: " + sortCDir);
    return Templates.tableGrid(createTableData(dialog, filter, sortCol, sortCDir));
  }

  private <T> TableDialogData createTableData(String dialogName, String filter, String sortCol, Object sortDir) {
    @SuppressWarnings("unchecked")
    TableDialogDefinition<T> dialog = tableRegistry.getDialogTableDefinitions(dialogName);
    TableDialogData tdd = new TableDialogData(dialogName, "/api/table/data", objectMapper);
    if (dialog != null) {
      for (var col : dialog.getColumns()) {
        tdd.col(col.header(), col.id());
      }
      Stream<T> dataStream = dialog.getDataSource().get();
      dataStream.forEach(rowData -> {
        List<String> row = new ArrayList<>();
        for (var col : dialog.getColumns()) {
          row.add(col.getStringAccessor().apply(rowData));
        }
        TableRowData rowDef = tdd.row(row);
        rowDef.detail("FullName", "Hero");
        rowDef.detail("UID", UUID.randomUUID().toString());
      });
    } else {
      tdd.col("Name", "The Name");
      tdd.col("Position", "The Position");
      tdd.col("Mail", "E-Mail Address");
      tdd.col("Status", "Status");
      tdd.col("Age", "Age of the Person");
      ExampleDataSource.getUserData().stream().filter(u -> filter == null || u.name().matches(".*" + filter + ".*")).forEach(u -> {
        TableRowData row = tdd.row(u.name(), u.postion(), u.eMail(), u.status(), "99");
        row.detail("FullName", u.name() + " " + u.postion() + "Hero");
        row.detail("UID", UUID.randomUUID().toString());
      });
    }
    return tdd;
  }


}
