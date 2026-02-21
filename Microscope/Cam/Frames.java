package cam;

import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/*
// original version: for reading from microscope
public class Frames {
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;

    static public native int open_shm(String shm_name);

    static public native byte[] get_frame();

    static public native byte[] get_frame_from_file(String file_name);

    int RGB_pixels[];

    public BufferedImage bi;

    public Frames() {
        System.loadLibrary("frames");

        RGB_pixels = new int[FRAME_WIDTH * FRAME_HEIGHT];
    }

    public BufferedImage convert_to_BI(byte buffer[]) {
        int i, j;

        j = 0;
        for (i = 0; i < RGB_pixels.length; i++) {
            RGB_pixels[i] = (int) (buffer[j] << 16) + (buffer[j + 1] << 8) + buffer[j + 2];
            j += 3;
        }

        bi = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);

        bi.setRGB(0, 0, FRAME_WIDTH, FRAME_HEIGHT, RGB_pixels, 0, FRAME_WIDTH);

        return bi;
    }

    public get_frame_from_file(String file_name) {

    }
}
*/

// version for reading from a file
public class Frames {
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;

    // static public native int   open_shm(String shm_name);
    // static public native byte[] get_frame();

    int RGB_pixels[];

    public Frames() {
        // System.loadLibrary("frames");
        RGB_pixels = new int[FRAME_WIDTH * FRAME_HEIGHT];
    }

    public byte[] get_frame_from_file(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                System.err.println("Nie można odczytać pliku obrazu!");
                return new byte[FRAME_WIDTH * FRAME_HEIGHT * 3];
            }

            if (img.getWidth() != FRAME_WIDTH || img.getHeight() != FRAME_HEIGHT) {
                java.awt.Image tmp = img.getScaledInstance(FRAME_WIDTH, FRAME_HEIGHT, java.awt.Image.SCALE_SMOOTH);
                BufferedImage scaled = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
                scaled.getGraphics().drawImage(tmp, 0, 0, null);
                img = scaled;
            }

            byte[] buffer = new byte[FRAME_WIDTH * FRAME_HEIGHT * 3];
            int idx = 0;
            for (int y = 0; y < FRAME_HEIGHT; y++) {
                for (int x = 0; x < FRAME_WIDTH; x++) {
                    int rgb = img.getRGB(x, y);
                    buffer[idx++] = (byte)((rgb >> 16) & 0xFF);
                    buffer[idx++] = (byte)((rgb >> 8) & 0xFF);
                    buffer[idx++] = (byte)(rgb & 0xFF);
                }
            }
            return buffer;
        }

        catch (Exception e) {
            e.printStackTrace();
            return new byte[FRAME_WIDTH * FRAME_HEIGHT * 3];
        }
    }
}
