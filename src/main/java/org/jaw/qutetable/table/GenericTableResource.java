/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable.table;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jaw.qutetable.DevUiDialogDefinition;
import org.jaw.qutetable.dialogdef.TableDialogDefinition;
import org.jaw.qutetable.table.data.TableDialogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Path("/api/table")
@Singleton
public class GenericTableResource {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @CheckedTemplate(basePath = "webui/")
  public static class Templates {
    public static native TemplateInstance appTableDialog(TableDialogData data);

    public static native TemplateInstance appTableDialogGrid(TableDialogData data);
  }

  @Inject
  DevUiDialogDefinition dialogDefinition;

  //-----------------------------------------------------------------------------------------------------------------
  //-----  TABLE DIALOG HTML
  //-----------------------------------------------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getTableDialogHtml( //
                                             @QueryParam("dialog") String dialog //
  ) {
    log.debug("select generic table dialog: {}", dialog);
    return Templates.appTableDialog(createTableData(dialog, null, null, null, null));
  }

  //-----------------------------------------------------------------------------------------------------------------
  //-----  TABLE DIALOG GRID HTML
  //-----------------------------------------------------------------------------------------------------------------
  @GET
  @Path("/data")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getTableDialogGridHtml( //
                                                 @QueryParam("dialog") String dialog, //
                                                 @QueryParam("filter") String filter, //
                                                 @QueryParam("sortCol") String sortCol, //
                                                 @QueryParam("sortDir") String sortCDir, //
                                                 @QueryParam("maxRows") Integer maxRows //
  ) {
    log.debug("get table data for {} with filter: {}, sortCol: {}, sortCDir: {}, maxRows: {}", dialog, filter, sortCol, sortCDir, maxRows);
    return Templates.appTableDialogGrid(createTableData(dialog, filter, sortCol, sortCDir, maxRows));
  }

  private <T> TableDialogData createTableData(String dialogName, String filterTxt, String sortCol, Object sortDir, Integer maxRows) {
    TableDialogDefinition<T> dialog = dialogDefinition.getTableRegistry().getDialogTableDefinitions(dialogName);
    if (dialog == null) {
      log.error("Dialog {} not found", dialogName);
      dialogDefinition.getTableRegistry().getDialogTableDefinitions().forEach(tdd -> log.info(" - found: {}", tdd.dialogMenuPath()));
      throw new IllegalArgumentException("Dialog " + dialogName + " not found");
    }
    return new GenericTableDataAccess<T>().createTableData(dialog, new GenericTableDataAccess.SearchDefinition(filterTxt, sortCol, sortDir, maxRows), dialogDefinition.getObjectMapper());
  }
}
