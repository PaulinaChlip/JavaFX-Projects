import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Mikroskop {
    protected boolean mode; // false = 2D (0), true = 3D (1)
    protected float zoom;
    protected String light_type;
    protected float light_intensity;
    protected float frequency;

    public Mikroskop() {
        set_default();
    }

    // metoda dopasowująca parametry
    protected void set_default() {
        this.zoom = 1.0f;
        this.light_type = "LED";
        this.light_intensity = 100.0f;
        this.frequency = 50.0f;
    }

    // metoda zapisu
    protected void save_frame(byte[] data, int width, int height, String format, File file) {
        if (data == null || file == null) return;

        try {
            // tworzenie pustego buforu w formacie RGB
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // wypełnienie go danymi z tablicy bajtów
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int r = data[(y * width + x) * 3] & 0xFF;
                    int g = data[(y * width + x) * 3 + 1] & 0xFF;
                    int b = data[(y * width + x) * 3 + 2] & 0xFF;
                    int rgb = (r << 16) | (g << 8) | b;
                    bufferedImage.setRGB(x, y, rgb);
                }
            }

            // zapis na dysku
            ImageIO.write(bufferedImage, format, file);
            System.out.println("Pomyślnie zapisano: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu pliku: " + e.getMessage());
        }
    }

    public boolean isMode() { return mode; }
    public void setMode(boolean mode) { this.mode = mode; }

    public float getZoom() { return zoom; }
    public void setZoom(float zoom) { this.zoom = zoom; }

    public String getLight_type() { return light_type; }
    public void setLight_type(String light_type) { this.light_type = light_type; }

    public float getLight_intensity() { return light_intensity; }
    public void setLight_intensity(float light_intensity) { this.light_intensity = light_intensity; }

    public float getFrequency() { return frequency; }
    public void setFrequency(float frequency) { this.frequency = frequency; }
}


class Mikroskop_2D extends Mikroskop {

    // parametry dla możliwej ruchomej głowicy
    private float position_x;
    private float position_y;

    public Mikroskop_2D() {
        super();
        this.mode = false;
        this.position_x = 0.0f;
        this.position_y = 0.0f;
    }

    @Override
    protected void set_default() {
        super.set_default(); 
        this.position_x = 320.0f;
        this.position_y = 240.0f;
    }

    @Override
    protected void save_frame(byte[] data, int width, int height, String format, File file) {
        System.out.println("Przygotowanie zapisu 2D z pozycji: X=" + position_x + " Y=" + position_y);
        super.save_frame(data, width, height, format, file);
    }

    public float getPosition_x() { return position_x; }
    public void setPosition_x(float position_x) { this.position_x = position_x; }

    public float getPosition_y() { return position_y; }
    public void setPosition_y(float position_y) { this.position_y = position_y; }
}


// klasa dla trybu 3D
class Mikroskop_3D extends Mikroskop {
    public Mikroskop_3D() {
        super();
        this.mode = true;
    }

    // mozliwość rozbudowy
}
