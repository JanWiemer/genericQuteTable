package org.jaw.qutetable.gentable.tabledata;

import java.time.Duration;

public class DoFormat {
  public static String duration(Duration duration) {
    return duration == null ? "-" : duration.toString();
  }
}
