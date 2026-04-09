package org.jaw.qutetable.example;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@ApplicationScoped
public class CarRepository {

  public static void main(String[] args) {
    CarRepository repo = new CarRepository();
    for (Car car : repo.getCars()) {
      System.out.println(car.toString());
    }
  }


  private final List<Car> cars = new ArrayList<>();

  public CarRepository() {
    generateTestData();
  }
  public List<Car> getCars() {
    return cars;
  }
  private void generateTestData() {
    String[] brands = {"Tesla", "BMW", "Audi", "Mercedes", "Volkswagen", "Ford", "Porsche", "Toyota"};
    String[] models = {"Model 3", "M3", "A4", "C-Class", "Golf", "Mustang", "911", "Corolla"};
    FuelType[] fuelTypes = FuelType.values();
    Random random = new Random();
    for (int i = 1; i <= 200; i++) {
      FuelType fuel = fuelTypes[i % fuelTypes.length];
      Engine engine = new Engine(fuel, 150 + (i % 300));
      List history = new ArrayList<>();
      history.add(new ServiceHistory(LocalDate.now().minusMonths(i), "Routine Checkup"));
      if (i % 2 == 0) {
        history.add(new ServiceHistory(LocalDate.now().minusDays(i), "Brake Inspection"));
      }
      Car car = new Car(
          (long) i,
          brands[i % brands.length],
          models[i % models.length] + " Mark " + (i % 4 + 1),
          2015 + (i % 9),
          30000.0 + (i * 150.5),
          i % 5 != 0, // Boolean Beispiel
          LocalDate.now().minusYears(i % 5).minusDays(i),
          engine,
          history
      );
      cars.add(car);
    }
  }

  public enum FuelType {
    ELECTRIC, PETROL, DIESEL, HYBRID
  }
  public static class Car {
    private Long id;
    private String brand;
    private String model;
    private Integer manufactureYear;
    private Double price;
    private Boolean isAvailable;
    private LocalDate registrationDate;
    private Engine engine;
    private List serviceHistory; // Generische Liste
    public Car(Long id, String brand, String model, Integer manufactureYear, Double price,
               Boolean isAvailable, LocalDate registrationDate, Engine engine,
               List serviceHistory) {
      this.id = id;
      this.brand = brand;
      this.model = model;
      this.manufactureYear = manufactureYear;
      this.price = price;
      this.isAvailable = isAvailable;
      this.registrationDate = registrationDate;
      this.engine = engine;
      this.serviceHistory = serviceHistory;
    }
    public Long getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Integer getManufactureYear() { return manufactureYear; }
    public Double getPrice() { return price; }
    public Boolean getIsAvailable() { return isAvailable; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public Engine getEngine() { return engine; }
    public List getServiceHistory() { return serviceHistory; }
    @Override
    public String toString() {
      return "Car #" + id + ": " + brand + " " + model + " [" + engine + "], Price: " + price +
          "$, Available: " + isAvailable + ", History-Entries: " + serviceHistory.size();
    }
  }
  public static class Engine {
    private FuelType fuelType;
    private Integer horsepower;
    public Engine(FuelType fuelType, Integer horsepower) {
      this.fuelType = fuelType;
      this.horsepower = horsepower;
    }
    public FuelType getFuelType() { return fuelType; }
    public Integer getHorsepower() { return horsepower; }
    @Override
    public String toString() {
      return fuelType + " (" + horsepower + " HP)";
    }
  }
  public static class ServiceHistory {
    private LocalDate serviceDate;
    private String description;
    public ServiceHistory(LocalDate serviceDate, String description) {
      this.serviceDate = serviceDate;
      this.description = description;
    }
    public LocalDate getServiceDate() { return serviceDate; }
    public String getDescription() { return description; }
    @Override
    public String toString() {
      return serviceDate + ": " + description;
    }
  }
}
