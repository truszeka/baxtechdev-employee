package com.baxtechdev.employee.controller;

import com.baxtechdev.employee.dto.DepartmentEmployees;
import com.baxtechdev.employee.service.EmployeeCatalogService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes endpoints for browsing employees.
 */
@RestController
@RequestMapping(path = "/rest", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployeeController {

    private final EmployeeCatalogService employeeCatalogService;

    /**
     * Creates a new controller instance.
     *
     * @param employeeCatalogService service that provides access to employee data
     */
    public EmployeeController(EmployeeCatalogService employeeCatalogService) {
        this.employeeCatalogService = employeeCatalogService;
    }

    /**
     * Lists all unique employees sorted by first and last name.
     *
     * @return the sorted list of employee names
     */
    @GetMapping("/employees")
    public List<String> listEmployees() {
        return employeeCatalogService.getAllEmployeeNames();
    }

    /**
     * Lists employees filtered by department.
     *
     * @param department the department name
     * @return the sorted list of employees belonging to the department
     */
    @GetMapping(value = "/employees", params = "department")
    public List<String> listEmployeesByDepartment(@RequestParam("department") String department) {
        return employeeCatalogService.getEmployeeNamesByDepartment(department);
    }

    /**
     * Lists employees grouped by their department.
     *
     * @return department summaries
     */
    @GetMapping("/employees/groupby/department")
    public List<DepartmentEmployees> groupByDepartment() {
        return employeeCatalogService.getEmployeesGroupedByDepartment();
    }
}
