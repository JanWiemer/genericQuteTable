package org.jaw.qutetable.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Person {

  public String firstName;
  public String lastName;
  public int age;
  public String email;
  public Address address;
  public Date lastLogin;
  public List<String> groups = new ArrayList<>();

  public Person(String firstName, String lastName, int age, String email, Address address, Date lastLogin) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
    this.email = email;
    this.address = address;
    this.groups = groups;
  }

  public static class Address {
    public String city;
    public String zipCode;
    public String street;
    public int number;

    public Address(String city, String zipCode, String street, int number) {
      this.city = city;
      this.zipCode = zipCode;
      this.street = street;
      this.number = number;
    }
  }

}
