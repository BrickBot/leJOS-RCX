package josx.vision;

/*
 * Supports detecting colored objects in regions
 */

import javax.media.*;
import javax.media.format.*;
import java.awt.*;

public class ColorEffect extends VisionEffect {
  private static final int PIXEL_THRESHOLD = 32;
  private static final int LIGHT_THRESHOLD = 192;

  public ColorEffect() {
    super();
  }

  public int process(Buffer inBuffer, Buffer outBuffer) {
    int outputDataLength = ((VideoFormat)outputFormat).getMaxDataLength();
    validateByteArraySize(outBuffer, outputDataLength);

    outBuffer.setLength(outputDataLength);
    outBuffer.setFormat(outputFormat);
    outBuffer.setFlags(inBuffer.getFlags());

    byte [] inData = (byte[]) inBuffer.getData();
    byte [] outData = (byte[]) outBuffer.getData();

    RGBFormat vfIn = (RGBFormat) inBuffer.getFormat();
    Dimension sizeIn = vfIn.getSize();

    int pixStrideIn = vfIn.getPixelStride();
    int lineStrideIn = vfIn.getLineStride();

    if ( outData.length < sizeIn.width*sizeIn.height*3 ) {
      System.out.println("the buffer is not full");
      return BUFFER_PROCESSED_FAILED;
    }

    System.arraycopy(inData,0,outData,0,inData.length);

    // Find the regions

    Region [] regions = Vision.getRegions();

    // Look for color listeners

    for(int i=0;i<regions.length;i++) {
      if (regions[i] != null) {
        ColorListener [] cl = regions[i].getColorListeners();
        int [] colors = regions[i].getColors();

        int rx = regions[i].getX();
        int ry = regions[i].getY();
        int width = regions[i].getWidth();
        int height = regions[i].getHeight();

        for(int j=0;j<cl.length;j++) {
          int r = (colors[j] >> 16) & 0xFF;
          int g = (colors[j] >> 8) & 0xFF;
          int b = colors[j] & 0xFF;

          // System.out.println("Looking for " + r + " , " + g + " , " + b);
         
          int pixCount = 0, totalPixs = 0;
          int aR = 0, aG = 0, aB = 0;

          for(int ii=ry; ii<ry+height; ii++) {
            for(int jj=rx; jj<rx+width; jj++) {
              int pos = ii*lineStrideIn + jj*pixStrideIn;

              int tr = inData[pos+2] & 0xFF;
              int tg = inData[pos+1] & 0xFF;
              int tb = inData[pos] & 0xFF;

              aR += tr;
              aG += tg;
              aB += tb;

              totalPixs++;

              if (Math.abs(tr - r) <= PIXEL_THRESHOLD &&
                  Math.abs(tg - g) <= PIXEL_THRESHOLD &&
                  Math.abs(tb - b) <= PIXEL_THRESHOLD) {
                pixCount++;
              }
            }       
          }
          // System.out.println("Matched " + pixCount + " out of " + totalPixs);
          
          if (Vision.captureColor) 
            System.out.println("Color = " + aR/totalPixs + " , " + aG/totalPixs + " , " + aB/totalPixs);

          if (pixCount > totalPixs/3) cl[j].colorDetected(i+1, colors[j]);
            
        }
      }
    }

    // Look for light listeners

    for(int i=0;i<regions.length;i++) {
      if (regions[i] != null) {
        LightListener [] ll = regions[i].getLightListeners();

        int rx = regions[i].getX();
        int ry = regions[i].getY();
        int width = regions[i].getWidth();
        int height = regions[i].getHeight();

        for(int j=0;j<ll.length;j++) {
         
          int pixCount = 0, totalPixs = 0;
          int aR = 0, aG = 0, aB = 0;

          for(int ii=ry; ii<ry+height; ii++) {
            for(int jj=rx; jj<rx+width; jj++) {
              int pos = ii*lineStrideIn + jj*pixStrideIn;

              int tr = inData[pos+2] & 0xFF;
              int tg = inData[pos+1] & 0xFF;
              int tb = inData[pos] & 0xFF;

              aR += tr;
              aG += tg;
              aB += tb;

              totalPixs++;

              if (tr >= LIGHT_THRESHOLD &&
                  tg >= LIGHT_THRESHOLD &&
                  tr >= LIGHT_THRESHOLD) {
                pixCount++;
              }
            }       
          }
          // System.out.println("Matched " + pixCount + " out of " + totalPixs);
          // System.out.println("Average = " + aR/totalPixs + " , " + aG/totalPixs + " , " + aB/totalPixs);

          if (pixCount > totalPixs/3) ll[j].lightDetected(i+1);
            
        }
      }
    }

    return BUFFER_PROCESSED_OK;
  }

  // methods for interface PlugIn
  public String getName() {
    return "Color Effect";
  }
}
