package org.jaw.qutetable.example;

import org.jaw.qutetable.example.Person.Address;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class PersonRepository {

  private final List<Person> personList = new ArrayList<>();

  private void add(String firstName, String lastName, int age, String email, Address address, Date lastLogin) {
    personList.add(new Person(firstName, lastName, age, email, address, lastLogin));
  }

  private Address addr(String city, String zipCode, String street, int number) {
    return new Address(city, zipCode, street, number);
  }

  public PersonRepository() {
    IntStream.range(1,100).forEach(i -> {
      add("Mad", "Max"+i, 12+i, "max@example.com", addr("Rome", "007", "Main Avenue", 42+i), new Date());
    });
  }

  public List<Person> getAllPersons() {
    return personList;
  }
}
