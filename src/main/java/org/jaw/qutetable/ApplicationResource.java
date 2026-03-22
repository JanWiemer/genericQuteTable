package org.jaw.qutetable;

import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.repository.CarRepository;
import org.jaw.qutetable.example.Person;
import org.jaw.qutetable.example.PersonRepository;
import org.jaw.qutetable.gentable.GenericTableResource;
import org.jaw.qutetable.gentable.definition.TableRegistry;
import org.jaw.qutetable.gentable.definition.TableRegistryBuilder;

@Path("/")
public class ApplicationResource {

  @CheckedTemplate(basePath = "")
  public static class Templates {
    public static native TemplateInstance application(ApplicationMenu menu);
  }

  @Inject
  GenericTableResource genericTableResource;

  private final PersonRepository personRepo = new PersonRepository();
  private final CarRepository carRepo = new CarRepository();

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getApplication() {
    Log.info("Load Application");
    TableRegistryBuilder reg = genericTableResource.createTableRegistryBuilder();
    reg.add("SYS.THREADS", Thread.class).from(() -> Thread.getAllStackTraces().keySet().stream()) //
        .columns("tid", "name").addAllDetails();
    reg.add("EXAMPLES.PERSONS", Person.class).from(() -> personRepo.getAllPersons().stream()) //
        .columns("firstName", "lastName").addAllDetails();
    reg.add("EXAMPLES.CARS", CarRepository.Car.class).from(() -> carRepo.getCars().stream()) //
        .columns("id", "brand", "model").addAllDetails();
    ApplicationMenu appMenu = reg.build().getApplicationMenu();
    return Templates.application(appMenu.build());
  }

}
