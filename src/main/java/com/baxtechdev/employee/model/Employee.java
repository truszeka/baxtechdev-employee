package com.baxtechdev.employee.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an employee enriched with the departments they belong to.
 */
public class Employee {

    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final Set<String> departments = new HashSet<>();

    /**
     * Creates a new {@link Employee} instance.
     *
     * @param firstName  the employee's first name
     * @param lastName   the employee's last name
     */
    public Employee(String firstName, String lastName) {
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        this.fullName = firstName + " " + lastName;
    }

    /**
     * Adds a department to the employee.
     *
     * @param department the department to add
     */
    public void addDepartment(String department) {
        if (department != null && !department.isBlank()) {
            departments.add(department.trim());
        }
    }

    /**
     * @return the employee's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the employee's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the employee's full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return an immutable view of the departments this employee belongs to
     */
    public Set<String> getDepartments() {
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
        return fullName.equals(employee.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }
}
