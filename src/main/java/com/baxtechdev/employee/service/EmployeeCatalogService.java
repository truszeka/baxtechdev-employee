package com.baxtechdev.employee.service;

import com.baxtechdev.employee.dto.DepartmentEmployees;
import com.baxtechdev.employee.model.Employee;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
        try (InputStream inputStream = resource.getInputStream()) {
            // Configure a DOM builder to parse the simple XML structure.
            // DOM építőt konfigurálunk az egyszerű XML struktúra feldolgozásához.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            return parseEmployees(document);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.error("Failed to load employee data from XML", e);
            throw new IllegalStateException("Unable to load employee data", e);
        }
    }

    /**
     * Transforms the DOM document into de-duplicated employee instances.
     * A DOM dokumentumot duplikációtól mentes dolgozói példányokká alakítja.
     */
    private List<Employee> parseEmployees(Document document) {
        NodeList employeeNodes = document.getElementsByTagName("employee");
        // Use a map to merge repeated employees under the same full name.
        // Mapet használunk az ismétlődő dolgozók egyesítésére ugyanazon teljes név alatt.
        Map<String, Employee> employeeMap = new HashMap<>();
        for (int i = 0; i < employeeNodes.getLength(); i++) {
            Node node = employeeNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                // Skip non-element nodes that do not represent employee entries.
                // Kihagyjuk a nem elem típusú node-okat, amelyek nem jelentenek dolgozó bejegyzést.
                continue;
            }
            Element element = (Element) node;
            String fullName = textContent(element, "name");
            if (fullName == null || fullName.isBlank()) {
                // Ignore entries that do not have a valid name specified.
                // Figyelmen kívül hagyjuk azokat a bejegyzéseket, amelyekhez nincs érvényes név megadva.
                continue;
            }
            String[] parts = splitName(fullName.trim());
            Employee employee = employeeMap.computeIfAbsent(
                    fullName.trim(), key -> new Employee(parts[0], parts[1]));
            NodeList departmentNodes = element.getElementsByTagName("department");
            for (int j = 0; j < departmentNodes.getLength(); j++) {
                Node departmentNode = departmentNodes.item(j);
                if (departmentNode.getNodeType() == Node.ELEMENT_NODE) {
                    // Register each department listed for the employee.
                    // Regisztráljuk a dolgozóhoz felsorolt összes osztályt.
                    employee.addDepartment(departmentNode.getTextContent());
                }
            }
        }
        List<Employee> sorted = employeeMap.values().stream()
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .toList();
        log.info("Parsed {} employees across {} XML nodes", sorted.size(), employeeNodes.getLength());
        return List.copyOf(sorted);
    }

    /**
     * Extracts the text content of the first child with the provided tag name.
     * Kinyeri az első megadott nevű gyermek szöveges tartalmát.
     */
    private String textContent(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            // Return null when the tag is absent so callers can decide how to handle it.
            // Nullt adunk vissza, ha a tag hiányzik, így a hívó dönthet a kezelésről.
            return null;
        }
        return nodes.item(0).getTextContent();
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
}
