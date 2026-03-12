package org.jaw.qutetable;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jaw.qutetable.example.Person;
import org.jaw.qutetable.example.PersonRepository;
import org.jaw.qutetable.gentable.GenericTableResource;
import org.jaw.qutetable.gentable.definition.TableRegistry;

import java.util.List;

@Path("/")
public class ApplicationResource {

  @Inject
  Template application;


  @Inject
  GenericTableResource genericTableResource;

  private final PersonRepository personRepo = new PersonRepository();

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getApplication() {
    Log.info("Load Application");
    TableRegistry reg = genericTableResource.tableRegistry;
    reg.add("SYS.THREADS", Thread.class).from(() -> Thread.getAllStackTraces().keySet().stream()).addAllFields();
    reg.add("PERSONS", Person.class).from(() -> personRepo.getAllPersons().stream()).addAllFields();

    List<DialogDefinition> dialogs = reg.getDialogDefinitions();
    dialogs.add(new DialogDefinition("User Dialog (Generic)", "/api/table?dialog=User")); // custom dialog
    dialogs.add(new DialogDefinition("User Dialog", "/api/dialog/user")); // custom dialog
    return application.data("dialogs", dialogs);
  }

}
