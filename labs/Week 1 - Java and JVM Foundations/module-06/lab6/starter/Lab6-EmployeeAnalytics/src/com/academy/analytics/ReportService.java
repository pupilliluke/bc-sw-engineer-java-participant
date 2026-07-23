package com.academy.analytics;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReportService {

    private final EmployeeService employeeService;

    public ReportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public void displayDashboard() {
        // TODO: stream stats (avg/max/min), department count, active/inactive
        // TODO: top performer, highest-paid dept, top 5 salaries — print dashboard block
        throw new UnsupportedOperationException("TODO");
    }

    public void displayEmployeesByDepartment() {
        employeeService.displayGroupedEmployees();
    }

    public void displaySalaryReport() {
        employeeService.displayReductions();
        System.out.println();
        employeeService.displaySummaryStatistics();
        System.out.println();
        employeeService.displayPartitionedEmployees();
    }

    public void displayTopPerformers() {
        System.out.println("Top Performers (Rating >= 4):");
        // TODO: employeeService.getTopPerformers(4).forEach(...)
        throw new UnsupportedOperationException("TODO");
    }

    public void displayHighestSalary() {
        employeeService.displayHighestPaidEmployeeOptional();
    }

    public void displayDepartmentStatistics() {
        // TODO: getDepartmentStatistics(); print count/avg/max/min per dept
        throw new UnsupportedOperationException("TODO");
    }

    public void displayActiveEmployees() {
        employeeService.displayActiveEmployees();
    }

    public void displayBonusInsights() {
        System.out.println("--- Bonus Insights ---");
        // TODO: second highest salary, longest name, highest avg dept, histogram, custom collector
        throw new UnsupportedOperationException("TODO");
    }
}
