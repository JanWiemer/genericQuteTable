package org.jaw.qutetable.gentable;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jaw.qutetable.ExampleDataSource;

@Path("/api/table")
public class GenericTableResource {

  @Inject
  Template dialogGenericTable;
  @Inject
  Template dialogGenericTableGrid;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getGenericTable(@QueryParam("object") String object) {
    Log.info("get generic table: " + object);
    TableDialogData tdd = createTableData(object, null, null, null);
    return dialogGenericTable.data("data", tdd);
  }

  @GET
  @Path("/data")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getTableData( //
                                        @QueryParam("object") String object, //
                                        @QueryParam("filter") String filter, //
                                        @QueryParam("tableSortColumnName") String sortCol, //
                                        @QueryParam("tableSortDir") String sortCDir) {
    Log.info("get table data for " + object + " with filter: " + filter + ", sortCol: " + sortCol + ", sortCDir: " + sortCDir);
    TableDialogData tdd = createTableData(object, filter, sortCol, sortCDir);
    return dialogGenericTableGrid.data("data", tdd);
  }

  @Nonnull
  private TableDialogData createTableData(String object, String filter, String sortCol, Object sortDir) {
    TableDialogData tdd = new TableDialogData("Example Table", "/api/table/data");
    tdd.col("Name", "The Name");
    tdd.col("Position", "The Position");
    tdd.col("Mail", "E-Mail Address");
    tdd.col("Status", "Status");
    tdd.col("Age", "Age of the Person");
    ExampleDataSource.getUserData().stream().filter(u -> filter == null || u.name().matches(".*" + filter + ".*")).forEach(u -> {
      tdd.row(u.name(), u.postion(), u.eMail(), u.status(), "99");
    });
    return tdd;
  }


}
