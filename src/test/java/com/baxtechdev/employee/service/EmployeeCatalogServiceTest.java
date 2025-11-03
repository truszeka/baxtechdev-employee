package com.baxtechdev.employee.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.baxtechdev.employee.dto.DepartmentEmployees;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EmployeeCatalogService}.
 */
class EmployeeCatalogServiceTest {

    private EmployeeCatalogService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeCatalogService();
    }

    @Test
    @DisplayName("Should list all unique employees sorted by first and last name")
    void shouldListAllEmployees() {
        List<String> names = service.getAllEmployeeNames();

        assertThat(names)
                .containsExactly(
                        "Dale Miller",
                        "George Smith",
                        "James Doyle",
                        "Joanne Olsen",
                        "Michael Smith",
                        "Peter Goeking",
                        "Samuel Palmisano");
    }

    @Test
    @DisplayName("Should filter employees by department")
    void shouldFilterByDepartment() {
        List<String> finance = service.getEmployeeNamesByDepartment("finance");
        List<String> it = service.getEmployeeNamesByDepartment("it");

        assertThat(finance)
                .containsExactly(
                        "Dale Miller",
                        "Joanne Olsen",
                        "Michael Smith",
                        "Peter Goeking");

        assertThat(it)
                .containsExactly(
                        "Michael Smith",
                        "Peter Goeking",
                        "Samuel Palmisano");
    }

    @Test
    @DisplayName("Should group employees by department")
    void shouldGroupByDepartment() {
        List<DepartmentEmployees> grouped = service.getEmployeesGroupedByDepartment();

        assertThat(grouped)
                .extracting(DepartmentEmployees::getDepartment)
                .containsExactly("finance", "it", "packaging");

        assertThat(grouped.get(0).getEmployees())
                .containsExactly(
                        "Dale Miller",
                        "Joanne Olsen",
                        "Michael Smith",
                        "Peter Goeking");
        assertThat(grouped.get(1).getEmployees())
                .containsExactly(
                        "Michael Smith",
                        "Peter Goeking",
                        "Samuel Palmisano");
        assertThat(grouped.get(2).getEmployees())
                .containsExactly(
                        "Dale Miller",
                        "James Doyle");
    }
}
