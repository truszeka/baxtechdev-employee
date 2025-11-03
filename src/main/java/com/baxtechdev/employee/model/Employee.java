package com.baxtechdev.employee.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an employee enriched with the departments they belong to.
 * Egy dolgozót reprezentál a hozzá tartozó osztályokkal.
 */
public class Employee {

    // First name portion of the employee identity.
    // A dolgozó személyazonosságának keresztnév része.
    private final String firstName;
    // Last name portion of the employee identity.
    // A dolgozó személyazonosságának vezetéknév része.
    private final String lastName;
    // Combined first and last name cached for quick access.
    // A keresztnév és vezetéknév kombinációja gyors eléréshez tárolva.
    private final String fullName;
    // Mutable set of departments that the employee belongs to.
    // A dolgozóhoz tartozó osztályok módosítható halmaza.
    private final Set<String> departments = new HashSet<>();

    /**
     * Creates a new {@link Employee} instance.
     * Létrehoz egy új {@link Employee} példányt.
     *
     * @param firstName  the employee's first name / a dolgozó keresztneve
     * @param lastName   the employee's last name / a dolgozó vezetékneve
     */
    public Employee(String firstName, String lastName) {
        // Capture the validated name components.
        // Rögzítjük az ellenőrzött névkomponenseket.
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        // Precompute the full name string as it is needed frequently.
        // Előre kiszámítjuk a teljes név szövegét, mivel gyakran szükség van rá.
        this.fullName = firstName + " " + lastName;
    }

    /**
     * Adds a department to the employee.
     * Osztályt ad a dolgozóhoz.
     *
     * @param department the department to add / a hozzáadandó osztály
     */
    public void addDepartment(String department) {
        if (department != null && !department.isBlank()) {
            // Trim whitespace to avoid duplicated department entries.
            // Levágjuk a szóközöket, hogy elkerüljük a duplikált osztály bejegyzéseket.
            departments.add(department.trim());
        }
    }

    /**
     * @return the employee's first name / a dolgozó keresztneve
     */
    public String getFirstName() {
        // Return the cached first name as stored at construction time.
        // Visszaadjuk a konstrukciókor eltárolt keresztnevet.
        return firstName;
    }

    /**
     * @return the employee's last name / a dolgozó vezetékneve
     */
    public String getLastName() {
        // Return the cached last name value.
        // Visszaadjuk az eltárolt vezetéknév értéket.
        return lastName;
    }

    /**
     * @return the employee's full name / a dolgozó teljes neve
     */
    public String getFullName() {
        // Provide the precomputed full name for quick concatenation-free access.
        // Gyors, összefűzés nélküli eléréshez adjuk vissza az előre kiszámított teljes nevet.
        return fullName;
    }

    /**
     * @return an immutable view of the departments this employee belongs to / a dolgozóhoz tartozó osztályok nem módosítható nézete
     */
    public Set<String> getDepartments() {
        // Protect internal state by exposing an immutable view only.
        // Belső állapotunk védelme érdekében csak nem módosítható nézetet adunk vissza.
        return Collections.unmodifiableSet(departments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Employee employee = (Employee) o;
        // Equality is based solely on the combined full name to align with deduplication rules.
        // Az egyenlőséget kizárólag a teljes név alapján határozzuk meg, igazodva a duplikációkezeléshez.
        return fullName.equals(employee.fullName);
    }

    @Override
    public int hashCode() {
        // Derive the hash from the full name to match the equals implementation.
        // A hash értéket a teljes névből képezzük, hogy egyezzen az equals megvalósítással.
        return Objects.hash(fullName);
    }
}
