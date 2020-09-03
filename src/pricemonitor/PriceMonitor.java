/**
 * 
 */
package pricemonitor;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;

/**
 * @author Chris Boado 905714629
 * @version Mar 25, 2016
 */
public class PriceMonitor extends Application {
    String url;
    ImageView view;
    ImageView goldIcon;
    Item item;
    VBox leftVBox;
    VBox rightVBox;
    Text itemText;
    Text lastUpdateText;
    int diffHours = 0;
    int listIndex = 0;
    Text currentPrice;
    Text highestPrice;
    Text lowestPrice;
    Text averagePrice;
    Text meanValue;
    Text medianValue;
    Text stdValue;
    Text vendorValue;
    Text actualProfit;
    Text percent5Profit;
    Text percent10Profit;
    Text percent15Profit;
    Text recipeText;
    TextFlow textFlow;
    TextFlow textFlow2;
    ListView<String> itemList;
    ObservableList<String> listViewItems;
    
    TabPane mainTabPane;
    SplitPane leftSplitPane;
    GridPane rightGridPane;
    GridPane ahStatsPane;
    GridPane overallStatsPane;
    GridPane recipePane;
    
    CreateChart lineChart;
    AddItemWindow addItemWindow;
    EditItemWindow editItemWindow;
    DeleteItemWindow deleteItemWindow;
    
    public PriceMonitor() throws Exception {
        url = "/resources/icons/logo.png";
        lastUpdateText = new Text(" ");
        diffHours = 0;
        listIndex = 0;
        lineChart = new CreateChart();
    }
    
