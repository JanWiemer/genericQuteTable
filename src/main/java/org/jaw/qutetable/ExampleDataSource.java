package org.jaw.qutetable;

import java.util.List;

public class ExampleDataSource {

  static List<User> getUserData() {
    return List.of(
        new User("Max Mustermann", 30, "Lead Developer", "max@example.com", "Aktiv"),
        new User("Otto Mumm", 19, "Secretary", "mumm@example.com", "Inaktiv"),
        new User("Willi Wutz", 23, "Developer", "wutz1@example.com", "Aktiv"),
        new User("Olli Wutz", 25, "Developer", "wutz2@example.com", "Aktiv"),
        new User("Katrin Knorke", 45, "Developer", "knorke@example.com", "Aktiv"),
        new User("Ludmilla Tun", 61, "Developer", "tun@example.com", "Aktiv")
    );
  }

  record User(String name, int age, String postion, String eMail, String status) {
  }

}
