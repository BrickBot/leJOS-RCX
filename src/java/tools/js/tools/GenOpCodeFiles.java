package js.tools;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import sun.tools.java.*;


public class GenOpCodeFiles
{
  static class OpCode
  {
    public int    args;
    public String mnemonic;

    OpCode()
    {
      args = 0;
      mnemonic = null;
    }
  }


  static OpCode gOC[];
  static final String kConstantsFile = "JOpCode.jav";
  static final String kDumpFile = "JBCO.jav";
  static final String kReadFile = "JBCI.jav";

  public static void initOpCodeArray (Field aFields[])
  throws Exception
  {
    gOC = new OpCode[256];
    for (int pIndex = 0; pIndex < aFields.length; pIndex++)
    {
      String pName = aFields[pIndex].getName();
      //System.out.println ("field: " + pName);
      if (pName.indexOf ("opc_") != -1)
      {
        int pOpCode = ((Integer) aFields[pIndex].get(null)).intValue();
        //System.out.println ("opcode: " + pOpCode);
        if (pOpCode >= 0)
        {
          gOC[pOpCode] = new OpCode();
          String mn = aFields[pIndex].getName().substring (4);
          gOC[pOpCode].mnemonic = mn;
          //System.out.println ("assigned: " + pOpCode + ":" + mn);
          switch (pOpCode)
          {
            case RuntimeConstants.opc_bipush:              
            case RuntimeConstants.opc_ldc:
            case RuntimeConstants.opc_iload:
            case RuntimeConstants.opc_lload:
            case RuntimeConstants.opc_fload:
            case RuntimeConstants.opc_dload:
            case RuntimeConstants.opc_aload:
            case RuntimeConstants.opc_istore:
            case RuntimeConstants.opc_lstore:
            case RuntimeConstants.opc_fstore:
            case RuntimeConstants.opc_dstore:
            case RuntimeConstants.opc_astore:
            case RuntimeConstants.opc_ret:
            case RuntimeConstants.opc_newarray:
            case RuntimeConstants.opc_wide:
              gOC[pOpCode].args = 1;
              break;
            case RuntimeConstants.opc_sipush:
            case RuntimeConstants.opc_ldc_w:

            case RuntimeConstants.opc_ldc2_w:
            case RuntimeConstants.opc_iinc:
            case RuntimeConstants.opc_ifeq:
            case RuntimeConstants.opc_ifne:
            case RuntimeConstants.opc_iflt:
            case RuntimeConstants.opc_ifge:
            case RuntimeConstants.opc_ifgt:
            case RuntimeConstants.opc_ifle:
            case RuntimeConstants.opc_if_icmpeq:
            case RuntimeConstants.opc_if_icmpne:
            case RuntimeConstants.opc_if_icmplt:
            case RuntimeConstants.opc_if_icmpge:
            case RuntimeConstants.opc_if_icmpgt:
            case RuntimeConstants.opc_if_icmple:
            case RuntimeConstants.opc_if_acmpeq:
            case RuntimeConstants.opc_if_acmpne:
            case RuntimeConstants.opc_goto:
            case RuntimeConstants.opc_jsr:
            case RuntimeConstants.opc_getstatic:
            case RuntimeConstants.opc_putstatic:
            case RuntimeConstants.opc_putfield:
            case RuntimeConstants.opc_getfield:
            case RuntimeConstants.opc_invokevirtual:
            case RuntimeConstants.opc_invokespecial:
            case RuntimeConstants.opc_invokestatic:
            case RuntimeConstants.opc_new:
            case RuntimeConstants.opc_anewarray:
            case RuntimeConstants.opc_checkcast:
            case RuntimeConstants.opc_instanceof:
            case RuntimeConstants.opc_ifnull:
            case RuntimeConstants.opc_ifnonnull:
            //case RuntimeConstants.opc_ret_w:
              gOC[pOpCode].args = 2;
              break;
            case RuntimeConstants.opc_multianewarray:
              gOC[pOpCode].args = 3;
              break;
            case RuntimeConstants.opc_invokeinterface:
            case RuntimeConstants.opc_goto_w:
            case RuntimeConstants.opc_jsr_w:
              gOC[pOpCode].args = 4;
              break;
            case RuntimeConstants.opc_lookupswitch:
            case RuntimeConstants.opc_tableswitch:
              gOC[pOpCode].args = -1;
              break;
            default:
              gOC[pOpCode].args = 0;
          }
        }
      }
    }
  }

  static String iTinyVMHome = System.getProperty ("tinyvm.home");
  static final String CLASSES = "SpecialClassConstants";
  static final String SIGNATURES = "SpecialSignatureConstants";

  public static void fatal (String aMsg)
  {
    System.err.println (aMsg);
    System.exit(1);
  }

