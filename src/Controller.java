import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Stack;

/*
    The following application does the following:
    Undo, Save, Load, different brush Sizes, color customization, Clear,
 */

public class Controller {

    Stack<Image> undoStack = new Stack<>();
    Stack<Image> redoStack = new Stack<>();

    @FXML
    private BorderPane borderPane;
    
    @FXML
    private Canvas canvas;

    @FXML
    private ComboBox brushSize;
    @FXML
    private ComboBox drawShape;

    @FXML
    private Slider redSlider;
    @FXML
    private Slider greenSlider;
    @FXML
    private Slider blueSlider;
    @FXML
    private Slider alphaSlider;

    @FXML
    private TextField redTF;
    @FXML
    private TextField greenTF;
    @FXML
    private TextField blueTF;
    @FXML
    private TextField alphaTF;

    @FXML
    private CheckBox eraser;

    @FXML
    private TextArea addText;

    private static final double INIT_VALUE = 0;

    Line line = new Line();
    Rectangle rectangle = new Rectangle();
    Circle circle = new Circle();

    private double leftBoundX;
    private double leftBoundY;

    public void initialize(){
    	
    	borderPane.setStyle("-fx-background-color: white");
    	
        GraphicsContext gc = canvas.getGraphicsContext2D();


        canvas.setCursor(Cursor.CROSSHAIR);
        //setting initial value of color to white (0,0,0,0)
        redSlider.setValue(INIT_VALUE);
        redTF.setText(Double.toString(INIT_VALUE));

        greenSlider.setValue(INIT_VALUE);
        greenTF.setText(Double.toString(INIT_VALUE));

        blueSlider.setValue(INIT_VALUE);
        blueTF.setText(Double.toString(INIT_VALUE));

        alphaSlider.setValue(INIT_VALUE);
        alphaTF.setText(Double.toString(INIT_VALUE));

        //binding slider to textField

        redTF.textProperty().bindBidirectional(redSlider.valueProperty(), NumberFormat.getNumberInstance());
        greenTF.textProperty().bindBidirectional(greenSlider.valueProperty(), NumberFormat.getNumberInstance());
        blueTF.textProperty().bindBidirectional(blueSlider.valueProperty(), NumberFormat.getNumberInstance());
        alphaTF.textProperty().bindBidirectional(alphaSlider.valueProperty(), NumberFormat.getNumberInstance());

        //when the mouse is pressed we need to get the size of brush and the color selected

        canvas.setOnMousePressed(e -> {

            //the brush size is extracted from the comboBox by referring to the index of selected item
            double size;
            if(brushSize.getSelectionModel().getSelectedIndex()==0){
                size=5;
            }else if(brushSize.getSelectionModel().getSelectedIndex()==1){
                size=25;
            }else{
                size=60;
            }

            //since user can enter a number larger than 255, the textField updates to the maximum value = 255, same for opacity > 1
            if(Double.parseDouble(redTF.getText())>255)
                redTF.setText("255");
            if(Double.parseDouble(greenTF.getText())>255)
                greenTF.setText("255");
            if(Double.parseDouble(blueTF.getText())>255)
                blueTF.setText("255");
            if(Double.parseDouble(alphaTF.getText())>1)
                alphaTF.setText("1.0");

            //extract the RGBA values from the slider property
            double redColor=Double.parseDouble(redSlider.getValue()+"");
            double greenColor=Double.parseDouble(greenSlider.getValue()+"");
            double blueColor=Double.parseDouble(blueSlider.getValue()+"");
            double alpha=Double.parseDouble(alphaSlider.getValue()+"");

            //creating the user selected color from the above rgba values
            Color color = Color.rgb((int) redColor,(int) greenColor,(int) blueColor, alpha);

            // User in eraser mode
            if(eraser.isSelected()) {
                pushUndo();

                //clear the pixels in a rectangular area where the corner is at (x-size/2,y-size/2) and size specified
                //by the width and height (size, size)

                gc.clearRect(e.getX()-size/2,e.getY()-size/2, size, size);

                //User in free drawing mode
            }

            // User in free drawing mode
            if(!eraser.isSelected() && drawShape.getSelectionModel().getSelectedIndex()==0) {
                pushUndo();
                gc.beginPath();
                gc.setLineWidth(size);
                gc.setStroke(color);
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
            //User in line drawing mode
            else if(!eraser.isSelected() && drawShape.getSelectionModel().getSelectedIndex()==1) {
                gc.setStroke(color);
                line.setStartX(e.getX()); // setting starting X value
                line.setStartY(e.getY()); // setting starting Y value
            }
            // User in rectangle drawing mode
            else if(!eraser.isSelected() && (drawShape.getSelectionModel().getSelectedIndex()==2 || drawShape.getSelectionModel().getSelectedIndex()==3)) {
                gc.setFill(color);
                gc.setStroke(color);
                rectangle.setX(e.getX());
                rectangle.setY(e.getY());
            }
            // User in circle drawing mode
            else if(!eraser.isSelected() && (drawShape.getSelectionModel().getSelectedIndex()==4 || drawShape.getSelectionModel().getSelectedIndex()==5)) {
                gc.setFill(color);
                gc.setStroke(color);
                leftBoundX=e.getX();
                leftBoundY=e.getY();
            }

            else if(!eraser.isSelected() && drawShape.getSelectionModel().getSelectedIndex()==6) {
                pushUndo();
                gc.setFill(color);      // change color of text to match color selected by user
                gc.setFont(Font.font(size+10));     //size chosen by user + 10 because text was so small
                gc.fillText(addText.getText(), e.getX(), e.getY());
            }
        });

        canvas.setOnMouseDragged(e -> {

            double size;
            if(brushSize.getSelectionModel().getSelectedIndex()==0){
                size=5;
            }else if(brushSize.getSelectionModel().getSelectedIndex()==1){
                size=25;
            }else{
                size=60;
            }

            if(Double.parseDouble(redTF.getText())>255)
                redTF.setText("255");
            if(Double.parseDouble(greenTF.getText())>255)
                greenTF.setText("255");
            if(Double.parseDouble(blueTF.getText())>255)
                blueTF.setText("255");
            if(Double.parseDouble(alphaTF.getText())>1)
                alphaTF.setText("1.0");

            double redColor=Double.parseDouble(redSlider.getValue()+"");
            double greenColor=Double.parseDouble(greenSlider.getValue()+"");
            double blueColor=Double.parseDouble(blueSlider.getValue()+"");
            double alpha=Double.parseDouble(alphaSlider.getValue()+"");

            Color color = Color.rgb((int) redColor,(int) greenColor,(int) blueColor, alpha);

            //User is erasing
            if(eraser.isSelected()) {

                //clear the pixels in a rectangular area where the corner is at (x-size/2,y-size/2) and size specified
                //by the width and height (size, size)

                gc.clearRect(e.getX()-size/2,e.getY()-size/2, size, size);

                //User in free drawing mode
            }else if(drawShape.getSelectionModel().getSelectedIndex()==0){
                gc.setLineWidth(size);
                gc.setStroke(color);
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseReleased(e -> {

            double size;
            if(brushSize.getSelectionModel().getSelectedIndex()==0){
                size=5;
            }else if(brushSize.getSelectionModel().getSelectedIndex()==1){
                size=25;
            }else{
                size=60;
            }

            if(Double.parseDouble(redTF.getText())>255)
                redTF.setText("255");
            if(Double.parseDouble(greenTF.getText())>255)
                greenTF.setText("255");
            if(Double.parseDouble(blueTF.getText())>255)
                blueTF.setText("255");
            if(Double.parseDouble(alphaTF.getText())>1)
                alphaTF.setText("1.0");

            double redColor=Double.parseDouble(redSlider.getValue()+"");
            double greenColor=Double.parseDouble(greenSlider.getValue()+"");
            double blueColor=Double.parseDouble(blueSlider.getValue()+"");
            double alpha=Double.parseDouble(alphaSlider.getValue()+"");

            Color color = Color.rgb((int) redColor,(int) greenColor,(int) blueColor, alpha);

            // User in line drawing mode
            if(!eraser.isSelected() && drawShape.getSelectionModel().getSelectedIndex()==1) {
            	
            	pushUndo(); // add to stack in case we want to undo
            	
                line.setEndX(e.getX()); //setting ending X value
                line.setEndY(e.getY()); //setting ending Y value
                gc.setLineWidth(size); //line width is the same as the width defined by user
                gc.setStroke(color);
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
                //draw the line from starting X&Y values to end X&Y values

                
            }
            // User in transparent rectangle drawing mode
            else if(!eraser.isSelected() && (drawShape.getSelectionModel().getSelectedIndex()==2||
            		drawShape.getSelectionModel().getSelectedIndex()==3)) {
            	
                	pushUndo();// add to stack in case we want to undo
            	
                	rectangle.setWidth(Math.abs((e.getX() - rectangle.getX()))); //width=absolute difference btw end
                // position (mouse position) and rectangle start pos
                	rectangle.setHeight(Math.abs((e.getY() - rectangle.getY())));//height=absolute difference btw end position
                	gc.setStroke(color);
                    gc.setLineWidth(5);
                	
                	// to be able to draw rectangle correctly, we need to check for compare the start x&y and end x&y
                    if (rectangle.getX() < e.getX() && rectangle.getY() < e.getY()) {

                        if(drawShape.getSelectionModel().getSelectedIndex()==2) // transparent rect
                            gc.rect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                        else    // fill rect
                            gc.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                        gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                    }
                    else if(rectangle.getX() < e.getX() && rectangle.getY() > e.getY()) {

                        if(drawShape.getSelectionModel().getSelectedIndex()==2) // transparent rect
                            gc.rect(rectangle.getX(), rectangle.getY()-rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
                        else    // fill rect
                            gc.fillRect(rectangle.getX(), rectangle.getY()-rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
                        gc.strokeRect(rectangle.getX(), rectangle.getY()-rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
                    }
                    else if(rectangle.getX() > e.getX() && rectangle.getY() > e.getY()) {

                        if(drawShape.getSelectionModel().getSelectedIndex()==2) // transparent rect
                            gc.rect(rectangle.getX()-rectangle.getWidth(), rectangle.getY()-rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
                        else    // fill rect
                                gc.fillRect(rectangle.getX()-rectangle.getWidth(), rectangle.getY()-rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
                        gc.strokeRect(rectangle.getX()-rectangle.getWidth(), rectangle.getY()-rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
                    }
                    else {
                        if (drawShape.getSelectionModel().getSelectedIndex() == 2)  // transparent rect
                            gc.rect(rectangle.getX() - rectangle.getWidth(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                        else    // fill rect
                            gc.fillRect(rectangle.getX() - rectangle.getWidth(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                        gc.strokeRect(rectangle.getX() - rectangle.getWidth(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
                    }
            }

            // User in circle drawing mode
            else if(!eraser.isSelected() && (drawShape.getSelectionModel().getSelectedIndex()==4||
            		drawShape.getSelectionModel().getSelectedIndex()==5) ) {

            		pushUndo();// add to stack in case we want to undo
                    gc.setLineWidth(5);
            		circle.setCenterX((e.getX()+leftBoundX)/2);
            		circle.setCenterY((e.getY()+leftBoundY)/2);
            		circle.setRadius(Math.sqrt(Math.pow((Math.abs(e.getX() - circle.getCenterX())),2)
                                                + Math.pow(Math.abs(e.getY() - circle.getCenterY()),2)));

                // to be able to draw circle correctly, we need to check for compare the start x&y and end x&y
                    if (leftBoundX < e.getX() && leftBoundY < e.getY()) {

                        if (drawShape.getSelectionModel().getSelectedIndex() == 4) // transparent circle
                            gc.strokeOval(leftBoundX, leftBoundY, 2 * circle.getRadius(), 2 * circle.getRadius());
                        else    // fill circle
                            gc.fillOval(leftBoundX, leftBoundY, 2 * circle.getRadius(), 2 * circle.getRadius());
                    }
                    else if (leftBoundX < e.getX() && leftBoundY > e.getY()) {

                        if (drawShape.getSelectionModel().getSelectedIndex() == 4) // transparent circle
                            gc.strokeOval(leftBoundX, leftBoundY-2*circle.getRadius(), 2 * circle.getRadius(), 2 * circle.getRadius());
                        else    // fill circle
                            gc.fillOval(leftBoundX, leftBoundY-2*circle.getRadius(), 2 * circle.getRadius(), 2 * circle.getRadius());
                    }
                    else if (leftBoundX > e.getX() && leftBoundY < e.getY()) {

                        if (drawShape.getSelectionModel().getSelectedIndex() == 4) // transparent circle
                            gc.strokeOval(leftBoundX-2*circle.getRadius(), leftBoundY, 2 * circle.getRadius(), 2 * circle.getRadius());
                        else    // fill circle
                            gc.fillOval(leftBoundX-2*circle.getRadius(), leftBoundY, 2 * circle.getRadius(), 2 * circle.getRadius());
                    }
                    else {
                        if (drawShape.getSelectionModel().getSelectedIndex() == 4) // transparent circle
                            gc.strokeOval(leftBoundX-2*circle.getRadius(), leftBoundY-2*circle.getRadius(), 2 * circle.getRadius(), 2 * circle.getRadius());
                        else    // fill circle
                            gc.fillOval(leftBoundX-2*circle.getRadius(), leftBoundY-2*circle.getRadius(), 2 * circle.getRadius(), 2 * circle.getRadius());
                    }
            }
        });

    }

    // save a snapshot of the canvas to the disk
    public void onSave(){

        FileChooser savefile = new FileChooser();
        savefile.setTitle("Save File");
        File file = savefile.showSaveDialog(Main.primaryStage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
                System.out.println("Error!");
            }
        }
    }

    // Load an image from the disk
    public void onLoad(){
            FileChooser openFile = new FileChooser();
            openFile.setTitle("Open File");
            File file = openFile.showOpenDialog(Main.primaryStage);

            if (file != null) {
                try {
                    InputStream io = new FileInputStream(file);
                    Image img = new Image(io);
                    canvas.getGraphicsContext2D().drawImage(img, 0, 0);
                } catch (IOException ex) {
                    System.out.println("Error!");
                }
            }
    }

     // clear the pixels in a rectangular area where the corner is at (0,0) and size specified
     // by the width and height of the whole canvas
    public void onClear(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void pushUndo(){
        Image snapshot = canvas.snapshot(null, null);   // take snapshot of current canvas
        undoStack.push(snapshot);   // push a snapshot of the canvas
    }

    public void undo() {
        if (!undoStack.empty()) {
            Image undoImage = undoStack.pop();
            Image tempImage = undoImage;
            canvas.getGraphicsContext2D().drawImage(undoImage, 0, 0);   // draw popped image on canvas
            redoStack.push(tempImage);
        }
    }

    public void redo() {
        if (!redoStack.empty()) {
            Image redoImage = redoStack.pop();
            Image tempImage = redoImage;
            canvas.getGraphicsContext2D().drawImage(redoImage, 0, 0);   // draw popped image on canvas
            undoStack.push(tempImage);
        }
    }

}
