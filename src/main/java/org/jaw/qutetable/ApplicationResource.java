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

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getApplication() {
    Log.info("Load Application");
    List<DialogDefinition> dialogs = List.of( //
        new DialogDefinition("User Dialog (Generic)", "/api/table?object=User"), //
        new DialogDefinition("User Dialog", "/api/dialog/user") //
    );
    return application.data("dialogs", dialogs);
  }

}
