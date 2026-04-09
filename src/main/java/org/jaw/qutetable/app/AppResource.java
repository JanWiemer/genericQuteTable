/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.app;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jaw.qutetable.DevUiDialogDefinition;

@Path("/devui")
public class AppResource {

  @Inject
  DevUiDialogDefinition dialogDefinition;

  @CheckedTemplate(basePath = "webui/")
  public static class Templates {
    public static native TemplateInstance app(AppData appData);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getApplication() {
    return Templates.app(dialogDefinition.getTableRegistry().getApplicationData());
  }

}
