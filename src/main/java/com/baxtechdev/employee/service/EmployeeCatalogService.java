package com.baxtechdev.employee.service;

import com.baxtechdev.employee.dto.DepartmentEmployees;
import com.baxtechdev.employee.model.Employee;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Loads employees from the XML resource and exposes methods to access them in the
 * different aggregations required by the REST API.
 * Betölti a dolgozókat az XML erőforrásból, és biztosítja a REST API által igényelt különböző aggregációkat.
 */
@Service
public class EmployeeCatalogService {

    // Logger used to provide operational insight into catalog processing.
    // Naplózó, amely műveleti betekintést ad a katalógus feldolgozásába.
    private static final Logger log = LoggerFactory.getLogger(EmployeeCatalogService.class);
    // Comparator that provides case-insensitive ordering by first and last name.
    // Összehasonlító, amely kis- és nagybetű függetlenül rendez keresztnév és vezetéknév szerint.
    private static final Comparator<Employee> EMPLOYEE_NAME_COMPARATOR =
            Comparator.comparing(Employee::getFirstName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Employee::getLastName, String.CASE_INSENSITIVE_ORDER);

    // Immutable snapshot of the employees loaded from the XML resource.
    // Az XML erőforrásból betöltött dolgozók változtathatatlan pillanatképe.
    private final List<Employee> employees;

    /**
     * Instantiates the service and loads the XML file from the classpath.
     * Példányosítja a szolgáltatást, és betölti az XML fájlt az osztályútvonalról.
     *
     * @throws IllegalStateException if the XML file cannot be parsed / ha az XML fájl nem dolgozható fel
     */
    public EmployeeCatalogService() {
        // Load the employees once at construction time to avoid re-reading the XML file.
        // Betöltjük a dolgozókat egyszer a konstrukció során, hogy elkerüljük az XML többszöri olvasását.
        log.info("Loading employee records from XML resource");
        this.employees = loadEmployees();
        log.info("Loaded {} unique employee records", employees.size());
    }

    /**
     * @return all employee names sorted by first and last name / az összes dolgozónév keresztnév és vezetéknév szerinti rendezésben
     */
    public List<String> getAllEmployeeNames() {
        // Sort the employees each time to guarantee consistent ordering in responses.
        // Minden alkalommal rendezzük a dolgozókat, hogy a válaszok konzisztens sorrendben érkezzenek.
        log.debug("Listing all employees without department filtering");
        return employees.stream()
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .map(Employee::getFullName)
                .toList();
    }

    /**
     * Returns employee names that belong to the provided department.
     * Visszaadja a megadott osztályhoz tartozó dolgozóneveket.
     *
     * @param department the department to filter by / az osztály, amelyre szűrni kell
     * @return the sorted list of employee names in the department / az osztályban lévő dolgozónevek rendezett listája
     */
    public List<String> getEmployeeNamesByDepartment(String department) {
        // Short-circuit when no department was provided to avoid unnecessary work.
        // Rövidre zárjuk a feldolgozást, ha nem adtak meg osztályt, hogy elkerüljük a felesleges munkát.
        if (department == null || department.isBlank()) {
            log.debug("No department provided, returning empty list");
            return List.of();
        }
        // Normalize department names to a common lowercase format before comparisons.
        // Normalizáljuk az osztályneveket közös kisbetűs formátumra az összehasonlítás előtt.
        String normalized = department.trim().toLowerCase(Locale.ROOT);
        log.debug("Listing employees for department '{}'", normalized);
        return employees.stream()
                .filter(employee -> containsDepartment(employee, normalized))
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .map(Employee::getFullName)
                .toList();
    }

    /**
     * Checks whether an employee belongs to a specific department.
     * Ellenőrzi, hogy a dolgozó egy adott osztályhoz tartozik-e.
     */
    private boolean containsDepartment(Employee employee, String normalizedDepartment) {
        // Convert department names for the employee to lowercase to match the normalized filter.
        // A dolgozó osztályneveit kisbetűre alakítjuk, hogy illeszkedjenek a normalizált szűrőhöz.
        return employee.getDepartments().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(name -> name.equals(normalizedDepartment));
    }

