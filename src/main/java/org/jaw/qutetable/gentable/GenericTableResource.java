package org.jaw.qutetable.gentable;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/gentable")
public class GenericTableResource {

  @Inject
  Template dialogGenericTable;
  @Inject
  Template dialogGenericTableGrid;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getGenericTable() {
    List<TableColumnDefinition> columns = new ArrayList<>();
    return dialogGenericTableGrid.data("columns", columns);
  }

}
