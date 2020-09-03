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
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
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
 * @version Apr 29, 2016
 */
public class EditItemWindow {
    private Stage editItemStage;
    private Scene scene;
    private GridPane pane;
    private GridPane pane2;
    private HBox hbox;
    private ListView<String> itemList;
    
    TextField nameField;
    TextField vendorField;
    TextField priceField;
    
    Button update;
    Button cancel;
    
    ArrayList<TextField> quantityList = new ArrayList<TextField>();
    ArrayList<TextField> ingredientList = new ArrayList<TextField>();
    
    ObservableList<String> listViewItems;
    
    static ConnectToDatabase db;
    String itemName;
    Item item;
    
    public EditItemWindow(ConnectToDatabase db, Item item, 
            ObservableList<String> listViewItems, ListView<String> itemList) {
        EditItemWindow.db = db;
        this.item = item;
        this.listViewItems = listViewItems;
        this.itemList = itemList;
        editItemStage = new Stage();
        scene = new Scene(pane());
        pane.prefHeightProperty().bind(scene.heightProperty());
        pane.prefWidthProperty().bind(scene.widthProperty());
        editItemStage.setScene(scene);
        editItemStage.setTitle("Edit " + item.getName());
        editItemStage.setResizable(false);
        editItemStage.initModality(Modality.APPLICATION_MODAL);
        //Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        //editItemStage.setX((primScreenBounds.getWidth() - editItemStage.getWidth()) / 2);
        //editItemStage.setY((primScreenBounds.getHeight() - editItemStage.getHeight()) / 2);
        editItemStage.hide();
    }
    