  public static void generateOpCodes (File aHeaderFile, File aJavaFile)
  throws Exception
  {
    PrintWriter pHeaderOut = new PrintWriter (new FileWriter (aHeaderFile));    
    PrintWriter pJavaOut = new PrintWriter (new FileWriter (aJavaFile));

    pHeaderOut.println ("/**");
    pHeaderOut.println (" * Machine-generated file. Do not modify.");
    pHeaderOut.println (" */");
    pHeaderOut.println();
    pHeaderOut.println ("#ifndef _OPCODES_H");
    pHeaderOut.println ("#define _OPCODES_H");
    pHeaderOut.println();

    pJavaOut.println ("package js.tinyvm;");
    pJavaOut.println ("/**");
    pJavaOut.println (" * Machine-generated file. Do not modify.");
    pJavaOut.println (" */");
    pJavaOut.println();
    pJavaOut.println ("public interface OpCodeConstants {");
    try {  
      //System.out.println ("## length = " + gOC.length);
      for (int i = 0; i < gOC.length; i++)
      {
        if (gOC[i] == null)
          continue;

        String pOpCodeName = gOC[i].mnemonic.toUpperCase();
        pHeaderOut.println ("#define OP_" + pOpCodeName + "\t" + i);

        pJavaOut.println ("  int OP_" + pOpCodeName +
                          " = " + i + ";");
      }
    } finally {
      pJavaOut.println ("}");
      pJavaOut.close();

      pHeaderOut.println ("#endif _OPCODES_H");
      pHeaderOut.close();
    }
  }

  public static void generateOpCodeInfo (File aHeaderFile, File aJavaFile)
  throws Exception
  {
    PrintWriter pHeaderOut = new PrintWriter (new FileWriter (aHeaderFile));    
    PrintWriter pJavaOut = new PrintWriter (new FileWriter (aJavaFile));

    pHeaderOut.println ("/**");
    pHeaderOut.println (" * Machine-generated file. Do not modify.");
    pHeaderOut.println (" */");
    pHeaderOut.println();
    pHeaderOut.println ("#ifndef _OPCODEINFO_H");
    pHeaderOut.println ("#define _OPCODEINFO_H");
    pHeaderOut.println();

    pJavaOut.println ("package js.tinyvm;");
    pJavaOut.println ("/**");
    pJavaOut.println (" * Machine-generated file. Do not modify.");
    pJavaOut.println (" */");
    pJavaOut.println();
    pJavaOut.println ("public interface OpCodeInfo {");
    try { 
      pJavaOut.println ("  int OPCODE_ARGS[] = {"); 

      pHeaderOut.println ("char OPCODE_ARGS[] = {");

      for (int i = 0; i < gOC.length; i++)
      {
        pJavaOut.print ("    " + (gOC[i] == null ? -1 : gOC[i].args));
        pJavaOut.println (i < gOC.length - 1 ? "," : "");

        pHeaderOut.print ("  " + (gOC[i] == null ? -1 : gOC[i].args));
        pHeaderOut.println (i < gOC.length - 1 ? "," : "");
      }
      pJavaOut.println ("  };");
      pJavaOut.println();
      pJavaOut.println ("  String OPCODE_NAME[] = {"); 

      pHeaderOut.println ("};");
      pHeaderOut.println();
      pHeaderOut.println ("char *OPCODE_NAME[] = {");
      for (int i = 0; i < gOC.length; i++)
      {
        pJavaOut.print ("    \"" + (gOC[i] == null ? "" : gOC[i].mnemonic) +
                        "\"");
        pJavaOut.println (i < gOC.length - 1 ? "," : "");

        pHeaderOut.print ("  \"" + (gOC[i] == null ? "" : gOC[i].mnemonic) +
                        "\"");
        pHeaderOut.println (i < gOC.length - 1 ? "," : "");
      }
      pJavaOut.println ("  };");

      pHeaderOut.println ("};");
    } finally {
      pJavaOut.println ("}");
      pJavaOut.close();

      pHeaderOut.println ("#endif _OPCODEINFO_H");
      pHeaderOut.close();
    }
  }

  public static void main (String[] args)
  throws Exception
  {
    if (iTinyVMHome == null)
      fatal ("Error: Property tinyvm.home undefined");
    File pHome = new File (iTinyVMHome);
    if (!pHome.exists())
      fatal ("Error: " + iTinyVMHome + " does not exist.");
    if (!pHome.isDirectory())
      fatal ("Error: " + iTinyVMHome + " is not a directory.");
    String bincls = "sun.tools.java.RuntimeConstants";
    Class cls = Class.forName(bincls);
    Field flds[] = cls.getFields();
    initOpCodeArray (flds);
    File pVmSrc = new File (pHome, "vmsrc");
    File pOpCodesH = new File (pVmSrc, "opcodes.h");
    File pEmul = new File (pHome, "emul");
    File pOpInfoH = new File (pEmul, "opcodeinfo.h");
    File pTools = new File (pHome, "jtools");
    File pToolsJs = new File (pTools, "js");
    File pToolsJsTinyVM = new File (pToolsJs, "tinyvm");
    File pOpCodesJ = new File (pToolsJsTinyVM, "OpCodeConstants.java");
    File pOpInfoJ = new File (pToolsJsTinyVM, "OpCodeInfo.java");
    generateOpCodes (pOpCodesH, pOpCodesJ); 
    generateOpCodeInfo (pOpInfoH, pOpInfoJ); 
  }
}

