/**
 * 
 */
package pricemonitor;

import java.sql.SQLException;
import java.util.ArrayList;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Chris Boado 905714629
 * @version Apr 28, 2016
 */
public class AddItemWindow {
    private int listIndex;
    private Stage addItemStage;
    static ConnectToDatabase db;
    public AddItemWindow(ConnectToDatabase db) {
        listIndex = 0;
        AddItemWindow.db = db;
        GridPane pane = new GridPane();
        GridPane pane2 = new GridPane();
        HBox hbox = new HBox();
        pane.setGridLinesVisible(true);
        pane2.setGridLinesVisible(true);
        int numRows = 5;
        int numCols = 2;
        /**for (int i = numRows; i > 0; i--) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100 / numRows);
            pane.getRowConstraints().add(row);
        }
        for (int i = numCols; i > 0; i--) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100 / numCols);
            pane.getColumnConstraints().add(col);
            pane2.getColumnConstraints().add(col);
        }**/
        
        Text name = new Text("Item Name:");
        Text vendor = new Text("Vendor Price:");
        Text current = new Text("Current Price:");
        Text ingredients = new Text("Ingredient Quantity and Name:");
        name.setFont(Font.font(null, FontWeight.BOLD, 12));
        vendor.setFont(Font.font(null, FontWeight.BOLD, 12));
        current.setFont(Font.font(null, FontWeight.BOLD, 12));
        ingredients.setFont(Font.font(null, FontWeight.BOLD, 12));
        name.setTextAlignment(TextAlignment.LEFT);
        vendor.setTextAlignment(TextAlignment.LEFT);
        current.setTextAlignment(TextAlignment.LEFT);
        ingredients.setTextAlignment(TextAlignment.LEFT);
        
        TextField nameField = new TextField();
        TextField vendorField = new TextField();
        TextField currentField = new TextField();
        TextField quantityField = new TextField();
        TextField ingredientField = new TextField("N/A if no ingredients");
        quantityField.setPrefWidth(40);
        ingredientField.setMinWidth(150);
        ArrayList<TextField> quantityList = new ArrayList<TextField>();
        quantityList.add(quantityField);
        ArrayList<TextField> ingredientList = new ArrayList<TextField>();
        ingredientList.add(ingredientField);
        nameField.textProperty().addListener(textFieldResize(nameField));
        vendorField.textProperty().addListener(textFieldResize(vendorField));
        currentField.textProperty().addListener(textFieldResize(currentField));
        quantityField.textProperty().addListener(textFieldResize(quantityField));
        ingredientField.textProperty().addListener(textFieldResize(ingredientField));
        
        Button add = new Button("Add Item");
        Button cancel = new Button("Cancel");
        Button addIngredient = new Button("Add Another Ingredient");
        add.setMinWidth(100);
        cancel.setMinWidth(100);
        hbox.setAlignment(Pos.CENTER);
        GridPane.setHalignment(addIngredient, HPos.LEFT);
        add.disableProperty().bind(
                Bindings.isEmpty(nameField.textProperty())
                .or(Bindings.isEmpty(vendorField.textProperty()))
                .or(Bindings.isEmpty(currentField.textProperty()))
                .or(Bindings.isEmpty(quantityField.textProperty()))
                .or(Bindings.isEmpty(ingredientField.textProperty())));
        add.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String itemName = nameField.getText().trim();
                int vendorPrice = Integer.valueOf(vendorField.getText().trim());
                int currentPrice = Integer.valueOf(currentField.getText().trim());
                String recipe = "";
                if ("N/A".compareTo(ingredientList.get(0).getText().trim()) != 0) {
                    for (TextField temp : quantityList) {
                        recipe = recipe + temp.getText().trim() + "," + 
                                ingredientList.get(quantityList.indexOf(temp)).getText().trim()
                                + ",";
                    }
                    recipe = recipe.substring(0, recipe.length() - 1);
                }
                else {
                    recipe = "N/A";
                }
                try {
                    db.addItem(itemName, vendorPrice, currentPrice, recipe);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                addItemStage.close();
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                listIndex = 0;
                nameField.clear();
                vendorField.clear();
                currentField.clear();
                ingredientField.clear();
                quantityField.clear();
                ingredientList.clear();
                ingredientList.trimToSize();
                quantityList.clear();
                quantityList.trimToSize();
                pane2.getChildren().clear();
                ingredientField.setText("N/A if no ingredients");
                ingredientList.add(ingredientField);
                quantityList.add(quantityField);
                pane2.add(quantityList.get(0), 0, 0);
                pane2.add(ingredientList.get(0), 1, 0);
                add.disableProperty().bind(
                        Bindings.isEmpty(nameField.textProperty())
                        .or(Bindings.isEmpty(vendorField.textProperty()))
                        .or(Bindings.isEmpty(currentField.textProperty()))
                        .or(Bindings.isEmpty(quantityList.get(0).textProperty()))
                        .or(Bindings.isEmpty(ingredientList.get(0).textProperty())));
                addItemStage.hide();
            }
        });
        addIngredient.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                listIndex++;
                TextField newQuantityField = new TextField();
                TextField newIngredientField = new TextField();
                newQuantityField.textProperty().addListener(textFieldResize(newQuantityField));
                newIngredientField.textProperty().addListener(textFieldResize(newIngredientField));
                quantityList.add(newQuantityField);
                ingredientList.add(newIngredientField);
                add.disableProperty().bind(
                        Bindings.isEmpty(quantityList.get(listIndex).textProperty()).or(
                        Bindings.isEmpty(ingredientList.get(listIndex).textProperty()).or(
                        Bindings.isEmpty(quantityList.get(0).textProperty()))
                        ));
                pane2.add(quantityList.get(listIndex), 0, listIndex);
                pane2.add(ingredientList.get(listIndex), 1, listIndex);
                pane.setMinWidth(pane.getWidth());
                addItemStage.sizeToScene();
            }
        });
        
        pane.add(name, 0, 0);
        pane.add(vendor, 0, 1);
        pane.add(current, 0, 2);
        pane.add(ingredients, 0, 3);
        pane.add(addIngredient, 0, 4);
        pane.add(nameField, 1, 0);
        pane.add(vendorField, 1, 1);
        pane.add(currentField, 1, 2);
        pane.add(pane2, 1, 3);
        pane.add(hbox, 1, 4);
        pane2.add(quantityList.get(0), 0, 0);
        pane2.add(ingredientList.get(0), 1, 0);
        hbox.getChildren().addAll(add, cancel);
        
        addItemStage = new Stage();
        Scene scene = new Scene(pane);
        pane.prefHeightProperty().bind(scene.heightProperty());
        pane.prefWidthProperty().bind(scene.widthProperty());
        pane.setPadding(new Insets(5,5,5,5));
        pane2.setPadding(new Insets(5,5,5,5));
        hbox.setPadding(new Insets(5,5,5,5));
        addItemStage.setScene(scene);
        //Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        addItemStage.setTitle("Add Item");
        addItemStage.setResizable(false);
        //addItemStage.setX((primScreenBounds.getWidth() - addItemStage.getWidth()) / 2);
        //addItemStage.setY((primScreenBounds.getHeight() - addItemStage.getHeight()) / 2);
        addItemStage.initModality(Modality.APPLICATION_MODAL);
        addItemStage.show();
    }
    
    private ChangeListener<String> textFieldResize(TextField anonField) {
        ChangeListener<String> textFieldResize = new ChangeListener<String>() {
            private TextField anonField;
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Text string = new Text(anonField.getText());
                double length = string.getLayoutBounds().getWidth();
                anonField.setMinWidth(length + 20);
                addItemStage.sizeToScene();
            }
            private ChangeListener<String> init(TextField var) {
                anonField = var;
                return this;
            }
        }.init(anonField);
        return textFieldResize;
    }
    
    public Stage getAddItemStage() {
        return addItemStage;
    }
}
