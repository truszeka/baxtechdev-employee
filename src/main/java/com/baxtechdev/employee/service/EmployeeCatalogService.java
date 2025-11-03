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
 */
@Service
public class EmployeeCatalogService {

    private static final Comparator<Employee> EMPLOYEE_NAME_COMPARATOR =
            Comparator.comparing(Employee::getFirstName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Employee::getLastName, String.CASE_INSENSITIVE_ORDER);

    private final List<Employee> employees;

    /**
     * Instantiates the service and loads the XML file from the classpath.
     *
     * @throws IllegalStateException if the XML file cannot be parsed
     */
    public EmployeeCatalogService() {
        this.employees = loadEmployees();
    }

    /**
     * @return all employee names sorted by first and last name
     */
    public List<String> getAllEmployeeNames() {
        return employees.stream()
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .map(Employee::getFullName)
                .toList();
    }

    /**
     * Returns employee names that belong to the provided department.
     *
     * @param department the department to filter by
     * @return the sorted list of employee names in the department
     */
    public List<String> getEmployeeNamesByDepartment(String department) {
        if (department == null || department.isBlank()) {
            return List.of();
        }
        String normalized = department.trim().toLowerCase(Locale.ROOT);
        return employees.stream()
                .filter(employee -> containsDepartment(employee, normalized))
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .map(Employee::getFullName)
                .toList();
    }

    private boolean containsDepartment(Employee employee, String normalizedDepartment) {
        return employee.getDepartments().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(name -> name.equals(normalizedDepartment));
    }

    /**
     * Groups employees by their departments.
     *
     * @return department summaries with sorted employee names
     */
    public List<DepartmentEmployees> getEmployeesGroupedByDepartment() {
        Map<String, List<Employee>> grouped = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Employee employee : employees) {
            for (String department : employee.getDepartments()) {
                grouped.computeIfAbsent(department, key -> new ArrayList<>()).add(employee);
            }
        }
        return grouped.entrySet().stream()
                .map(entry -> new DepartmentEmployees(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(EMPLOYEE_NAME_COMPARATOR)
                                .map(Employee::getFullName)
                                .toList()))
                .sorted(Comparator.comparing(DepartmentEmployees::getDepartment, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<Employee> loadEmployees() {
        ClassPathResource resource = new ClassPathResource("employee.xml");
        try (InputStream inputStream = resource.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            return parseEmployees(document);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Unable to load employee data", e);
        }
    }

    private List<Employee> parseEmployees(Document document) {
        NodeList employeeNodes = document.getElementsByTagName("employee");
        Map<String, Employee> employeeMap = new HashMap<>();
        for (int i = 0; i < employeeNodes.getLength(); i++) {
            Node node = employeeNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            String fullName = textContent(element, "name");
            if (fullName == null || fullName.isBlank()) {
                continue;
            }
            String[] parts = splitName(fullName.trim());
            Employee employee = employeeMap.computeIfAbsent(
                    fullName.trim(), key -> new Employee(parts[0], parts[1]));
            NodeList departmentNodes = element.getElementsByTagName("department");
            for (int j = 0; j < departmentNodes.getLength(); j++) {
                Node departmentNode = departmentNodes.item(j);
                if (departmentNode.getNodeType() == Node.ELEMENT_NODE) {
                    employee.addDepartment(departmentNode.getTextContent());
                }
            }
        }
        List<Employee> sorted = employeeMap.values().stream()
                .sorted(EMPLOYEE_NAME_COMPARATOR)
                .toList();
        return List.copyOf(sorted);
    }

    private String textContent(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    private String[] splitName(String fullName) {
        String[] parts = fullName.trim().split(" ", 2);
        if (parts.length == 1) {
            return new String[] {parts[0], ""};
        }
        return parts;
    }
}
