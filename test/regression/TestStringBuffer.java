import josx.util.Assertion;
import josx.platform.rcx.*;

public class TestStringBuffer
{
        
    public static void main(String []args)
    {
        StringBuffer s1 = new StringBuffer(30);
        s1.append("2^10=");
        s1.append(1024);
        s1.append("!");
        Assertion.test(s1.toString(), "2^10=1024!".equals(s1.toString()));

        LCD.showNumber(1);
                
        s1.delete(0,30);
        s1.append("one=");
        s1.append(1.0);
        s1.append("!!");
        Assertion.test(s1.toString(), "one=1.0!!".equals(s1.toString()));
        
        LCD.showNumber(2);
                
        s1.delete(0,30);
        s1.append("pi=");
        s1.append(3.1415927);
        s1.append("!");
        Assertion.test(s1.toString(), "pi=3.1415927!".equals(s1.toString()));
        
        LCD.showNumber(3);
                
        s1.delete(0,30);
        s1.append("pi!=");
        s1.append(-3.1415927);
        s1.append("!");
        Assertion.test(s1.toString(), "pi!=-3.1415927!".equals(s1.toString()));
        
        LCD.showNumber(4);
                
        s1.delete(0,30);
        s1.append("pi!=");
        s1.append(3.1415927e30);
        s1.append("!");
        Assertion.test(s1.toString(), "pi!=3.1415927E30!".equals(s1.toString()));
        
        LCD.showNumber(5);
                
        s1.delete(0,30);
        s1.append("pi!=");
        s1.append(3.1415927e-30);
        s1.append("!");
        Assertion.test(s1.toString(), "pi!=3.1415925E-30!".equals(s1.toString()));
        
        LCD.showNumber(6);
                
        s1.delete(0,30);
        s1.append("pi!=");
        s1.append(-3.1415927e30);
        s1.append("!");
        Assertion.test(s1.toString(), "pi!=-3.1415927E30!".equals(s1.toString()));
        
        LCD.showNumber(7);
                
        s1.delete(0,30);
        s1.append("pi!=");
        s1.append(-3.1415927e-30);
        s1.append("!");
        Assertion.test(s1.toString(), "pi!=-3.1415925E-30!".equals(s1.toString()));        
    }
}
