package com.baxtechdev.employee.dto;

import java.util.List;

/**
 * DTO that captures a department together with the employees assigned to it.
 * DTO, amely egy osztályt és a hozzá tartozó dolgozókat tartalmazza.
 */
public class DepartmentEmployees {

    // Department name this DTO represents.
    // Az a osztály név, amelyet ez a DTO képvisel.
    private final String department;
    // Sorted list of employee names belonging to the department.
    // Az osztályhoz tartozó dolgozónevek rendezett listája.
    private final List<String> employees;

    /**
     * Creates a new instance.
     * Létrehoz egy új példányt.
     *
     * @param department the department name / az osztály neve
     * @param employees  the sorted list of employee names belonging to the department / az osztályhoz tartozó dolgozónevek rendezett listája
     */
    public DepartmentEmployees(String department, List<String> employees) {
        // Store the provided data directly; the service guarantees immutability.
        // Az átadott adatokat közvetlenül tároljuk; a szolgáltatás biztosítja a változtathatatlanságot.
        this.department = department;
        this.employees = employees;
    }

    /**
     * @return the department name / az osztály neve
     */
    public String getDepartment() {
        // Return the department identifier exactly as supplied.
        // Pontosan úgy adjuk vissza az osztály azonosítót, ahogy kaptuk.
        return department;
    }

    /**
     * @return the employee names assigned to the department / az osztályhoz rendelt dolgozónevek
     */
    public List<String> getEmployees() {
        // Expose the employee list for serialization by the REST layer.
        // A dolgozók listáját az REST réteg szerializációjához tesszük elérhetővé.
        return employees;
    }
}
