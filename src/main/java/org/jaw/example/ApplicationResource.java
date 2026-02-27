package org.jaw.example;

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
  Template tableDialogDatagrid;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getApplication() {
    Log.info("Load Application");
    return application.data("entries", getData());
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
    return tableDialogDatagrid.data("entries", applyFilter(filter, getData()));
  }

  private List<User> applyFilter(String filter, List<User> data) {
    return data.stream().filter(u -> filter == null || u.name.matches(".*" + filter + ".*")).toList();
  }

  static List<User> getData() {
    return List.of(
        new User("Max Mustermann", "Lead Developer", "max@example.com", "Aktiv"),
        new User("Otto Mumm", "Sekretaer", "mumm@example.com", "Inaktiv"),
        new User("Willi Wutz", "Developer", "wutz1@example.com", "Aktiv"),
        new User("Olli Wutz", "Developer", "wutz2@example.com", "Aktiv"),
        new User("Katrin Knorke", "Developer", "knorke@example.com", "Aktiv"),
        new User("Ludmilla Tun", "Developer", "tun@example.com", "Aktiv"),
        new User("Jan Wiemer", "Architect", "wiemer@example.com", "Aktiv")
    );
  }

  record User(String name, String postion, String eMail, String status) {
  }
}
