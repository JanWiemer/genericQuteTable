package org.jaw.qutetable;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/")
public class ApplicationResource {

  @Inject
  Template application;
  @Inject
  Template tableDialogDataGrid;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getApplication() {
    Log.info("Load Application");
    return application.data("entries", getUserData());
  }

  @GET
  @Path("api/data")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getTableData( //
                                        @QueryParam("filter") String filter, //
                                        @QueryParam("sortCol") String sortCol, //
                                        @QueryParam("sortCDir") String sortCDir //
  ) {
    Log.info("get table data with filter: " + filter + ", sortCol: " + sortCol + ", sortCDir: " + sortCDir);
    return tableDialogDataGrid.data("entries", applyFilter(filter, getUserData()));
  }

  private List<ExampleDataSource.User> applyFilter(String filter, List<ExampleDataSource.User> data) {
    return data.stream().filter(u -> filter == null || u.name().matches(".*" + filter + ".*")).toList();
  }

  private List<ExampleDataSource.User> getUserData() {
    return ExampleDataSource.getUserData();
  }


}
