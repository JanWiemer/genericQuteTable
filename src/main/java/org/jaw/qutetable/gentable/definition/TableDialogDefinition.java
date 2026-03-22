package org.jaw.qutetable.gentable.definition;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record TableDialogDefinition<T>( //
                                        String dialogMenuPath, //
                                        Class<T> dialogObjectType, //
                                        String dialogResourcePath, //
                                        String dialogResourceDataPath, //
                                        Supplier<Stream<T>> dataSource, //
                                        List<TableDialogColumnDefinition<T>> columns, //
                                        List<TableDialogColumnDefinition<T>> details //
) {
  public TableDialogDefinition(TableDialogDefinitionBuilder<T> b) {
    this(b.dialogMenuPath, b.dialogObjectType, b.dialogResourcePath, b.dialogResourceDataPath, b.dataSource, b.columns, b.details);
  }
}
