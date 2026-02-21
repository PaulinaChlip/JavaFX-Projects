import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.application.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.*;

//-----------------------------------------------  KLASY RYSOWANIA FIGUR -----------------------------------------------------
abstract class M_figure{
    double x;
    double y;
    Color color;

    abstract void draw(GraphicsContext gc);
    abstract double calc_area();
}

class M_circle extends M_figure {
    double radius;

    @Override
    void draw(GraphicsContext gc){
        gc.setFill(color);
        gc.fillOval(x, y, radius, radius);
    }

    @Override
    double calc_area(){
        return radius*radius*Math.PI;
    }
}

class M_oval extends M_figure {
    double width;
    double height;

    @Override
    void draw(GraphicsContext gc){
        gc.setFill(color);
        gc.fillOval(x, y, width, height);
    }

    @Override
    double calc_area(){
        return Math.PI*(width/2)*(height/2);

    }
}

class M_rectangle extends M_figure {
    double width;
    double height;

    @Override
    void draw(GraphicsContext gc){
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
    }

    @Override
    double calc_area(){
        return width*height;
    }
}

//----------------------------------------------------  MAIN RYSOWANIE --------------------------------------------------------
public class Main extends Application {
    private static final int FRAME_WIDTH  = 840;
    private static final int FRAME_HEIGHT = 680;
    GraphicsContext gc;
    Canvas canvas;
    Stage stage;
    double circle_area;
    double rect_area;
    double oval_area;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rysowanie i zliczanie pól figur");

        RadioButton rbCircle = new RadioButton("Circle");
        RadioButton rbRect = new RadioButton("Rectangle");
        RadioButton rbOval = new RadioButton("Oval");
        Button startButton = new Button("START");

        ToggleGroup group = new ToggleGroup();
        rbCircle.setToggleGroup(group);
        rbRect.setToggleGroup(group);
        rbOval.setToggleGroup(group);
        //rbCircle.setSelected(true);
        group.selectToggle(null);

        Label areaLabel = new Label("");
        areaLabel.setStyle("-fx-font-size: 16px;");


        HBox menu = new HBox(10);   // 10 = odstęp
        menu.getChildren().addAll(rbCircle, rbRect, rbOval,areaLabel, startButton);

        canvas = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        BorderPane root = new BorderPane();
        root.setTop(menu);
        root.setCenter(canvas);



        startButton.setOnAction(e -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            areaLabel.setText("");
            group.selectToggle(null);

            M_figure[] figures = start_canvas(canvas);
            circle_area=0.0;
            rect_area=0.0;
            oval_area=0.0;

            for (int i = 0; i < 30; i++) {
                if( i<10){
                    circle_area += figures[i].calc_area();
                }
                else if( i<20){
                    rect_area += figures[i].calc_area();
                }
                else{
                    oval_area += figures[i].calc_area();
                }

                figures[i].draw(gc);
            }
        });

        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selected = (RadioButton) newToggle;
                switch (selected.getText()) {
                    case "Circle" -> areaLabel.setText("Pola kół: " + String.format("%.3f", circle_area)+" px");
                    case "Rectangle" -> areaLabel.setText("Pola prostokątów: " + String.format("%.3f", rect_area)+" px");
                    case "Oval" -> areaLabel.setText("Pola owali: " + String.format("%.3f", oval_area)+" px");
                }
            }
        });

        Scene scene = new Scene(root, 960, 600);


        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            exit_dialog();
        });

        primaryStage.show();
    }


    public M_figure[] start_canvas(Canvas canvas){
        M_figure[] figures = new M_figure[30];
        for (int i = 0; i < 30; i++) { // 30 figur, 10 każdego typu
            int figureWidth = 50 + (int)(Math.random() * 40);   // losowa szerokość 10-40
            int figureHeight = 50 + (int)(Math.random() * 40);  // losowa wysokość 10-40
            Color col = Color.color(Math.random(), Math.random(), Math.random(),0.6);

            double x = Math.random() * (canvas.getWidth() - figureWidth);
            double y = Math.random() * (canvas.getHeight() - figureHeight);

            M_figure f;

            if (i < 10) { // Circle
                M_circle c = new M_circle();
                c.x = x;
                c.y = y;
                c.radius = figureWidth / 2.0; // promień = połowa szerokości
                c.color = col;
                //circle_area = circle_area + c.calc_area();
                f = c;
            } else if (i < 20) { // Rectangle
                M_rectangle r = new M_rectangle();
                r.x = x;
                r.y = y;
                r.width = figureWidth;
                r.height = figureHeight;
                r.color = col;
                //rect_area = rect_area + r.calc_area();
                f = r;
            } else { // Oval
                M_oval o = new M_oval();
                o.x = x;
                o.y = y;
                o.width = figureWidth;
                o.height = figureHeight;
                o.color = col;
                //oval_area = oval_area + o.calc_area();
                f = o;
            }

            figures[i] = f;
        }

        return figures;
    }


    public void exit_dialog()
    {
        System.out.println("exit dialog");

        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Do you really want to exit the program?.",
                ButtonType.YES, ButtonType.NO);

        alert.setResizable(true);
        alert.onShownProperty().addListener(e -> {
            Platform.runLater(() -> alert.setResizable(false));
        });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES){
            Platform.exit();
        }
        else {}
    }
}

