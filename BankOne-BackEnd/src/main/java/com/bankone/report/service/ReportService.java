package com.bankone.report.service;

import com.bankone.account.repository.AccountRepository;
import com.bankone.common.exception.BadRequestException;
import com.bankone.report.dto.AccountMixReport;
import com.bankone.report.dto.ApprovalsReport;
import com.bankone.report.dto.DailyTrendPoint;
import com.bankone.report.dto.NamedCount;
import com.bankone.report.dto.TransactionTrendsReport;
import com.bankone.transaction.repository.TransactionRepository;
import com.bankone.transfer.enums.TransferRequestStatus;
import com.bankone.transfer.repository.TransferRequestRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ISO_LOCAL_DATE;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransferRequestRepository transferRequestRepository;

    public ReportService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            TransferRequestRepository transferRequestRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transferRequestRepository = transferRequestRepository;
    }

    @Transactional(readOnly = true)
    public TransactionTrendsReport transactionTrends(LocalDate from, LocalDate to) {
        Range range = requireRange(from, to);
        Instant fromTs = range.from().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toTs = range.toExclusive().atStartOfDay().toInstant(ZoneOffset.UTC);

        Map<String, DailyTrendPoint> byDay = new LinkedHashMap<>();

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        long creditCount = 0;
        long debitCount = 0;

        for (Object[] row : transactionRepository.aggregateDailyByType(fromTs, toTs)) {
            String day = String.valueOf(row[0]);
            String type = String.valueOf(row[1]);
            BigDecimal amount = row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString());
            long count = row[3] == null ? 0L : ((Number) row[3]).longValue();

            DailyTrendPoint existing = byDay.getOrDefault(day, new DailyTrendPoint(
                    day, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0));

            if ("CREDIT".equalsIgnoreCase(type)) {
                existing = new DailyTrendPoint(
                        day, amount, existing.debitAmount(), count, existing.debitCount());
                totalCredit = totalCredit.add(amount);
                creditCount += count;
            } else if ("DEBIT".equalsIgnoreCase(type)) {
                existing = new DailyTrendPoint(
                        day, existing.creditAmount(), amount, existing.creditCount(), count);
                totalDebit = totalDebit.add(amount);
                debitCount += count;
            }
            byDay.put(day, existing);
        }

        return new TransactionTrendsReport(
                range.from().format(DAY),
                range.to().format(DAY),
                new ArrayList<>(byDay.values()),
                totalCredit,
                totalDebit,
                creditCount,
                debitCount
        );
    }

    @Transactional(readOnly = true)
    public AccountMixReport accountMix() {
        List<NamedCount> byType = toNamedCounts(accountRepository.countGroupedByAccountType());
        List<NamedCount> byStatus = toNamedCounts(accountRepository.countGroupedByStatus());
        long total = byType.stream().mapToLong(NamedCount::count).sum();
        return new AccountMixReport(byType, byStatus, total);
    }

    @Transactional(readOnly = true)
    public ApprovalsReport approvals(LocalDate from, LocalDate to) {
        Range range = requireRange(from, to);
        LocalDateTime fromAt = range.from().atStartOfDay();
        LocalDateTime toAt = range.toExclusive().atStartOfDay();

        List<NamedCount> byStatus = toNamedCounts(
                transferRequestRepository.countByStatusInRange(fromAt, toAt));
        List<NamedCount> byStaff = toNamedCounts(
                transferRequestRepository.countByResolvedStaffInRange(
                        fromAt, toAt, TransferRequestStatus.PENDING));
        long total = byStatus.stream().mapToLong(NamedCount::count).sum();
        return new ApprovalsReport(
                range.from().format(DAY),
                range.to().format(DAY),
                byStatus,
                byStaff,
                total
        );
    }

    public byte[] transactionTrendsPdf(LocalDate from, LocalDate to) {
        TransactionTrendsReport report = transactionTrends(from, to);
        return buildPdf("Transaction trends", report.fromDate() + " → " + report.toDate(), List.of(
                sectionTable("Daily amounts",
                        List.of("Date", "Credit amount", "Debit amount", "Credit #", "Debit #"),
                        report.daily().stream()
                                .map(d -> List.of(
                                        d.date(),
                                        money(d.creditAmount()),
                                        money(d.debitAmount()),
                                        String.valueOf(d.creditCount()),
                                        String.valueOf(d.debitCount())
                                ))
                                .toList()),
                sectionTable("Totals",
                        List.of("Metric", "Value"),
                        List.of(
                                List.of("Total credit amount", money(report.totalCreditAmount())),
                                List.of("Total debit amount", money(report.totalDebitAmount())),
                                List.of("Credit transactions", String.valueOf(report.totalCreditCount())),
                                List.of("Debit transactions", String.valueOf(report.totalDebitCount()))
                        ))
        ));
    }

    public byte[] accountMixPdf() {
        AccountMixReport report = accountMix();
        return buildPdf("Account mix", "Point-in-time snapshot", List.of(
                sectionTable("By account type",
                        List.of("Type", "Count"),
                        report.byType().stream()
                                .map(n -> List.of(n.name(), String.valueOf(n.count())))
                                .toList()),
                sectionTable("By status",
                        List.of("Status", "Count"),
                        report.byStatus().stream()
                                .map(n -> List.of(n.name(), String.valueOf(n.count())))
                                .toList()),
                sectionTable("Total",
                        List.of("Metric", "Value"),
                        List.of(List.of("Total accounts", String.valueOf(report.totalAccounts()))))
        ));
    }

    public byte[] approvalsPdf(LocalDate from, LocalDate to) {
        ApprovalsReport report = approvals(from, to);
        return buildPdf("Transfer approvals", report.fromDate() + " → " + report.toDate(), List.of(
                sectionTable("By status",
                        List.of("Status", "Count"),
                        report.byStatus().stream()
                                .map(n -> List.of(n.name(), String.valueOf(n.count())))
                                .toList()),
                sectionTable("By resolving staff",
                        List.of("Staff", "Count"),
                        report.byStaff().stream()
                                .map(n -> List.of(n.name(), String.valueOf(n.count())))
                                .toList()),
                sectionTable("Total",
                        List.of("Metric", "Value"),
                        List.of(List.of("Total requests", String.valueOf(report.totalRequests()))))
        ));
    }

    private record Range(LocalDate from, LocalDate to, LocalDate toExclusive) {
    }

    private Range requireRange(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now(ZoneOffset.UTC) : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (end.isBefore(start)) {
            throw new BadRequestException("to date must be on or after from date");
        }
        return new Range(start, end, end.plusDays(1));
    }

    private List<NamedCount> toNamedCounts(List<Object[]> rows) {
        List<NamedCount> result = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] == null ? "UNKNOWN" : String.valueOf(row[0]);
            long count = row[1] == null ? 0L : ((Number) row[1]).longValue();
            result.add(new NamedCount(name, count));
        }
        return result;
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : String.format(Locale.US, "%,.2f", value);
    }

    private record PdfSection(String title, List<String> headers, List<List<String>> rows) {
    }

    private PdfSection sectionTable(String title, List<String> headers, List<List<String>> rows) {
        return new PdfSection(title, headers, rows);
    }

    private byte[] buildPdf(String title, String subtitle, List<PdfSection> sections) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph("BankOne — " + title, titleFont));
            document.add(new Paragraph(subtitle, subtitleFont));
            document.add(new Paragraph("Generated at " + Instant.now(), subtitleFont));
            document.add(new Paragraph(" "));

            for (PdfSection section : sections) {
                document.add(new Paragraph(section.title(), sectionFont));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(section.headers().size());
                table.setWidthPercentage(100);
                for (String header : section.headers()) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, cellFont));
                    cell.setBackgroundColor(new Color(226, 232, 240));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(cell);
                }
                if (section.rows().isEmpty()) {
                    PdfPCell empty = new PdfPCell(new Phrase("No data for this period", cellFont));
                    empty.setColspan(section.headers().size());
                    table.addCell(empty);
                } else {
                    for (List<String> row : section.rows()) {
                        for (String value : row) {
                            table.addCell(new Phrase(value == null ? "" : value, cellFont));
                        }
                    }
                }
                document.add(table);
                document.add(new Paragraph(" "));
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to build PDF report", ex);
        }
    }
}
