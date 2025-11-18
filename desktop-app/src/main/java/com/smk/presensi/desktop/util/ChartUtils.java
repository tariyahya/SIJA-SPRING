package com.smk.presensi.desktop.util;

import javafx.scene.chart.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Utility class for JavaFX Chart operations
 */
public class ChartUtils {

    /**
     * Apply custom colors to PieChart slices
     */
    public static void applyPieChartColors(PieChart chart, Map<String, String> colorMap) {
        chart.getData().forEach(data -> {
            String status = extractStatus(data.getName());
            String color = colorMap.getOrDefault(status, "#9E9E9E");
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        });
    }

    /**
     * Extract status from PieChart label (e.g., "HADIR (25)" -> "HADIR")
     */
    private static String extractStatus(String label) {
        int index = label.indexOf(" (");
        return index > 0 ? label.substring(0, index) : label;
    }

    /**
     * Get default status colors
     */
    public static Map<String, String> getDefaultStatusColors() {
        return Map.of(
            "HADIR", "#4CAF50",      // Green
            "TERLAMBAT", "#FF9800",  // Orange
            "SAKIT", "#FFC107",      // Amber
            "IZIN", "#2196F3",       // Blue
            "ALFA", "#f44336",       // Red
            "ALPHA", "#f44336"       // Red (alternative spelling)
        );
    }

    /**
     * Format date for chart labels
     */
    public static String formatDateLabel(LocalDate date, DateFormat format) {
        DateTimeFormatter formatter = switch (format) {
            case SHORT -> DateTimeFormatter.ofPattern("dd/MM");
            case MEDIUM -> DateTimeFormatter.ofPattern("dd MMM");
            case LONG -> DateTimeFormatter.ofPattern("dd MMMM yyyy");
        };
        return date.format(formatter);
    }

    /**
     * Get week label from date (e.g., "W46 2025")
     */
    public static String getWeekLabel(LocalDate date, boolean includeYear) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int week = date.get(weekFields.weekOfWeekBasedYear());
        int year = date.getYear();
        return includeYear ? "W" + week + " " + year : "W" + week;
    }

    /**
     * Create XYChart.Series from data map
     */
    public static <X, Y> XYChart.Series<X, Y> createSeries(String name, Map<X, Y> data) {
        XYChart.Series<X, Y> series = new XYChart.Series<>();
        series.setName(name);
        
        data.forEach((key, value) -> {
            series.getData().add(new XYChart.Data<>(key, value));
        });
        
        return series;
    }

    /**
     * Configure number axis for integer values
     */
    public static void configureIntegerAxis(NumberAxis axis) {
        axis.setMinorTickVisible(false);
        axis.setTickUnit(1);
        axis.setAutoRanging(true);
    }

    /**
     * Add hover tooltips to chart data points
     */
    public static <X, Y> void addTooltips(XYChart<X, Y> chart) {
        for (XYChart.Series<X, Y> series : chart.getData()) {
            for (XYChart.Data<X, Y> data : series.getData()) {
                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                    String.format("%s: %s", data.getXValue(), data.getYValue())
                );
                javafx.scene.control.Tooltip.install(data.getNode(), tooltip);
            }
        }
    }

    /**
     * Set chart legend position
     */
    public static void setLegendPosition(Chart chart, LegendPosition position) {
        chart.setLegendSide(switch (position) {
            case TOP -> javafx.geometry.Side.TOP;
            case BOTTOM -> javafx.geometry.Side.BOTTOM;
            case LEFT -> javafx.geometry.Side.LEFT;
            case RIGHT -> javafx.geometry.Side.RIGHT;
        });
    }

    /**
     * Apply smooth animation to chart
     */
    public static void enableSmoothAnimation(Chart chart) {
        chart.setAnimated(true);
    }

    /**
     * Date format enum
     */
    public enum DateFormat {
        SHORT,   // 18/11
        MEDIUM,  // 18 Nov
        LONG     // 18 November 2025
    }

    /**
     * Legend position enum
     */
    public enum LegendPosition {
        TOP, BOTTOM, LEFT, RIGHT
    }
}
