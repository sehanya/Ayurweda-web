package com.example.ayurlink.controller;

import com.example.ayurlink.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final PaymentService paymentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        LocalDate today = LocalDate.now();
        Map<String, Object> dailySummary = paymentService.getDailySummary(today);

        model.addAttribute("dailySummary", dailySummary);
        return "superadmin/dashboard";
    }

    @GetMapping("/payments/daily")
    public String dailySummary(@RequestParam(required = false) String date, Model model) {
        LocalDate selectedDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        Map<String, Object> summary = paymentService.getDailySummary(selectedDate);

        model.addAttribute("summary", summary);
        model.addAttribute("selectedDate", selectedDate);
        return "superadmin/daily-summary";
    }

    @GetMapping("/payments/monthly")
    public String monthlySummary(@RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 Model model) {
        LocalDate now = LocalDate.now();
        int selectedYear = (year != null) ? year : now.getYear();
        int selectedMonth = (month != null) ? month : now.getMonthValue();

        Map<String, Object> summary = paymentService.getMonthlySummary(selectedYear, selectedMonth);

        model.addAttribute("summary", summary);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        return "superadmin/monthly-summary";
    }
    @GetMapping("/reports")
    public String reports(Model model) {

        LocalDate today = LocalDate.now();
        Map<String, Object> dailySummary = paymentService.getDailySummary(today);
        Map<String, Object> monthlySummary = paymentService.getMonthlySummary(
                today.getYear(),
                today.getMonthValue()
        );
        Map<String, Object> systemOverview = paymentService.getSystemOverview();

        model.addAttribute("todayRevenue", dailySummary.get("totalRevenue"));
        model.addAttribute("monthRevenue", monthlySummary.get("totalRevenue"));
        model.addAttribute("totalDoctors", systemOverview.get("totalDoctors"));
        model.addAttribute("totalPatients", systemOverview.get("totalPatients"));

        return "superadmin/reports";
    }
    @GetMapping("/reports/payment-methods")
    public String paymentMethodsReport(Model model) {
        Map<String, Object> paymentMethodData = paymentService.getPaymentMethodAnalysis();
        model.addAttribute("data", paymentMethodData);
        return "superadmin/reports/payment-methods";
    }

    // Doctor Performance Report
    @GetMapping("/reports/doctor-performance")
    public String doctorPerformanceReport(Model model) {
        Map<String, Object> doctorData = paymentService.getDoctorPerformanceReport();
        model.addAttribute("data", doctorData);
        return "superadmin/reports/doctor-performance";
    }

    // Treatment Analysis Report
    @GetMapping("/reports/treatment-analysis")
    public String treatmentAnalysisReport(Model model) {
        Map<String, Object> treatmentData = paymentService.getTreatmentAnalysis();
        model.addAttribute("data", treatmentData);
        return "superadmin/reports/treatment-analysis";
    }

    // Refund Report
    @GetMapping("/reports/refunds")
    public String refundsReport(Model model) {
        Map<String, Object> refundData = paymentService.getRefundReport();
        model.addAttribute("data", refundData);
        return "superadmin/reports/refunds";
    }

    // Patient Analytics Report
    @GetMapping("/reports/patients")
    public String patientsReport(Model model) {
        Map<String, Object> patientData = paymentService.getPatientAnalytics();
        model.addAttribute("data", patientData);
        return "superadmin/reports/patients";
    }

    // System Overview Report
    @GetMapping("/reports/system-overview")
    public String systemOverviewReport(Model model) {
        Map<String, Object> systemData = paymentService.getSystemOverview();
        model.addAttribute("data", systemData);
        return "superadmin/reports/system-overview";
    }

}