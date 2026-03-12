package org.jaw.qutetable.gentable;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.logging.Log;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.core.MultivaluedMap;

public class OLDGenericTableDataAccess {
/*
  private final String dataPath;
  private int maxNumberOfElements;
  private int registeredClassSequence = 0;

  private final Map<String, GenericClassData<?>> registeredClasses = new HashMap<>();

  public OLDGenericTableDataAccess( String dataPath, int maxNumberOfElements) {
    this.dataPath = dataPath;
    this.maxNumberOfElements = maxNumberOfElements;
  }

  public String getDataPath() {
    return dataPath;
  }

  public <T> GenericClassData<T> registerClass(String name, Class<T> type, Supplier<List<T>> dataSource) {
    return registerClassByStream(name, type, () -> dataSource.get().stream());
  }

  public <T> GenericClassData<T> registerClassByStream(String name, Class<T> type, Supplier<Stream<T>> dataSource) {
    GenericClassData<T> classData = new GenericClassData<>(++registeredClassSequence, name, type, dataSource);
    registeredClasses.put(name, classData);
    return classData;
  }

  public TemplateInstance getMainView(Template template) {
    List<String> classList = registeredClasses.values().stream().sorted().map(cd -> cd.typeName).collect(Collectors.toList());
    return template.data("classes", classList).data("data_path", dataPath);
  }

  public TemplateInstance getTable(String typeName, int numberOfResults, MultivaluedMap<String, String> searchParams) {
    if (numberOfResults > 0) {
      maxNumberOfElements = numberOfResults;
    }
    GenericClassData<?> typeData = registeredClasses.get(typeName);
    if (typeData == null) {
      if (typeName != null) {
        return quteEngine.parse("Unknown Type: " + typeName).instance();
      } else {
        return quteEngine.parse("No Type Selected...").instance();
      }
    }
    TemplateInstance template = quteEngine.parse(tableTemplate(typeData)).instance();
    template.data("className", typeData.type);
    template.data("data_path", dataPath);
    // process filter data
    for (FieldData field : typeData.getFields()) {
      List<String> searchPatternValList = searchParams.get("search." + field.name);
      String searchPatternVal = searchPatternValList == null ? "" : searchPatternValList.getFirst();
      template.data("pattern_" + field.name, searchPatternVal);
    }
    // process sort data
    String toggleSortField = searchParams.get("sort") == null ? null : searchParams.get("sort").getFirst();
    String sortField = null;
    boolean sortFielsAsc = true;
    for (FieldData field : typeData.getFields()) {
      List<String> sortValList = searchParams.get("sort." + field.name);
      String sortValStr = sortValList == null || sortValList.isEmpty() ? "-" : sortValList.getFirst();
      Boolean ascending = null;
      if (sortValStr != null && sortValStr.contains(">")) {
        ascending = true;
      } else if (sortValStr != null && sortValStr.contains("<")) {
        ascending = false;
      }
      if (toggleSortField != null) {
        if (toggleSortField.equals(field.name)) {
          if (ascending == null) {
            ascending = true;
          } else if (ascending) {
            ascending = false;
          } else {
            ascending = null;
          }
        } else {
          ascending = null;
        }
      }
      if (ascending != null) {
        sortField = field.name;
        sortFielsAsc = ascending;
      }
      sortValStr = ascending == null ? "-" : ascending ? ">" : "<";
      template.data("sort_" + field.name, sortValStr);
    }
    return setTemplateData(template, typeData, searchParams, sortField, sortFielsAsc);
  }

  private TemplateInstance setTemplateData(TemplateInstance template, GenericClassData<?> typeData, MultivaluedMap<String, String> searchParams, String sortField, boolean sortFielsAsc) {
    Stream<?> dataStream = typeData.dataSource.get();
    dataStream = addFilterAndSortCriteria(dataStream, typeData, searchParams, sortField, sortFielsAsc);
    int limit = Math.max(100, 2 * maxNumberOfElements);
    dataStream = dataStream.limit(limit + 1);
    int cnt[] = new int[1];
    List<Map<String, String>> dataMapList = new ArrayList<>();
    dataStream.forEach(data -> {
      cnt[0]++;
      if (dataMapList.size() < maxNumberOfElements) {
        Map<String, String> objData = new HashMap<>();
        for (FieldData fieldData : typeData.fields) {
          objData.put(fieldData.name, fieldData.getStringAccessor().apply(data));
        }
        dataMapList.add(objData);
      }
    });
    String totalResults = (cnt[0] > limit ? ">" : "") + cnt[0];
    template.data("inputs", dataMapList);
    template.data("numberOfResultsTotal", totalResults);
    template.data("numberOfResultsDisplayed", dataMapList.size());
    return template;
  }

  private Stream<?> addFilterAndSortCriteria(Stream<?> dataStream, GenericClassData<?> typeData, MultivaluedMap<String, String> searchParams, String sortField, boolean sortFielsAsc) {
    for (FieldData fieldData : typeData.getFields()) {
      Function<Object, String> fieldAccessor = fieldData.getStringAccessor();
      List<String> searchPatternList = searchParams.get("search." + fieldData.name);
      if (searchPatternList != null && searchPatternList.size() == 1) {
        String pattern = searchPatternList.getFirst();
        String searchPattern = pattern == null ? ".*" : ".*" + pattern + ".*";
        dataStream = dataStream.filter(o -> fieldAccessor.apply(o).matches(searchPattern));
      }
    }
    if (sortField != null) {
      if (sortFielsAsc) {
        dataStream = dataStream.sorted(typeData.getField(sortField).comparator);
      } else {
        dataStream = dataStream.sorted(typeData.getField(sortField).comparator.reversed());
      }
    }
    return dataStream;
  }
*/
}