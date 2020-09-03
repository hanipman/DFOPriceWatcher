/**
 * 
 */
package pricemonitor;

import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Chris Boado 905714629
 * @version May 2, 2016
 */
public class DeleteItemWindow {
    private Stage deleteItemStage;
    private Scene scene;
    private BorderPane bpane;
    private GridPane gpane;
    
    Text text;
    
    Button yes;
    Button no;
    
    ConnectToDatabase db;
    
    public DeleteItemWindow(ConnectToDatabase db, String itemName) {
        this.db = db;
        text = new Text("Are you sure you want to delete " + itemName + " from the database?");
        Button yes = new Button("Yes");
        yes.setMinWidth(100);
        yes.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try {
                    db.deleteItem(itemName);
                    deleteItemStage.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        Button no = new Button("No");
        no.setMinWidth(100);
        no.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                deleteItemStage.close();
            }
        });
        gpane = new GridPane();
        for (int i = 2; i > 0; i--) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            gpane.getColumnConstraints().add(col);
        }
        gpane.add(yes, 0, 0);
        gpane.add(no, 1, 0);
        GridPane.setHalignment(yes, HPos.CENTER);
        GridPane.setHalignment(no, HPos.CENTER);
        bpane = new BorderPane();
        bpane.setCenter(text);
        bpane.setBottom(gpane);
        scene = new Scene(bpane);
        bpane.prefHeightProperty().bind(scene.heightProperty());
        bpane.prefWidthProperty().bind(scene.widthProperty());
        BorderPane.setAlignment(text, Pos.CENTER);
        bpane.setPadding(new Insets(5,5,5,5));

        //scene = new Scene(bpane);
        deleteItemStage = new Stage();
        deleteItemStage.setScene(scene);
        deleteItemStage.setTitle("Delete " + itemName + "?");
        deleteItemStage.setResizable(false);
        //Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        //deleteItemStage.setX((primScreenBounds.getWidth() - deleteItemStage.getWidth()) / 2);
        //deleteItemStage.setY((primScreenBounds.getHeight() - deleteItemStage.getHeight()) / 2);
        deleteItemStage.initModality(Modality.APPLICATION_MODAL);
        deleteItemStage.show();
    }
}
