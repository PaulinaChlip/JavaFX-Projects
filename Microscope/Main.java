import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.nio.ByteBuffer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.image.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelFormat;
import Cam.Frames;


public class Main extends Application {
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;
    double brightness = 0.0; 
    double contrast   = 1.0; 
    boolean negative = false;
    double saturation = 1.0;
    double zoom=1.0;
    double centerX = 320.0;
    double centerY = 240.0;
    double lastMouseX;
    double lastMouseY;
    private boolean isLiveMode = true; 
    private boolean showBothPics = false;  
    double overlayAlpha = 0.5; 
    byte[] overlayImage = null; 
    private Canvas canvas2;
    private GraphicsContext gc2;
    private byte[] buffer2;
    private boolean dualMode=false;
    private boolean overlayMode=false;
    private byte[] lastProcessedBuffer;
    private String currentFilePath = "Pictures/cute_cat.jpg";
    private String placeholderImagePath = "Pictures/cute_cat.jpg";

    GraphicsContext gc;
    Canvas canvas;
    byte buffer[];
    PixelWriter pixelWriter;
    PixelFormat<ByteBuffer> pixelFormat;

    Frames frames;
         
    public static void main(String[] args) {launch(args);}
    private Mikroskop mikroskop;


    @Override
    public void start(Stage primaryStage){

        List<String> opcje = Arrays.asList("2D", "3D");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("2D", opcje);
        dialog.setTitle("Wybór trybu");
        dialog.setHeaderText("Wybierz tryb pracy");
        dialog.setContentText("Dostępne tryby:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String wybor = result.get();
            if (wybor.equals("3D")) {
                this.mikroskop = new Mikroskop_3D();
                this.mikroskop.setMode(true);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd urządzenia");
                alert.setHeaderText(null);
                alert.setContentText("Brak odpowiedniego urządzenia");
                alert.showAndWait();
                return;
            } else {
                this.mikroskop= new Mikroskop_2D();
                this.mikroskop.setMode(false);
                Set2DView(primaryStage);
            }
        }
    }


    private void Set2DView(Stage primaryStage){
        Timeline timeline;

        frames = new Frames();
        //int result;
        //result = frames.open_shm("/frames");

        primaryStage.setTitle("Camera");
        Scene scene;

        canvas = new Canvas(650, 490);
        gc = canvas.getGraphicsContext2D();

        canvas2 = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        gc2 = canvas2.getGraphicsContext2D();
        canvas2.setVisible(false); // ukrycie domyślne
        HBox canvasContainer= new HBox(10);
        canvasContainer.getChildren().addAll(canvas, canvas2);

        BorderPane root = new BorderPane();

        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setPrefWidth(200);
        leftPanel.setAlignment(Pos.TOP_CENTER);

        VBox centerPane = new VBox(canvas);
        centerPane.setAlignment(Pos.CENTER);

        root.setLeft(leftPanel);
        root.setCenter(centerPane);
      
        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            if (zoom > 1.0) {
                double deltaX = e.getX() - lastMouseX;
                double deltaY = e.getY() - lastMouseY;
                centerX -= deltaX / zoom;
                centerY -= deltaY / zoom;

                centerX = Math.max(0, Math.min(640, centerX));
                centerY = Math.max(0, Math.min(480, centerY));

                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        Label bLabel = new Label("Brightness");
        Slider brightnessSlider = new Slider(-100, 100, 0);
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightness = newVal.doubleValue();});

