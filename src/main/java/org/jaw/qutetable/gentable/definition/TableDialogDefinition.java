package org.jaw.qutetable.gentable.definition;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record TableDialogDefinition<T>( //
                                        String dialogMenuPath, //
                                        Class<T> dialogObjectType, //
                                        String dialogResourcePath, //
                                        String dialogResourceDataPath, //
                                        Supplier<Stream<T>> dataSource, //
                                        List<TableDialogColumnDefinition<T>> columns, //
                                        List<TableDialogColumnDefinition<T>> details, //
                                        BiFunction<T, ObjectMapper, String> jsonDetailFunction //
) {

  public TableDialogDefinition(TableDialogDefinitionBuilder<T> b) {
    this(b.dialogMenuPath, b.dialogObjectType, b.dialogResourcePath, b.dialogResourceDataPath, b.dataSource, b.columns, b.details, b.jsonDetailFunction);
  }

  public String computeJSonDetails(T rowData, ObjectMapper objectMapper) {
    return jsonDetailFunction.apply(rowData, objectMapper);
  }

}
