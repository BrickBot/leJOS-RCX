package josx.vision;

import javax.media.*;
import javax.media.format.*;
import java.awt.*;

/*
 * Flips image Horizontally
 */
public class FlipEffect extends VisionEffect {

  public FlipEffect() {
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

    int lines = inData.length/ lineStrideIn;
    int pixsPerLine = lineStrideIn/pixStrideIn;

    byte [] buf = new byte[lineStrideIn];
    int pos = 0;

    for(int i=0;i<lines;i++) {
      // System.arraycopy(inData, pos, buf, 0, lineStrideIn);
      for(int j=0;j<pixsPerLine;j++) {
        for(int k=0;k<3;k++)
          buf[lineStrideIn - (j*pixStrideIn) -3 + k] = inData[pos + (j*pixStrideIn) + k];
      }
      System.arraycopy(buf,0,outData,pos,lineStrideIn);
      pos += lineStrideIn;
    }
    return BUFFER_PROCESSED_OK;
  }

  // methods for interface PlugIn
  public String getName() {
    return "Flip Effect";
  }
}
