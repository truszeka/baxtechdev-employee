package com.baxtechdev.employee.controller;

import com.baxtechdev.employee.dto.DepartmentEmployees;
import com.baxtechdev.employee.service.EmployeeCatalogService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes endpoints for browsing employees.
 * REST vezérlő, amely végpontokat biztosít a dolgozók böngészéséhez.
 */
@RestController
@RequestMapping(path = "/rest", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployeeController {

    // Logger providing visibility into incoming REST requests.
    // Naplózó, amely láthatóságot biztosít a beérkező REST kérésekhez.
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);
    // Service dependency that performs the employee lookup logic.
    // Szolgáltatás függőség, amely a dolgozók lekérdezési logikáját végzi.
    private final EmployeeCatalogService employeeCatalogService;

    /**
     * Creates a new controller instance.
     * Létrehozza az új vezérlő példányt.
     *
     * @param employeeCatalogService service that provides access to employee data / szolgáltatás, amely hozzáférést biztosít a dolgozói adatokhoz
     */
    public EmployeeController(EmployeeCatalogService employeeCatalogService) {
        // Store the dependency for later use across request handlers.
        // Elmentjük a függőséget a későbbi kéréskezelőkben történő használathoz.
        this.employeeCatalogService = employeeCatalogService;
    }

    /**
     * Lists all unique employees sorted by first and last name.
     * Kilistázza az összes egyedi dolgozót keresztnév és vezetéknév szerint rendezve.
     *
     * @return the sorted list of employee names / a dolgozónevek rendezett listája
     */
    @GetMapping(value = "/employees", params = "!department")
    public List<String> listEmployees() {
        // Delegate to the catalog service for the aggregated list of employees.
        // Átadjuk a katalógus szolgáltatásnak a dolgozók összesített listájának előállítását.
        log.info("Handling request for all employees");
        return employeeCatalogService.getAllEmployeeNames();
    }

    /**
     * Lists employees filtered by department.
     * Kilistázza az osztály szerint szűrt dolgozókat.
     *
     * @param department the department name / az osztály neve
     * @return the sorted list of employees belonging to the department / a megadott osztályhoz tartozó dolgozók rendezett listája
     */
    @GetMapping(value = "/employees", params = "department")
    public List<String> listEmployeesByDepartment(@RequestParam("department") String department) {
        // Let the service handle filtering by department and log the incoming request.
        // A szolgáltatás végzi az osztály szerinti szűrést, és naplózzuk a beérkező kérést.
        log.info("Handling request for employees in department '{}'", department);
        return employeeCatalogService.getEmployeeNamesByDepartment(department);
    }

    /**
     * Lists employees grouped by their department.
     * Kilistázza a dolgozókat osztályok szerint csoportosítva.
     *
     * @return department summaries / osztály összesítések
     */
    @GetMapping("/employees/groupby/department")
    public List<DepartmentEmployees> groupByDepartment() {
        // Ask the service for grouped data to satisfy the endpoint contract.
        // A szolgáltatástól kérünk csoportosított adatokat a végpont szerződésének teljesítéséhez.
        log.info("Handling request for employees grouped by department");
        return employeeCatalogService.getEmployeesGroupedByDepartment();
    }
}
