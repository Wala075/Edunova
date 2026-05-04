package edu.edunova.utils;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for real-time input control on JavaFX fields.
 */
public class InputUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── TextField filters ────────────────────────────────────────────────────

    /**
     * Restricts a TextField to decimal numbers only (digits + one dot).
     * Also enforces max value of 20 for grade fields.
     *
     * @param field    the TextField to restrict
     * @param maxValue maximum allowed value (use Double.MAX_VALUE for no limit)
     */
    public static void applyDecimalFilter(TextField field, double maxValue) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            // Allow only digits and a single dot
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldVal);
                return;
            }

            // Enforce max value once a complete number is typed
            if (!newVal.isEmpty() && !newVal.equals(".")) {
                try {
                    double val = Double.parseDouble(newVal);
                    if (val > maxValue) {
                        field.setText(oldVal);
                    }
                } catch (NumberFormatException ignored) {
                    // still being typed (e.g. "1.")
                }
            }
        });
    }

    /**
     * Restricts a TextField to positive integers only (digits, no dot).
     *
     * @param field    the TextField to restrict
     * @param maxValue maximum allowed integer value (use Integer.MAX_VALUE for no limit)
     */
    public static void applyIntegerFilter(TextField field, int maxValue) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            // Allow only digits
            if (!newVal.matches("\\d*")) {
                field.setText(oldVal);
                return;
            }

            // Enforce max value
            if (!newVal.isEmpty()) {
                try {
                    int val = Integer.parseInt(newVal);
                    if (val > maxValue) {
                        field.setText(oldVal);
                    }
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    // ── DatePicker control ───────────────────────────────────────────────────

    /**
     * Configures a DatePicker to:
     * - Display dates in dd/MM/yyyy format
     * - Block invalid characters in the text editor (only digits and '/')
     * - Revert to the previous valid date if the typed value is invalid
     */
    public static void applyDateFilter(DatePicker datePicker) {

        // Custom converter: text ↔ LocalDate in dd/MM/yyyy
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) return null;
                try {
                    return LocalDate.parse(text.trim(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return null; // invalid → revert
                }
            }
        });

        // Block invalid characters in the editor (only digits and '/')
        datePicker.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            // Allow digits, '/', and empty string only
            if (!newVal.matches("[\\d/]*")) {
                datePicker.getEditor().setText(oldVal);
                return;
            }
            // Max length: dd/MM/yyyy = 10 chars
            if (newVal.length() > 10) {
                datePicker.getEditor().setText(oldVal);
            }
        });

        // On focus lost: validate the typed date and revert if invalid
        datePicker.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String text = datePicker.getEditor().getText();
                if (text == null || text.trim().isEmpty()) {
                    datePicker.setValue(null);
                    return;
                }
                try {
                    LocalDate parsed = LocalDate.parse(text.trim(), DATE_FORMATTER);
                    datePicker.setValue(parsed);
                } catch (DateTimeParseException e) {
                    // Revert editor to the last valid value
                    LocalDate current = datePicker.getValue();
                    datePicker.getEditor().setText(
                            current != null ? DATE_FORMATTER.format(current) : "");
                }
            }
        });

        // Set prompt text to show expected format
        datePicker.getEditor().setPromptText("jj/MM/aaaa");
    }
}