        Label cLabel = new Label("Contrast");
        Slider contrastSlider = new Slider(0.5, 2.0, 1.5);
        contrastSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            contrast = newVal.doubleValue();});

        Label sLabel = new Label("Saturation");
        Slider saturationSlider = new Slider(0.0, 2.0, 1.0);
        saturationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            saturation = newVal.doubleValue();});

        Label zLabel = new Label("Zoom");
        Slider zoomSlider = new Slider(1.0, 4.0, 1.0); // max zoom 4×
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoom = newVal.doubleValue();});

        Label oLabel = new Label("Opacity");
        Slider overlaySlider = new Slider(0.0, 1.0, 0.5); 
        overlaySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            overlayAlpha = newVal.doubleValue();
        });

        canvas.setOnScroll(e -> {
            double oldZoom = zoom;
            double delta = e.getDeltaY();
            double zoomFactor = 1.05;

            if (delta > 0) {
                zoom *= zoomFactor;
            } else if (delta < 0) {
                zoom /= zoomFactor;
            }

            zoom = Math.max(1.0, Math.min(4.0, zoom));

            if (oldZoom != zoom) {
                double mouseX = e.getX();
                double mouseY = e.getY();

                centerX += (mouseX - 320) / oldZoom * 0.1;
                centerY += (mouseY - 240) / oldZoom * 0.1;
            }

            zoomSlider.setValue(zoom);
        });

        Button negButton = new Button("Negative OFF");
        negButton.setOnAction(e -> {negative = !negative;negButton.setText(negative ? "Negative ON" : "Negative OFF");});

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            brightness = 0.0;
            contrast   = 1.0;
            saturation = 1.0;
            negative   = false;
            zoom = 1.0;
            centerX=320;
            centerY=240;

            brightnessSlider.setValue(brightness);
            contrastSlider.setValue(contrast);
            saturationSlider.setValue(saturation);
            zoomSlider.setValue(zoom);
            negButton.setText("Negative OFF");
        });

        Button modeButton = new Button("Mode: LIVE");
        modeButton.setOnAction(e -> {
            if (isLiveMode) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Wybierz obraz mikroskopowy");
                fileChooser.getExtensionFilters().addAll(
                        new javafx.stage.FileChooser.ExtensionFilter("Obrazy", "*.png", "*.jpg", "*.jpeg")
                );
                java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

                if (selectedFile != null) {
                    currentFilePath = selectedFile.getAbsolutePath();
                    isLiveMode = false;
                    modeButton.setText("Mode: FILE (" + selectedFile.getName() + ")");
                }
            } else {
                isLiveMode = true;
                modeButton.setText("Mode: LIVE");
            }
        });


        Button dualButton = new Button("Dual Mode: OFF");
        dualButton.setOnAction(e -> {
            dualMode = !dualMode;
            if (dualMode){
                javafx.stage.FileChooser fileChooser=new javafx.stage.FileChooser();
                fileChooser.setTitle("Wybierz obraz do porównania");
                fileChooser.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("Obrazy", "*.png", "*.jpg", "*.jpeg")
                );
                java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

                if (selectedFile != null) {
                    buffer2 = frames.get_frame_from_file(selectedFile.getAbsolutePath());
                    canvas2.setVisible(true);
                    dualButton.setText("Dual Mode: ON (" + selectedFile.getName() + ")");
                } else {
                    dualMode = false;
                    canvas2.setVisible(false);
                    dualButton.setText("Dual Mode: OFF");
                }
            } else {
                canvas2.setVisible(false);
                dualButton.setText("Dual Mode: OFF");
            }
        });
  
        Button saveButton = new Button("Save image");
        saveButton.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save image from Microscope");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Obraz JPG", "*.jpg"),
                    new javafx.stage.FileChooser.ExtensionFilter("Obraz PNG", "*.png")
            );

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                String ext = file.getName().endsWith(".png") ? "png" : "jpg";

                mikroskop.save_frame(lastProcessedBuffer, FRAME_WIDTH, FRAME_HEIGHT, ext, file);
            }
        });


        Button overlayButton = new Button("Overlay");
        overlayButton.setOnAction(e -> {
            overlayMode = !overlayMode; 

            if (overlayMode) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Wybierz obraz do nałożenia");
                fileChooser.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("Obrazy", "*.png", "*.jpg", "*.jpeg")
                );
                java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

                if (selectedFile != null) {
                    buffer2 = frames.get_frame_from_file(selectedFile.getAbsolutePath());
                    overlayButton.setText("Overlay Mode: ON (" + selectedFile.getName() + ")");
                } else {
                    overlayMode = false;
                    overlayButton.setText("Overlay Mode: OFF");
                }
            } else {
                overlayButton.setText("Overlay Mode: OFF");
            }
        });


        timeline = new Timeline(new KeyFrame(Duration.millis(130), e->disp_frame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        double controlWidth = 160;
        for (Slider slider : Arrays.asList(brightnessSlider, contrastSlider, saturationSlider, zoomSlider)) {
            slider.setPrefWidth(controlWidth);}
      
        for (Button button : Arrays.asList(negButton, overlayButton, saveButton,resetButton,dualButton, modeButton)) {
            button.setPrefWidth(controlWidth);
            button.setAlignment(Pos.CENTER);}

        centerPane.getChildren().add(canvasContainer);
        leftPanel.getChildren().addAll(
                bLabel, brightnessSlider,
                cLabel, contrastSlider,
                sLabel, saturationSlider,
                zLabel, zoomSlider,
                oLabel, overlaySlider,
                negButton,resetButton, modeButton, dualButton, saveButton, overlayButton);


        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void disp_frame(){
        pixelWriter = gc.getPixelWriter();
        pixelFormat = PixelFormat.getByteRgbInstance();

        //buffer = frames.get_frame();
        Picture obraz1 = new Picture();
        if (isLiveMode) {
            // live frames downoloading ( to uncomment if using a camera/microscope)
            // obraz.picture_frame = frames.get_frame();

            // placeholder image:
            obraz1.picture_frame = frames.get_frame_from_file(placeholderImagePath);
        } else {
            obraz1.picture_frame = frames.get_frame_from_file(currentFilePath);
        }
        obraz1.zoom = this.zoom;

        obraz1.applyBrightnessContrast(contrast, brightness);
        obraz1.applySaturation(saturation);
        obraz1.negative = negative;
        obraz1.applyNegative();

        if (overlayMode && buffer2 != null) {

            obraz1.applyOverlay(buffer2, overlayAlpha); 
        }
        byte[] outBuffer = obraz1.cropAndScale2(FRAME_WIDTH, FRAME_HEIGHT, centerX, centerY);

        this.lastProcessedBuffer = outBuffer;
        pixelWriter.setPixels(5, 5, FRAME_WIDTH, FRAME_HEIGHT, pixelFormat, outBuffer, 0, FRAME_WIDTH*3);

        if (dualMode && buffer2 != null) {
            Picture obraz2 = new Picture(buffer2);
            obraz2.show_obok(gc2, buffer2, 5, 5, FRAME_WIDTH, FRAME_HEIGHT);
        }
    }

}
