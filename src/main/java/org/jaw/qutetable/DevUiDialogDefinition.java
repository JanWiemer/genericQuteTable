/* Copyright (c) SSI Schäfer Software Development GmbH */
package org.jaw.qutetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.jaw.qutetable.app.AppMenuData;
import org.jaw.qutetable.dialogdef.DialogRegistry;
import org.jaw.qutetable.example.CarRepository;
import org.jaw.qutetable.example.Person;
import org.jaw.qutetable.example.PersonRepository;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.function.Predicate;

@Startup
@Singleton
public class DevUiDialogDefinition {

  public static final Set<Class<?>> EXCLUDED_CLASSES = Set.of();
  public static final Predicate<Field> INCLUDE_FIELD_FILTER = f -> !EXCLUDED_CLASSES.contains(f.getType());

  public static final Set<Class<?>> DO_NOT_EXPAND_CLASSES = Set.of(String.class, Character.class, Byte.class, Boolean.class, Date.class, Duration.class);
  public static final Predicate<Field> EXPAND_FIELD_FILTER = f -> !Number.class.isAssignableFrom(f.getType()) && !DO_NOT_EXPAND_CLASSES.contains(f.getType());

  private final PersonRepository personRepo = new PersonRepository();
  private final CarRepository carRepo = new CarRepository();
  private final ObjectMapper objectMapper;
  private DialogRegistry tableRegistry;

  public DevUiDialogDefinition() {
    objectMapper = createDevUiObjectMapper();
  }

  @PostConstruct
  void init() {
    DialogRegistry.Builder reg = DialogRegistry.create("Example QUTE Table Application");
    reg.setFlatFieldDisplayPredicate(f->f.getType().getPackageName().startsWith("org.jaw"));

    reg.add("SYS.THREADS", Thread.class).from(() -> Thread.getAllStackTraces().keySet().stream()) //
            .columns("tid", "name").addAllDetails();

    reg.add("EXAMPLES.PERSONS", Person.class).from(() -> personRepo.getAllPersons().stream()) //
            .columns("firstName", "lastName").addAllDetails();

    reg.add("EXAMPLES.CARS", CarRepository.Car.class).from(() -> carRepo.getCars().stream()) //
            .columns("id", "brand", "model", "engine").addAllDetails();

    tableRegistry = reg.build();
  }

  public ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

  public DialogRegistry getTableRegistry() {
    return tableRegistry;
  }

  public AppMenuData getApplicationMenu() {
    return getTableRegistry().getApplicationMenu();
  }

  private ObjectMapper createDevUiObjectMapper() {
    ObjectMapper m = new ObjectMapper();
    m.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    m.registerModule(new JavaTimeModule());
    m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return m;
  }

}
