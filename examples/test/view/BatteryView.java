import josx.platform.rcx.*;

public class BatteryView implements PortView, LCDConstants, Segment
{
    BatteryView() {
    }

    public void showCursor() {
	LCD.setSegment(BATTERY);
    }

    public void showPort() {
    }

    public void showValues() {
	LCD.setNumber (LCD_PROGRAM, 0, 0);
	LCD.setNumber (LCD_SIGNED, (Battery.getVoltageMilliVolt()+5)/10, LCD_DECIMAL_2);
	LCD.setSegment (STANDING);
    }

    public void runPressed() {
    }

    public void prgmPressed() {
    }

    public void shutdown() {
    }
}
