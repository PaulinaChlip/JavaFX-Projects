import java.nio.ByteBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelFormat;


public class Picture {
    public byte[] picture_frame;
    public double brightness = 0.0;
    public double contrast = 1.0;
    public boolean negative = false;
    public double saturation = 1.0;
    public double zoom = 1.0;


    public Picture() {}

    public Picture(byte[] frame) {
        this.picture_frame = frame;
    }


    public void applyBrightnessContrast(double new_contrast, double new_brightness) {
        if (picture_frame == null) return;
        for (int i = 0; i < picture_frame.length; i++) {
            int v = picture_frame[i] & 0xFF;
            v = (int) ((v - 128) * new_contrast + 128 + new_brightness);
            picture_frame[i] = (byte) Math.max(0, Math.min(255, v));
        }
    }

    public void applyNegative() {
        if (picture_frame == null) return;
        if (!negative) return;

        for (int i = 0; i < picture_frame.length; i++) {
            picture_frame[i] = (byte) (255 - (picture_frame[i] & 0xFF));
        }
    }


    public void applySaturation(double sat) {
        if (picture_frame == null) return;

        for (int i = 0; i < picture_frame.length; i += 3) {
            int r = picture_frame[i] & 0xFF;
            int g = picture_frame[i + 1] & 0xFF;
            int b = picture_frame[i + 2] & 0xFF;

            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

            r = (int) (gray + sat * (r - gray));
            g = (int) (gray + sat * (g - gray));
            b = (int) (gray + sat * (b - gray));

            picture_frame[i] = (byte) Math.max(0, Math.min(255, r));
            picture_frame[i + 1] = (byte) Math.max(0, Math.min(255, g));
            picture_frame[i + 2] = (byte) Math.max(0, Math.min(255, b));
        }
    }


    public byte[] cropAndScale(int frameWidth, int frameHeight, int srcWidth, int srcHeight, double cx, double cy) {
        if (picture_frame == null) return null;

        byte[] out = new byte[frameWidth * frameHeight * 3];

        int cropW = (int)(srcWidth / zoom);
        int cropH = (int)(srcHeight / zoom);

        int startX = (int)(cx - cropW / 2.0);
        int startY = (int)(cy - cropH / 2.0);
        
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;
        if (startX + cropW > srcWidth) startX = srcWidth - cropW;
        if (startY + cropH > srcHeight) startY = srcHeight - cropH;

        for (int y = 0; y < frameHeight; y++) {
            int srcY = startY + (y * cropH / frameHeight);
            if (srcY >= srcHeight) srcY = srcHeight - 1;

            for (int x = 0; x < frameWidth; x++) {
                int srcX = startX + (x * cropW / frameWidth);
                if (srcX >= srcWidth) srcX = srcWidth - 1;

                int srcIdx = (srcY * srcWidth + srcX) * 3;
                int dstIdx = (y * frameWidth + x) * 3;

                out[dstIdx]     = picture_frame[srcIdx];
                out[dstIdx + 1] = picture_frame[srcIdx + 1];
                out[dstIdx + 2] = picture_frame[srcIdx + 2];
            }
        }
        return out;
    }


    public byte[] cropAndScale2(int frameWidth, int frameHeight, double centerX, double centerY) {
        if (picture_frame == null) return null;

        byte[] out = new byte[frameWidth * frameHeight * 3];

        int srcWidth = frameWidth; // szerokość oryginalnego obrazu
        int srcHeight = frameHeight; // wysokość oryginalnego obrazu

        int cropW = (int) (srcWidth / zoom);
        int cropH = (int) (srcHeight / zoom);
        
        int startX = (int) Math.max(0, Math.min(srcWidth - cropW, centerX - cropW / 2));
        int startY = (int) Math.max(0, Math.min(srcHeight - cropH, centerY - cropH / 2));

        for (int y = 0; y < frameHeight; y++) {
            int srcY = startY + y * cropH / frameHeight;
            if (srcY >= srcHeight) srcY = srcHeight - 1;

            for (int x = 0; x < frameWidth; x++) {
                int srcX = startX + x * cropW / frameWidth;
                if (srcX >= srcWidth) srcX = srcWidth - 1;

                int srcIdx = (srcY * srcWidth + srcX) * 3;
                int dstIdx = (y * frameWidth + x) * 3;

                out[dstIdx] = picture_frame[srcIdx];
                out[dstIdx + 1] = picture_frame[srcIdx + 1];
                out[dstIdx + 2] = picture_frame[srcIdx + 2];
            }
        }
        return out;
    }


    public void show_obok(GraphicsContext gc, byte[] frameData, int canvasX, int canvasY, int drawWidth, int drawHeight) {
        if (frameData == null) return;

        PixelWriter pw = gc.getPixelWriter();
        PixelFormat<ByteBuffer> pf = PixelFormat.getByteRgbInstance();

        pw.setPixels(canvasX, canvasY, drawWidth, drawHeight, pf, frameData, 0, drawWidth * 3);
    }


    public void applyOverlay(byte[] overlayFrame, double opacity) {
        if (this.picture_frame == null || overlayFrame == null) return;
        
        for (int i = 0; i < picture_frame.length; i++) {
            int base = this.picture_frame[i] & 0xFF;
            int over = overlayFrame[i] & 0xFF;
            
            int mixed = (int) ((1.0 - opacity) * base + opacity * over);
            this.picture_frame[i] = (byte) mixed;
        }
    }

    public void reset() {  //nie używana aktualnie, zostawiam na przyszłość
        brightness = 0.0;
        contrast   = 1.0;
        saturation = 1.0;
        negative   = false;
    }
}