    private GridPane pane() {
        pane = new GridPane();
        pane.setGridLinesVisible(true);
        hbox = new HBox();
        
        update = new Button("Update");
        cancel = new Button("Cancel");
        update.setMinWidth(100);
        cancel.setMinWidth(100);
        
        
        Text name = new Text("Item Name:");
        Text vendor = new Text("Vendor Price:");
        Text current = new Text("Prices:");
        Text ingredients = new Text("Ingredient Quantity and Name:");
        name.setFont(Font.font(null, FontWeight.BOLD, 12));
        vendor.setFont(Font.font(null, FontWeight.BOLD, 12));
        current.setFont(Font.font(null, FontWeight.BOLD, 12));
        ingredients.setFont(Font.font(null, FontWeight.BOLD, 12));
        name.setTextAlignment(TextAlignment.LEFT);
        vendor.setTextAlignment(TextAlignment.LEFT);
        current.setTextAlignment(TextAlignment.LEFT);
        ingredients.setTextAlignment(TextAlignment.LEFT);
        
        nameField = new TextField(item.getName());
        vendorField = new TextField(String.valueOf(item.getVendorPrice()));
        priceField = new TextField(item.priceString());
        setMinTextFieldSize(nameField);
        setMinTextFieldSize(vendorField);
        setMinTextFieldSize(priceField);
        nameField.textProperty().addListener(textFieldResize(nameField));
        vendorField.textProperty().addListener(textFieldResize(vendorField));
        priceField.textProperty().addListener(textFieldResize(priceField));
        pane.add(name, 0, 0);
        pane.add(vendor, 0, 1);
        pane.add(current, 0, 2);
        pane.add(ingredients, 0, 3);
        pane.add(nameField, 1, 0);
        pane.add(vendorField, 1, 1);
        pane.add(priceField, 1, 2);
        pane.add(pane2(), 1, 3);
        pane.add(hbox(), 1, 4);
        pane.setPadding(new Insets(5,5,5,5));
        
        update.disableProperty().bind(
                Bindings.isEmpty(nameField.textProperty())
                .or(Bindings.isEmpty(vendorField.textProperty()))
                .or(Bindings.isEmpty(priceField.textProperty())));
        update.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String recipe = "";
                if (ingredientList.size() > 1) {
                    for (int i = 0; i < quantityList.size(); i++) {
                        recipe = recipe + quantityList.get(i).getText() + ","
                                + ingredientList.get(i).getText() + ",";
                    }
                    recipe = recipe.substring(0, recipe.length() - 1);
                }
                else {
                    recipe = "N/A";
                }
                try {
                    db.updateItem(item.getID(),
                            nameField.getText().trim(), 
                            Integer.valueOf(vendorField.getText().trim()),
                            priceField.getText().trim(), 
                            recipe.trim());
                    //listViewItems = db.getItemNames();
                    //itemList.getSelectionModel().clearSelection();
                    itemList.getSelectionModel().select(0);
                    itemList.getSelectionModel().select(item.getName());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                nameField.clear();
                vendorField.clear();
                priceField.clear();
                quantityList.clear();
                quantityList.trimToSize();
                ingredientList.clear();
                ingredientList.trimToSize();
                pane2.getChildren().clear();
                update.disableProperty().bind(
                        Bindings.isEmpty(nameField.textProperty())
                        .or(Bindings.isEmpty(vendorField.textProperty()))
                        .or(Bindings.isEmpty(priceField.textProperty())));
                editItemStage.close();
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                nameField.clear();
                vendorField.clear();
                priceField.clear();
                quantityList.clear();
                quantityList.trimToSize();
                ingredientList.clear();
                ingredientList.trimToSize();
                pane2.getChildren().clear();
                update.disableProperty().bind(
                        Bindings.isEmpty(nameField.textProperty())
                        .or(Bindings.isEmpty(vendorField.textProperty()))
                        .or(Bindings.isEmpty(priceField.textProperty())));
                editItemStage.close();
            }
        });
        return pane;
    }
    
    private GridPane pane2() {
        pane2 = new GridPane();
        pane2.setGridLinesVisible(true);
        pane2.setPadding(new Insets(5,5,5,5));
        if (item.getRecipe().getNumOfIngred() > 1) {
            for (int i = 0; i < item.getRecipe().getRecipeArray().length; i = i + 2) {
                TextField newQuantityField = new TextField(item.getRecipe().getRecipeArray()[i]);
                TextField newIngredientField = new TextField(item.getRecipe().getRecipeArray()[i + 1]);
                setMinTextFieldSize(newQuantityField);
                setMinTextFieldSize(newIngredientField);
                newQuantityField.textProperty().addListener(textFieldResize(newQuantityField));
                newIngredientField.textProperty().addListener(textFieldResize(newIngredientField));
                quantityList.add(newQuantityField);
                ingredientList.add(newIngredientField);
                System.out.println(quantityList.size());
                System.out.println(ingredientList.size());
                update.disableProperty().bind(
                        Bindings.isEmpty(newQuantityField.textProperty())
                        .or(Bindings.isEmpty(newIngredientField.textProperty())));
                pane2.add(quantityList.get(i/2), 0, i/2);
                pane2.add(ingredientList.get(i/2), 1, i/2);
                editItemStage.sizeToScene();
            }
        }
        return pane2;
    }
    
    private HBox hbox() {
        hbox = new HBox();
        hbox.setPadding(new Insets(5,5,5,5));
        hbox.getChildren().addAll(update, cancel);
        return hbox;
    }
    
    private void setMinTextFieldSize(TextField field) {
        Text string = new Text(field.getText());
        double length = string.getLayoutBounds().getWidth();
        field.setMinWidth(length + 20);
    }
    
    private ChangeListener<String> textFieldResize(TextField anonField) {
        ChangeListener<String> textFieldResize = new ChangeListener<String>() {
            private TextField anonField;
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Text string = new Text(anonField.getText());
                double length = string.getLayoutBounds().getWidth();
                anonField.setMinWidth(length + 20);
                editItemStage.sizeToScene();
            }
            private ChangeListener<String> init(TextField var) {
                anonField = var;
                return this;
            }
        }.init(anonField);
        return textFieldResize;
    }
    
    public Stage getEditItemStage() {
        return editItemStage;
    }
}
