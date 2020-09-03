/**
 * 
 */
package pricemonitor;

import java.util.Date;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * @author Chris Boado 905714629
 * @version May 4, 2016
 */
public class IncomeTrackerTab {
    
    Tab incomeTrackerTab;
    HBox hbox;
    GridPane gpane;
    
    public IncomeTrackerTab() {
        incomeTrackerTab = new Tab("Income Tracker");
        hbox = new HBox();
        gpane = new GridPane();
    }
    
    private class CreateChart {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        //final LineChart<Date, Number> lineChart = 
        //        new LineChart<Date, Number>(xAxis, yAxis);
        Series<Date, Number> income = new Series<Date, Number>();
        public CreateChart() {
            
        }
    }
    
    private class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int priorValue, int value) {
            setPrefSize(100,100);
            
            final Label label = createDataThresholdLabel(priorValue, value);
            
            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            
            setOnMouseExited(new EventHandler<MouseEvent>() {
               @Override public void handle(MouseEvent mouseEvent) {
                   getChildren().clear();
                   setCursor(Cursor.CROSSHAIR);
               }
            });
        }
        
        private Label createDataThresholdLabel(int priorValue, int value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
            
            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            }
            else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            }
            else {
                label.setTextFill(Color.FIREBRICK);
            }
            
            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
}
