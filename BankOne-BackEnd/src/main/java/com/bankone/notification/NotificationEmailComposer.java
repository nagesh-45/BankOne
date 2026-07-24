package com.bankone.notification;

import com.bankone.notification.dto.BankActionEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * Builds customer-facing BankOne notification emails (plain + HTML).
 * Subjects stay calm and specific to reduce spam scoring.
 */
@Component
public class NotificationEmailComposer {

    private static final Map<String, String> TITLES = Map.of(
            "ACCOUNT_OPENED", "Your account is ready",
            "DEPOSIT_COMPLETE", "Deposit confirmed",
            "WITHDRAW_COMPLETE", "Withdrawal confirmed"
    );

    public NotificationEmailContent compose(BankActionEvent event) {
        String title = TITLES.getOrDefault(
                event.getAction() == null ? "" : event.getAction(),
                humanize(event.getAction())
        );
        String subject = "BankOne: " + title;
        String summary = nullToEmpty(event.getSummary());
        String when = nullToEmpty(event.getOccurredAt());
        String reference = nullToEmpty(event.getEntityId());
        String type = nullToEmpty(event.getEntityType());

        String plain = """
                BankOne notification

                %s

                %s

                Reference: %s
                Type: %s
                When (UTC): %s

                This message was sent because of activity on your BankOne account.
                If you did not expect this, contact your bank branch.

                — BankOne
                """.formatted(title, summary, reference, type, when);

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#eef2f6;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#eef2f6;padding:24px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="560" cellspacing="0" cellpadding="0" style="max-width:560px;width:100%%;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #dbe3ee;">
                          <tr>
                            <td style="background:#0b3a5b;padding:20px 28px;">
                              <div style="font-size:20px;font-weight:700;letter-spacing:0.02em;color:#ffffff;">BankOne</div>
                              <div style="margin-top:4px;font-size:13px;color:#b6d0e4;">Account notification</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px;">
                              <h1 style="margin:0 0 12px;font-size:22px;line-height:1.3;color:#0b3a5b;">%s</h1>
                              <p style="margin:0 0 20px;font-size:15px;line-height:1.55;color:#374151;">%s</p>
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f8fb;border-radius:8px;border:1px solid #e5edf5;">
                                <tr>
                                  <td style="padding:14px 16px;font-size:13px;color:#4b5563;">
                                    <div style="margin-bottom:8px;"><strong style="color:#111827;">Reference</strong><br/>%s</div>
                                    <div style="margin-bottom:8px;"><strong style="color:#111827;">Type</strong><br/>%s</div>
                                    <div><strong style="color:#111827;">When (UTC)</strong><br/>%s</div>
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:20px 0 0;font-size:12px;line-height:1.5;color:#6b7280;">
                                You received this because of activity on your BankOne account.
                                If you did not expect this message, contact your branch.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:14px 28px;background:#f8fafc;border-top:1px solid #e5edf5;font-size:11px;color:#9ca3af;">
                              © BankOne · Transactional notification · Do not reply to this email
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escape(subject),
                escape(title),
                escape(summary),
                escape(reference),
                escape(type),
                escape(when)
        );

        return new NotificationEmailContent(subject, plain, html);
    }

    private static String humanize(String action) {
        if (!StringUtils.hasText(action)) {
            return "Account update";
        }
        return action.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
