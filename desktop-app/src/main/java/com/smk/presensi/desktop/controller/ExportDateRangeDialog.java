package com.smk.presensi.desktop.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

/**
 * Dialog for selecting date range for export
 */
public class ExportDateRangeDialog extends Dialog<LocalDate[]> {
    
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    
    public ExportDateRangeDialog() {
        setTitle("Select Date Range");
        setHeaderText("Choose the date range for export");
        
        // Create dialog pane
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create grid pane for form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Start date
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        grid.add(new Label("Start Date:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        
        // End date
        endDatePicker = new DatePicker(LocalDate.now());
        grid.add(new Label("End Date:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        
        dialogPane.setContent(grid);
        
        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                
                // Validation
                if (startDate == null || endDate == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setContentText("Both dates must be selected!");
                    alert.showAndWait();
                    return null;
                }
                
                if (startDate.isAfter(endDate)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setContentText("Start date must be before or equal to end date!");
                    alert.showAndWait();
                    return null;
                }
                
                return new LocalDate[]{startDate, endDate};
            }
            return null;
        });
    }
}
