Compiled from Test1.java
public synchronized class Test1 extends java.lang.Object 
    /* ACC_SUPER bit set */
{
    int x;
    static java.lang.String y;
    static java.lang.Class class$Test1;
    public static void main(java.lang.String[]);
    public Test1();
    static java.lang.Class class$(java.lang.String);
}

Method void main(java.lang.String[])
   0 new #11 <Class java.lang.StringBuffer>
   3 dup
   4 new #9 <Class java.lang.Object>
   7 dup
   8 invokespecial #13 <Method java.lang.Object()>
  11 invokestatic #28 <Method java.lang.String valueOf(java.lang.Object)>
  14 invokespecial #15 <Method java.lang.StringBuffer(java.lang.String)>
  17 ldc #4 <String "abc">
  19 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  22 ldc #2 <Real 4.5>
  24 invokevirtual #18 <Method java.lang.StringBuffer append(float)>
  27 ldc #1 <String "">
  29 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  32 ldc2_w #32 <Double 3.2>
  35 invokevirtual #17 <Method java.lang.StringBuffer append(double)>
  38 ldc2_w #30 <Long 56>
  41 invokevirtual #20 <Method java.lang.StringBuffer append(long)>
  44 ldc #1 <String "">
  46 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  49 bipush 22
  51 invokevirtual #19 <Method java.lang.StringBuffer append(int)>
  54 ldc #1 <String "">
  56 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  59 bipush 99
  61 invokevirtual #16 <Method java.lang.StringBuffer append(char)>
  64 ldc #1 <String "">
  66 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  69 iconst_5
  70 invokevirtual #19 <Method java.lang.StringBuffer append(int)>
  73 ldc #1 <String "">
  75 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  78 iconst_1
  79 invokevirtual #22 <Method java.lang.StringBuffer append(boolean)>
  82 ldc #1 <String "">
  84 invokevirtual #21 <Method java.lang.StringBuffer append(java.lang.String)>
  87 iconst_5
  88 invokevirtual #19 <Method java.lang.StringBuffer append(int)>
  91 invokevirtual #27 <Method java.lang.String toString()>
  94 putstatic #29 <Field java.lang.String y>
  97 getstatic #24 <Field java.lang.Class class$Test1>
 100 ifnonnull 111
 103 ldc #3 <String "Test1">
 105 invokestatic #23 <Method java.lang.Class class$(java.lang.String)>
 108 putstatic #24 <Field java.lang.Class class$Test1>
 111 return

Method Test1()
   0 aload_0
   1 invokespecial #13 <Method java.lang.Object()>
   4 return

Method java.lang.Class class$(java.lang.String)
   0 aload_0
   1 invokestatic #25 <Method java.lang.Class forName(java.lang.String)>
   4 areturn
   5 astore_1
   6 new #8 <Class java.lang.NoClassDefFoundError>
   9 dup
  10 aload_1
  11 invokevirtual #26 <Method java.lang.String getMessage()>
  14 invokespecial #14 <Method java.lang.NoClassDefFoundError(java.lang.String)>
  17 athrow
Exception table:
   from   to  target type
     0     5     5   <Class java.lang.ClassNotFoundException>
