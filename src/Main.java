import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application {

    public static Stage primaryStage;
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage=stage;
        FXMLLoader loader =  new FXMLLoader(getClass().getResource("paint.fxml"));
        Parent root = loader.load();
        Controller myController = loader.getController();
        
        Rectangle2D screenbounds = Screen.getPrimary().getBounds();
        stage.setWidth(screenbounds.getWidth());
        stage.setHeight(screenbounds.getHeight());
        Scene scene = new Scene(root);
        
        //this code block handles key-events (shortcuts)
        scene.setOnKeyPressed(event -> {
			if(event.getCode().toString().equalsIgnoreCase("Z"))
				myController.undo();
			if(event.getCode().toString().equalsIgnoreCase("Y"))
				myController.redo();
			if(event.getCode().toString().equalsIgnoreCase("C"))
				myController.onClear();
			if(event.getCode().toString().equalsIgnoreCase("S"))
				myController.onSave();
			if(event.getCode().toString().equalsIgnoreCase("L"))
				myController.onLoad();
		});
        stage.setScene(scene);
        
        Image icon = new Image("palette_icon.png");

        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setTitle("Paint Application");
        stage.getIcons().add(icon);
        stage.show();
        
        //this code block checks if the user wants to save their work before closing
        stage.setOnCloseRequest(event -> {Alert alert = new Alert(AlertType.CONFIRMATION);
        								 alert.getButtonTypes().removeAll(ButtonType.OK);
        								 alert.getButtonTypes().add(ButtonType.YES);
        								 alert.getButtonTypes().add(ButtonType.NO);
        								 
        								 alert.setTitle("Close PaintApp");
        								 alert.setHeaderText("You're about to close PaintApp!");
        								 alert.setContentText("Do you want to save?");

										Optional<ButtonType> result = alert.showAndWait();

										// the user wants to save
        								 if (result.get() == ButtonType.YES) {
        									 myController.onSave();
        									 stage.close();
        								 }
										 // the user don't want to save
        								 else if (result.get() == ButtonType.NO) {
        									 stage.close();
        								 }
										 // the user wants to stay in the paint application
        								 else if (result.get() == ButtonType.CANCEL) {
        									 event.consume();
        									 alert.close();
        								 }
        	
        								 });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
