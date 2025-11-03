package com.baxtechdev.employee.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.baxtechdev.employee.dto.DepartmentEmployees;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EmployeeCatalogService}.
 * Egységtesztek az {@link EmployeeCatalogService} osztályhoz.
 */
class EmployeeCatalogServiceTest {

    // Service instance under test.
    // A tesztelt szolgáltatás példánya.
    private EmployeeCatalogService service;

    @BeforeEach
    void setUp() {
        // Create a fresh service before each test to ensure isolation.
        // Minden teszt előtt új szolgáltatást hozunk létre az elszigeteltség biztosítására.
        service = new EmployeeCatalogService();
    }

    @Test
    @DisplayName("Should list all unique employees sorted by first and last name")
    void shouldListAllEmployees() {
        // Execute the listing logic without filters.
        // Szűrők nélkül futtatjuk a listázási logikát.
        List<String> names = service.getAllEmployeeNames();

        // Verify the expected order and uniqueness of the returned names.
        // Ellenőrizzük a visszaadott nevek elvárt sorrendjét és egyediségét.
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
        // Retrieve the employees per department to validate filtering behaviour.
        // Osztályonként kérjük le a dolgozókat a szűrési működés ellenőrzéséhez.
        List<String> finance = service.getEmployeeNamesByDepartment("finance");
        List<String> it = service.getEmployeeNamesByDepartment("it");

        // Assert the finance department membership ordering.
        // Ellenőrizzük a pénzügyi osztály tagságának sorrendjét.
        assertThat(finance)
                .containsExactly(
                        "Dale Miller",
                        "Joanne Olsen",
                        "Michael Smith",
                        "Peter Goeking");

        // Assert the IT department membership ordering.
        // Ellenőrizzük az IT osztály tagságának sorrendjét.
        assertThat(it)
                .containsExactly(
                        "Michael Smith",
                        "Peter Goeking",
                        "Samuel Palmisano");
    }

    @Test
    @DisplayName("Should return empty list when department parameter is null")
    void shouldHandleNullDepartment() {
        // Execute the filter with a null department to mirror invalid client input.
        // Null osztály paraméterrel futtatjuk a szűrőt az érvénytelen kliens bemenet modellezésére.
        List<String> result = service.getEmployeeNamesByDepartment(null);

        // Expect no employees to be returned when the department filter is absent.
        // Arra számítunk, hogy nem kapunk dolgozókat, ha az osztályszűrő hiányzik.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when department parameter is blank")
    void shouldHandleBlankDepartment() {
        // Exercise the department filter with whitespace-only input.
        // Csak szóközöket tartalmazó bemenettel vizsgáljuk a szűrőt.
        List<String> result = service.getEmployeeNamesByDepartment("   ");

        // Confirm that a blank department is treated as invalid and yields no matches.
        // Megerősítjük, hogy az üres osztály érvénytelennek számít és nem ad találatot.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when department does not exist")
    void shouldHandleUnknownDepartment() {
        // Request employees for a department that is not present in the dataset.
        // Olyan osztály dolgozóit kérjük le, amely nem szerepel az adathalmazban.
        List<String> result = service.getEmployeeNamesByDepartment("legal");

        // Validate that an unknown department yields no entries rather than throwing.
        // Ellenőrizzük, hogy az ismeretlen osztály üres listát adjon vissza kivétel helyett.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should group employees by department")
    void shouldGroupByDepartment() {
        // Request the grouped employee representation.
        // Lekérjük a csoportosított dolgozói reprezentációt.
        List<DepartmentEmployees> grouped = service.getEmployeesGroupedByDepartment();

        // Validate the department ordering in the result set.
        // Ellenőrizzük az osztályok sorrendjét az eredményhalmazban.
        assertThat(grouped)
                .extracting(DepartmentEmployees::getDepartment)
                .containsExactly("finance", "it", "packaging");

        // Confirm finance grouping contents.
        // Jóváhagyjuk a pénzügyi csoport tartalmát.
        assertThat(grouped.get(0).getEmployees())
                .containsExactly(
                        "Dale Miller",
                        "Joanne Olsen",
                        "Michael Smith",
                        "Peter Goeking");
        // Confirm IT grouping contents.
        // Jóváhagyjuk az IT csoport tartalmát.
        assertThat(grouped.get(1).getEmployees())
                .containsExactly(
                        "Michael Smith",
                        "Peter Goeking",
                        "Samuel Palmisano");
        // Confirm packaging grouping contents.
        // Jóváhagyjuk a csomagolási csoport tartalmát.
        assertThat(grouped.get(2).getEmployees())
                .containsExactly(
                        "Dale Miller",
                        "James Doyle");
    }
}
