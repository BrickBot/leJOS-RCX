import josx.platform.rcx.*;

public class MotorView implements PortView, LCDConstants, Segment
{
    static final int MAXSTATE = 3;
    static final int MAXPOWER = 7;

    public static final MotorView A = new MotorView( Motor.A, 0);
    public static final MotorView B = new MotorView( Motor.B, 1);
    public static final MotorView C = new MotorView( Motor.C, 2);

    private static final int[] MOTOR_VIEW = 
	new int[] { MOTOR_A_VIEW, MOTOR_B_VIEW, MOTOR_C_VIEW};
    private static final int[] MOTOR_FORWARD = 
	new int[] { MOTOR_A_FWD, MOTOR_B_FWD, MOTOR_C_FWD};
    private static final int[] MOTOR_BACKWARD = 
	new int[] { MOTOR_A_REV, MOTOR_B_REV, MOTOR_C_REV};

    final Motor motor;

    int number;
    int state;
    int power;

    MotorView( Motor m, int n )
    {
	motor = m;
	number = n;

	state = 0;
	power = 7;
	setState();
	setPower();
    }

    void setPower()
    {
	motor.setPower(power);
    }

    void setState()
    {
	if (state == 0)
	    motor.flt();
	else if (state == 1)
	    motor.forward();
	else if (state == 2)
	    motor.backward();
	else if (state == 3)
	    motor.stop();
    }

    public void showCursor()
    {
	LCD.setSegment (MOTOR_VIEW[number]);
    }

    public void showPort()
    {
	if (state == 1 || state == 3) {
	    LCD.setSegment (MOTOR_FORWARD[number]);
	}
	if (state == 2 || state == 3) {
	    LCD.setSegment (MOTOR_BACKWARD[number]);
	}
    }

    public void showValues()
    {
	LCD.setNumber ( LCD_PROGRAM, power, 0);
    }

    public void runPressed()
    {
	state++;
	if (state > MAXSTATE)
	    state = 0;
	setState();
    }
    
    public void prgmPressed() {
	power++;
	if (power > MAXPOWER)
	    power = 0;
	setPower();
    }
    
    public void shutdown() {
	motor.stop();
    }
}