    /**
     * Groups employees by their departments.
     * Csoportosítja a dolgozókat az osztályaik szerint.
     *
     * @return department summaries with sorted employee names / osztály összesítések rendezett dolgozónevekkel
     */
    public List<DepartmentEmployees> getEmployeesGroupedByDepartment() {
        // Maintain a case-insensitive key order to stabilise department ordering.
        // Kis- és nagybetűt nem érzékeny kulcssorrendet tartunk fenn a stabil osztálysorrend érdekében.
        Map<String, List<Employee>> grouped = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Employee employee : employees) {
            // Associate each employee with every department they belong to.
            // Minden dolgozót összekapcsolunk az összes osztállyal, amelyhez tartozik.
            for (String department : employee.getDepartments()) {
                grouped.computeIfAbsent(department, key -> new ArrayList<>()).add(employee);
            }
        }
        log.debug("Grouping employees across {} departments", grouped.size());
        return grouped.entrySet().stream()
                .map(entry -> new DepartmentEmployees(
                        entry.getKey(),
                        entry.getValue().stream()
                                // Sort employees within each department for deterministic output.
                                // Rendezjük a dolgozókat osztályonként a determinisztikus kimenet érdekében.
                                .sorted(EMPLOYEE_NAME_COMPARATOR)
                                .map(Employee::getFullName)
                                .toList()))
                .sorted(Comparator.comparing(DepartmentEmployees::getDepartment, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Reads the employee XML file from the classpath and builds the employee list.
     * Beolvassa az osztályútvonalról az employee XML fájlt, és felépíti a dolgozói listát.
     */
    private List<Employee> loadEmployees() {
        ClassPathResource resource = new ClassPathResource("employee.xml");
        // Instantiate a dedicated XML mapper for translating structured data into objects.
        // Létrehozunk egy dedikált XML mappert a strukturált adatok objektummá alakításához.
        XmlMapper xmlMapper = new XmlMapper();
        try (InputStream inputStream = resource.getInputStream()) {
            // Deserialize the XML document straight into helper transfer objects.
            // Deszerializáljuk az XML dokumentumot közvetlenül segédátviteli objektumokba.
            EmployeeListXml employeeList = xmlMapper.readValue(inputStream, EmployeeListXml.class);
            return parseEmployees(employeeList);
        } catch (IOException e) {
            log.error("Failed to load employee data from XML", e);
            throw new IllegalStateException("Unable to load employee data", e);
        }
    }

    /**
     * Converts the Jackson-mapped structure into deduplicated domain employees.
     * A Jackson által leképzett struktúrát alakítja duplikációmentes domain dolgozókká.
     */
    private List<Employee> parseEmployees(EmployeeListXml employeeList) {
        // Guard against an empty XML payload to simplify downstream operations.
        // Védekezünk az üres XML tartalom ellen, hogy egyszerűbbek legyenek a későbbi műveletek.
        if (employeeList == null || CollectionUtils.isEmpty(employeeList.getEmployees())) {
            log.warn("No employee nodes discovered in XML input");
            return List.of();
        }
        // Use a map to merge repeated employees under the same full name.
        // Mapet használunk az ismétlődő dolgozók egyesítésére ugyanazon teljes név alatt.
        Map<String, Employee> employeeMap = new HashMap<>();
        for (EmployeeXml entry : employeeList.getEmployees()) {
            if (entry == null || entry.getName() == null || entry.getName().isBlank()) {
                // Skip entries without a valid name to maintain data quality.
                // Kihagyjuk a hiányzó névvel rendelkező bejegyzéseket az adatminőség megőrzéséért.
                continue;
            }
            String[] parts = splitName(entry.getName().trim());
            Employee employee = employeeMap.computeIfAbsent(
                    entry.getName().trim(), key -> new Employee(parts[0], parts[1]));
            if (!CollectionUtils.isEmpty(entry.getDepartments())) {
                for (String department : entry.getDepartments()) {
                    // Register each declared department for the deduplicated employee.
                    // A duplikációmentes dolgozóhoz hozzárendeljük az összes deklarált osztályt.
                    employee.addDepartment(department);
                }
            }
        }
        List<Employee> sorted = employeeMap.values().stream()
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .toList();
        log.info(
                "Parsed {} employees across {} XML nodes",
                sorted.size(),
                employeeList.getEmployees().size());
        return List.copyOf(sorted);
    }

    /**
     * Splits a full name into first and last name components.
     * Feldarabolja a teljes nevet keresztnévre és vezetéknévre.
     */
    private String[] splitName(String fullName) {
        String[] parts = fullName.trim().split(" ", 2);
        if (parts.length == 1) {
            // Provide an empty last name when only a single token exists.
            // Üres vezetéknevet adunk, ha csak egyetlen elem szerepel.
            return new String[] {parts[0], ""};
        }
        return parts;
    }

    /**
     * Lightweight container mirroring the XML root element.
     * Könnyűsúlyú tároló, amely tükrözi az XML gyökérelemet.
     */
    private static class EmployeeListXml {

        // Backing list for the employee elements extracted from XML.
        // A dolgozó elemeket tároló lista az XML-ből kinyerve.
        @JacksonXmlElementWrapper(useWrapping = false)
        @com.fasterxml.jackson.annotation.JsonProperty("employee")
        private List<EmployeeXml> employees;

        EmployeeListXml() {
            // Default constructor required by Jackson for instantiation.
            // Alapértelmezett konstruktor, amelyet a Jackson használ példányosításhoz.
        }

        List<EmployeeXml> getEmployees() {
            // Expose the raw employee representations for post-processing.
            // Elérhetővé tesszük az alkalmazás utófeldolgozásához a dolgozó reprezentációkat.
            return employees;
        }

        void setEmployees(List<EmployeeXml> employees) {
            // Allow Jackson to inject the parsed employee list during deserialization.
            // Lehetővé tesszük, hogy a Jackson beállítsa a feldolgozott dolgozó listát deszerializáláskor.
            this.employees = employees;
        }
    }

    /**
     * Lightweight DTO representing an employee entry in the XML file.
     * Könnyűsúlyú DTO, amely az XML fájl egy dolgozó bejegyzését reprezentálja.
     */
    private static class EmployeeXml {

        // Full name string as it appears in the XML document.
        // A teljes név szövege úgy, ahogy az XML dokumentumban szerepel.
        private String name;

        // All departments listed for the XML employee element.
        // Az XML dolgozó elemhez felsorolt összes osztály.
        @JacksonXmlElementWrapper(useWrapping = false)
        @com.fasterxml.jackson.annotation.JsonProperty("department")
        private List<String> departments;

        EmployeeXml() {
            // Default constructor for the XML mapper to instantiate the DTO.
            // Alapértelmezett konstruktor, hogy az XML mapper példányosíthassa a DTO-t.
        }

        String getName() {
            // Provide the raw combined name for business-level transformation.
            // Biznisz szintű átalakításhoz adjuk vissza a nyers kombinált nevet.
            return name;
        }

        void setName(String name) {
            // Accept the parsed name supplied by the XML mapper.
            // Elfogadjuk az XML mapper által szolgáltatott feldolgozott nevet.
            this.name = name;
        }

        List<String> getDepartments() {
            // Return the list of departments so we can enrich the domain object.
            // Visszaadjuk az osztályok listáját, hogy gazdagítsuk a domain objektumot.
            return departments;
        }

        void setDepartments(List<String> departments) {
            // Allow the XML mapper to populate the department collection.
            // Lehetővé tesszük, hogy az XML mapper feltöltse az osztálygyűjteményt.
            this.departments = departments;
        }
    }
}
