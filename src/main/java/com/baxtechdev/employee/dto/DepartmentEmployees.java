package com.baxtechdev.employee.dto;

import java.util.List;

/**
 * DTO that captures a department together with the employees assigned to it.
 */
public class DepartmentEmployees {

    private final String department;
    private final List<String> employees;

    /**
     * Creates a new instance.
     *
     * @param department the department name
     * @param employees  the sorted list of employee names belonging to the department
     */
    public DepartmentEmployees(String department, List<String> employees) {
        this.department = department;
        this.employees = employees;
    }

    /**
     * @return the department name
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @return the employee names assigned to the department
     */
    public List<String> getEmployees() {
        return employees;
    }
}