    static ConnectToDatabase db = null;
    public static void main(String[] args) throws Exception {
        db = new ConnectToDatabase();
        Application.launch(args);
        db.disconnect();
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane tabPane = new TabPane();
        Tab statsTab = new Tab("Stats");
        Tab incomeTrackerTab = new Tab("Income Tracker");
        
        statsTab.setClosable(false);
        tabPane.getTabs().addAll(mainTab(), statsTab, incomeTrackerTab);
        Pane pane = new Pane();
        pane.getChildren().addAll(createMenuBar(), tabPane);
        
        Scene scene = new Scene(pane);
        tabPane.prefHeightProperty().bind(pane.heightProperty());
        tabPane.prefWidthProperty().bind(pane.widthProperty());
        pane.prefHeightProperty().bind(scene.heightProperty());
        pane.prefWidthProperty().bind(scene.widthProperty());
        primaryStage.setTitle("DFO Price Monitor");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    
    //Window Creation
    private ObservableList<String> listViewItems() throws Exception {
        listViewItems = db.getItemNames();
        return listViewItems;
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.useSystemMenuBarProperty().set(true);
        
        Menu menu = new Menu("About");
        MenuItem addItem = new MenuItem("Add New Item");
        addItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addItemWindow = new AddItemWindow(db);
                addItemWindow.getAddItemStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent event) {
                        itemList.getSelectionModel().clearSelection();
                        itemList.setItems(null);
                        try {
                            itemList.setItems(listViewItems());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        
        menu.getItems().add(addItem);
        menuBar.getMenus().add(menu);
        return menuBar;
    }
    
    private int lengthOfString() throws Exception {
        double width = 0;
        for (int i = 0; i < listViewItems().size(); i++) {
            final Text text = new Text(listViewItems().get(i));
            new Scene(new Group(text));
            text.applyCss();
            double temp = text.getLayoutBounds().getWidth();
            if (temp > width) { width = temp; };
        }
        width = Math.ceil(width) + 50;
        return (int) width;
    }
    
    private ImageView createImage() {
        view = new ImageView(new Image(url));
        view.setFitHeight(100);
        view.setFitWidth(100);
        return view;
    }
    
    private Text itemText() {
        itemText = new Text("DFO Price Monitor");
        itemText.setStyle("-fx-font: 24 arial;");
        return itemText;
    }
    
    private Text lastUpdate() {
        lastUpdateText.setText("Last updated hours ago");
        return lastUpdateText;
    }
    
    private ListView<String> itemList() throws Exception {
        itemList = new ListView<String>();
        ContextMenu cm = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        cm.getItems().add(edit);
        itemList.setItems(listViewItems());
        itemList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    try {
                        item = db.createItem(new_val);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    File f = new File("src/resources/icons/" + item.getName() + ".png");
                    if (f.exists() && !f.isDirectory()) {
                        url = url.substring(0,17) + item.getName() + ".png";
                        view.setImage(new Image(url));
                    }
                    else {
                        view.setImage(new Image(url));
                    }
                    itemText.setText(item.getName());
                    lineChart.setData(item);
                    lastUpdateText.setText("Last updated " + item.getDiffHours() + " hours ago");
                    currentPrice.setText(item.getPriceList().get(item.getPriceList().size() - 1).toString());
                    highestPrice.setText(Integer.toString(item.getHighest()));
                    lowestPrice.setText(Integer.toString(item.getLowest()));
                    averagePrice.setText(Integer.toString(item.getAverage()));
                    meanValue.setText(item.getAverage() + " \u00B1" + item.getUncertainty());
                    stdValue.setText(Double.toString(item.getStd()));
                    medianValue.setText(Double.toString(item.getMedian()));
                    vendorValue.setText(Integer.toString(item.getVendorPrice()));
                    actualProfit.setText(String.valueOf(item.getActualProfit()) + "%");
                    percent5Profit.setText(String.valueOf(item.getPercent5Profit()));
                    percent10Profit.setText(String.valueOf(item.getPercent10Profit()));
                    percent15Profit.setText(String.valueOf(item.getPercent15Profit()));
                    setRecipePane();
                });
        itemList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu  contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
            editItem.setOnAction(event -> {
                editItemWindow = new EditItemWindow(db, item, listViewItems, itemList);
                editItemWindow.getEditItemStage().show();
                /**editItemWindow.getEditItemStage().setOn(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent paramT) {
                        try {
                            listViewItems = db.getItemNames();
                            itemList.getSelectionModel().select(item.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });**/
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                deleteItemWindow = new DeleteItemWindow(db, item.getName());
            });
            
            contextMenu.getItems().addAll(editItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                }
                else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
        return itemList;
    }
    
    private VBox leftVBox() {
        leftVBox = new VBox();
        leftVBox.setMinHeight(150);
        leftVBox.setMaxHeight(150);
        leftVBox.setPadding(new Insets(5,5,5,5));
        leftVBox.setAlignment(Pos.CENTER);
        leftVBox.getChildren().add(createImage());
        leftVBox.getChildren().add(new Text("DFO Price Monitor"));
        return leftVBox;
    }
    
    private VBox rightVBox() {
        rightVBox = new VBox();
        rightVBox.getChildren().addAll(createImage(), itemText(), lastUpdate());
        rightVBox.setAlignment(Pos.CENTER);
        return rightVBox;
    }
    
    private GridPane recipePane() {
        recipePane = new GridPane();
        int numRows = 1;
        int numCols = 2;
        for (int i = numRows; i > 0; i--) {
        	RowConstraints row = new RowConstraints();
        	row.setPercentHeight(100 / numRows);
        	recipePane.getRowConstraints().add(row);
        }
        for (int i = numCols; i > 0; i--) {
        	ColumnConstraints col = new ColumnConstraints();
        	col.setPercentWidth(100 / numCols);
        	recipePane.getColumnConstraints().add(col);
        }
        //recipeText = new Text("");
        //recipeText.setFont(Font.font(null, 12));
        //recipePane.add(recipeText, 0, 0);
        textFlow = new TextFlow();
        textFlow2 = new TextFlow();
        recipePane.add(textFlow, 0, 0);
        recipePane.add(textFlow2, 1, 0);
        recipePane.add(goldIcon(), 1, 0);
        textFlow.setTextAlignment(TextAlignment.CENTER);
        textFlow2.setTextAlignment(TextAlignment.CENTER);
        GridPane.setHalignment(goldIcon, HPos.RIGHT);
        GridPane.setValignment(goldIcon, javafx.geometry.VPos.TOP);
        recipePane.setGridLinesVisible(true);
        return recipePane;
    }
    
    private GridPane setRecipePane() {
        textFlow.getChildren().clear();
        textFlow2.getChildren().clear();
        if (Objects.equals(item.getRecipe().getRecipeArray()[0], "N/A")) {
            textFlow.getChildren().add(new Text(item.getRecipe().getRecipeArray()[0]));
            textFlow2.getChildren().add(new Text("0"));
        }
        else {
            for (int i = 0; i < item.getRecipe().getRecipeArray().length - 1; i = i + 2) {
                textFlow.getChildren().add(new Text("x" + item.getRecipe().getRecipeArray()[i] + " " + item.getRecipe().getRecipeArray()[i + 1] + "\n"));
            }
            textFlow2.getChildren().add(new Text(String.valueOf(item.getRecipe().getTotalPrice())));
    	}
    	return recipePane;
    }
    
    private GridPane ahStatsPane() {
        ahStatsPane = new GridPane();
        int numRows = 5;
        int numCols = 2;
        for (int i = numRows; i > 0; i--) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100 / numRows);
            ahStatsPane.getRowConstraints().add(row);
        }
        for (int i = numCols; i > 0; i--) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100 / numCols);
            ahStatsPane.getColumnConstraints().add(col);
        }
        ahStatsPane.setPadding(new Insets(10,10,10,10));
        
        Text currentPriceText = new Text("Current Price:");
        currentPriceText.setFont(Font.font(null, FontWeight.BOLD, 20));
        Text highestPriceText = new Text("Highest Price:");
        highestPriceText.setFont(Font.font(null, FontWeight.BOLD, 20));
        Text lowestPriceText = new Text("Lowest Price:");
        lowestPriceText.setFont(Font.font(null, FontWeight.BOLD, 20));
        Text averagePriceText = new Text("Average Price:");
        averagePriceText.setFont(Font.font(null, FontWeight.BOLD, 20));
        Text recipeText = new Text("Recipe Price:");
        recipeText.setFont(Font.font(null, FontWeight.BOLD, 20));
        currentPrice = new Text("");
        highestPrice = new Text("");
        lowestPrice = new Text("");
        averagePrice = new Text("");
        ahStatsPane.add(currentPriceText, 0, 0);
        ahStatsPane.add(currentPrice, 1, 0);
        ahStatsPane.add(highestPriceText, 0, 1);
        ahStatsPane.add(highestPrice, 1, 1);
        ahStatsPane.add(lowestPriceText, 0, 2);
        ahStatsPane.add(lowestPrice, 1, 2);
        ahStatsPane.add(averagePriceText, 0, 3);
        ahStatsPane.add(averagePrice, 1, 3);
        ahStatsPane.add(recipeText, 0, 4);
        ahStatsPane.add(recipePane(), 1, 4);
        for (int i = 0; i < 4; i++) {
            ahStatsPane.add(goldIcon(), 1, i);
            GridPane.setHalignment(goldIcon, HPos.RIGHT);
        }
        GridPane.setHalignment(currentPriceText, HPos.LEFT);
        GridPane.setHalignment(highestPriceText, HPos.LEFT);
        GridPane.setHalignment(lowestPriceText, HPos.LEFT);
        GridPane.setHalignment(averagePriceText, HPos.LEFT);
        GridPane.setHalignment(currentPrice, HPos.CENTER);
        GridPane.setHalignment(highestPrice, HPos.CENTER);
        GridPane.setHalignment(lowestPrice, HPos.CENTER);
        GridPane.setHalignment(averagePrice, HPos.CENTER);
        ahStatsPane.setGridLinesVisible(true);
        return ahStatsPane;
    }
    
    private GridPane overallStatsPane() {
        overallStatsPane = new GridPane();
        int numRows = 4;
        int numCols = 4;
        for (int i = numRows; i > 0; i--) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100 / numRows);
            overallStatsPane.getRowConstraints().add(row);
            //if (i > numCols) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(100 / numCols);
                overallStatsPane.getColumnConstraints().add(col);
            //}
        }
        Text meanText = new Text("Mean Value:");
        Text medianText = new Text("Median Value:");
        Text stdText = new Text("Standard Deviation:");
        Text vendorText = new Text("Vendor Price:");
        Text actualProfitText = new Text("Actual Profit:");
        Text percent5ProfitText = new Text("5% Profit Margin:");
        Text percent10ProfitText = new Text("10% Profit Margin:");
        Text percent15ProfitText = new Text("15% Profit Margin:");
        meanValue = new Text("");
        medianValue = new Text("");
        stdValue = new Text("");
        vendorValue = new Text("");
        actualProfit = new Text("");
        percent5Profit = new Text("");
        percent10Profit = new Text("");
        percent15Profit = new Text("");
        overallStatsPane.add(meanText, 0, 0);
        overallStatsPane.add(meanValue, 1, 0);
        overallStatsPane.add(medianText, 0, 1);
        overallStatsPane.add(medianValue, 1, 1);
        overallStatsPane.add(stdText, 0, 2);
        overallStatsPane.add(stdValue, 1, 2);
        overallStatsPane.add(vendorText, 0, 3);
        overallStatsPane.add(vendorValue, 1, 3);
        overallStatsPane.add(actualProfitText, 2, 0);
        overallStatsPane.add(actualProfit, 3, 0);
        overallStatsPane.add(percent5ProfitText, 2, 1);
        overallStatsPane.add(percent5Profit, 3, 1);
        overallStatsPane.add(percent10ProfitText, 2, 2);
        overallStatsPane.add(percent10Profit, 3, 2);
        overallStatsPane.add(percent15ProfitText, 2, 3);
        overallStatsPane.add(percent15Profit, 3, 3);
        overallStatsPane.setGridLinesVisible(true);
        overallStatsPane.setPadding(new Insets(5,5,5,5));
        return overallStatsPane;
    }
    
    private SplitPane leftSplitPane() throws Exception {
        leftSplitPane = new SplitPane();
        leftSplitPane.setOrientation(Orientation.VERTICAL);
        leftSplitPane.setMaxWidth(lengthOfString());
        leftSplitPane.setMinWidth(lengthOfString());
        leftSplitPane.getItems().addAll(itemList(), leftVBox());
        return leftSplitPane;
    }
    
    private GridPane rightGridPane() {
        rightGridPane = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(75);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        rightGridPane.getColumnConstraints().addAll(col1, col2);
        rightGridPane.add(lineChart.getLineChart(),0,0);
        //rightGridPane.getChildren().add(createChart());
        rightGridPane.add(new Text(" "), 0, 1);
        rightGridPane.add(overallStatsPane(), 0, 2);
        rightGridPane.add(rightVBox(), 1, 2);
        rightGridPane.add(ahStatsPane(), 1, 0);
        rightGridPane.setGridLinesVisible(true);
        
        GridPane.setHgrow(lineChart.getLineChart(), Priority.ALWAYS);
        GridPane.setVgrow(ahStatsPane, Priority.ALWAYS);
        GridPane.setHalignment(view, HPos.CENTER);
        GridPane.setHalignment(itemText, HPos.CENTER);
        GridPane.setHalignment(lastUpdateText, HPos.CENTER);
        return rightGridPane;
    }
    
    private SplitPane mainSplitPane() throws Exception {
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(leftSplitPane(), rightGridPane());
        return mainSplitPane;
    }
    
    private Tab mainTab() throws Exception {
        Tab mainTab = new Tab();
        mainTab.setContent(mainSplitPane());
        mainTab.setClosable(false);
        mainTab.setText("Main");
        return mainTab;
    } //Window Creation end
    
    //Chart Creation
    /**@SuppressWarnings("unchecked")
    private LineChart<Number, Number> createChart() {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Days");
        yAxis.setLabel("Gold");
        final LineChart<Number, Number> lineChart = 
                new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setTitle("Gold Trends");
        lineChart.setCreateSymbols(false);
        
        Series<Number, Number> goldTrend = new Series<Number, Number>();
        Series<Number, Number> highest = new Series<Number, Number>();
        Series<Number, Number> lowest = new Series<Number, Number>();
        Series<Number, Number> average = new Series<Number, Number>();
        goldTrend.setName("Auction House Prices");
        highest.setName("Highest");
        lowest.setName("Lowest");
        average.setName("average");
        
        int highestPrice = 14;
        int lowestPrice = 5;
        int averagePrice = 7;
        yAxis.setAutoRanging(true);
        Data<Number, Number> goldTrendData = null;
        for (int i = 1; i <= 10; i++) {
            goldTrendData = new Data<Number, Number>(i, i + 4);
            
            goldTrend.getData().add(goldTrendData);
            highest.getData().add(new Data<Number, Number>(i, highestPrice));
            lowest.getData().add(new Data<Number, Number>(i, lowestPrice));
            average.getData().add(new Data<Number, Number>(i, averagePrice));
            
            goldTrendData.setNode(new HoveredThresholdNode(i, i + 4));
        }
        
        lineChart.getData().addAll(highest, lowest, average, goldTrend);
        return lineChart;
    }**/
    
    private class CreateChart {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<String, Number> lineChart =
                new LineChart<String, Number>(xAxis, yAxis);
        Series<String, Number> goldTrend = new Series<String, Number>();
        Series<String, Number> highest = new Series<String, Number>();
        Series<String, Number> lowest = new Series<String, Number>();
        Series<String, Number> average = new Series<String, Number>();
        
        Data<String, Number> goldTrendData = null;
        
        @SuppressWarnings("unchecked")
        private CreateChart() {
            lineChart.getData().addAll(highest, lowest, average, goldTrend);
            createChart();
        }
        
        private void createChart() {
            xAxis.setLabel("Days");
            yAxis.setLabel("Gold");
            yAxis.setAutoRanging(false);
            lineChart.setTitle("Gold Trends");
            lineChart.setCreateSymbols(true);
            lineChart.setAnimated(false);
            goldTrend.setName("Auction House Prices");
            highest.setName("Highest");
            lowest.setName("Lowest");
            average.setName("Average");
        }
        
        public void setData(Item item) {
            yAxis.setUpperBound(item.getHighest() + 5);
            yAxis.setLowerBound(item.getLowest() - 5);
            
            goldTrend.getData().clear();
            highest.getData().clear();
            lowest.getData().clear();
            average.getData().clear();
            for (int i = 1; i <= item.getPriceList().size(); i++) {
                goldTrendData = new Data<String, Number>(String.valueOf(i), item.getPriceList().get(i - 1));
                
                goldTrend.getData().add(goldTrendData);
                highest.getData().add(new Data<String, Number>(String.valueOf(i), item.getHighest()));
                lowest.getData().add(new Data<String, Number>(String.valueOf(i), item.getLowest()));
                average.getData().add(new Data<String, Number>(String.valueOf(i), item.getAverage()));
                
                goldTrendData.setNode(new HoveredThresholdNode(i, item.getPriceList().get(i - 1)));
            }
        }
        
        //Change current price to be the last of the list.
        
        public LineChart<String, Number> getLineChart() {
            return lineChart;
        }
    }
    
    private ImageView goldIcon() {
        goldIcon = new ImageView(new Image("/resources/icons/Gold.png"));
        return goldIcon;
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
