package js.tinyvm;

import js.classfile.*;

public class CodeUtilities
implements OpCodeConstants, OpCodeInfo, Constants
{
  String iFullName;
  JClassFile iCF;
  Binary iBinary;

  public CodeUtilities (String aMethodName, JClassFile aCF, Binary aBinary)
  {
    iFullName = fullMethod (aCF, aMethodName);
    iCF = aCF;
    iBinary = aBinary;
  }

  public void exitOnBadOpCode (int aOpCode)
  {
    Utilities.fatal (
      "Unsupported " + OPCODE_NAME[aOpCode] + " in " + iFullName + ".\n" +
      "The following features/conditions are currently unsupported::\n" +
      "- Switch statements.\n" +
      "- Integer increment constant too large. (If > 255, declare it).\n" +
      "- Arithmetic or logical operations on variables of type long.\n" +
      "- Remainder operations on floats or doubles.\n" +
      "- Too many constants or locals ( > 255).\n" +
      "- Method code too long ( > 64 Kb!).\n" +
    "");                    
  }

  public static String fullMethod (JClassFile aCF, String aMethodName)
  {
    return aCF.getName().toString() + ":" + aMethodName;
  }

  public int processConstantIndex (int aPoolIndex)
  {
    IConstantPoolEntry pEntry = null; 
    try {
      pEntry = iCF.getConstantPool().getEntry (aPoolIndex);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (!(pEntry instanceof JCPE_Integer) &&
        !(pEntry instanceof JCPE_Float) &&
        !(pEntry instanceof JCPE_String) &&
        !(pEntry instanceof JCPE_Double) &&
        !(pEntry instanceof JCPE_Long))
    {
      Utilities.fatal ("Classfile error: LDC-type instruction " +
                       "does not refer to a suitable constant. ");
    }
    ConstantRecord pRecord = new ConstantRecord ((JConstantPoolEntry) pEntry);
    int pIdx = iBinary.getConstantIndex (pRecord);
    if (pIdx == -1)

    {
      Utilities.fatal ("Bug CU-2: Didn't find constant " + pEntry + 
                       " of class " + pEntry.getClass().getName());
    }
    return pIdx;                           
  }

  public int processClassIndex (int aPoolIndex)
  {
    IConstantPoolEntry pEntry = null; 
    try {
      pEntry = iCF.getConstantPool().getEntry (aPoolIndex);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (!(pEntry instanceof JCPE_Class))
    {
      Utilities.fatal ("Classfile error: Instruction requiring " +
                       "CONSTANT_Class entry got " +
                       (pEntry == null ? "null" : 
                       pEntry.getClass().getName()));
    }
    JCPE_Class pClassEntry = (JCPE_Class) pEntry;
    int pIdx = iBinary.getClassIndex (pClassEntry.getName());
    if (pIdx == -1)
    {
      Utilities.fatal ("Bug CU-3: Didn't find class " + pEntry + 
                       " from class " + iCF.getThisClass().getName());
    }
    return pIdx;                           
  }

  public int processMultiArray (int aPoolIndex)
  {
    IConstantPoolEntry pEntry = null; 
    try {
      pEntry = iCF.getConstantPool().getEntry (aPoolIndex);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (!(pEntry instanceof JCPE_Class))
    {
      Utilities.fatal ("Classfile error: Instruction requiring " +
                       "CONSTANT_Class entry got " +
                       (pEntry == null ? "null" : 
                       pEntry.getClass().getName()));
    }
    JCPE_Class pClassEntry = (JCPE_Class) pEntry;
    int[] pTypeDim = JClassName.getTypeAndDimensions (pClassEntry.getName());
    Utilities.assert (pTypeDim[0] <= 0xFF);
    Utilities.assert (pTypeDim[1] <= 0xFF);
    Utilities.assert (pTypeDim[1] > 0);
    return pTypeDim[0] << 8 | pTypeDim[1];
  }

  /**
   * @return The word that should be written as parameter
   *         of putstatic, getstatic, getfield, or putfield.
   */
  int processField (int aFieldIndex, boolean aStatic)
  {
    IConstantPoolEntry pEntry = null; 
    try {
      pEntry = iCF.getConstantPool().getEntry (aFieldIndex);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (!(pEntry instanceof JCPE_Fieldref))
    {
      Utilities.fatal ("Classfile error: Instruction requiring " +
                       "CONSTANT_Fieldref entry got " +
                       (pEntry == null ? "null" : 
                       pEntry.getClass().getName()));
    }
    JCPE_Fieldref pFieldEntry = (JCPE_Fieldref) pEntry;
    JCPE_Class pClass = pFieldEntry.getClassEntry();
    ClassRecord pClassRecord = iBinary.getClassRecord (pClass.getName());
    if (pClassRecord == null)
    {
      Utilities.fatal ("Bug CU-3: Didn't find class " + pClass + 
                       " from class " + iCF.getThisClass().getName());
    }
    String pName = pFieldEntry.getNameAndType().getName();
    int pOffset = aStatic ? pClassRecord.getStaticFieldOffset (pName) :
                            pClassRecord.getInstanceFieldOffset (pName);
    if (pOffset == -1)
    {
      Utilities.fatal ("Error: Didn't find field " + pClass + ":" + pName +
                       " from class " + iCF.getThisClass().getName());
    }
    Utilities.assert (pOffset <= MAX_FIELD_OFFSET);
    int pFieldType = InstanceFieldRecord.descriptorToType (
                     pFieldEntry.getNameAndType().getDescriptor());
    int pFieldSize = InstanceFieldRecord.getTypeSize (pFieldType);
    return ((pFieldSize - 1) << F_SIZE_SHIFT) | pOffset;
  }

  /**
   * @return The word that should be written as parameter
   *         of an invocation opcode.
   */
  int processMethod (int aMethodIndex, boolean aSpecial)
  {
    IConstantPoolEntry pEntry = null; 
    try {
      pEntry = iCF.getConstantPool().getEntry (aMethodIndex);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (!(pEntry instanceof JCPE_Methodref))
    {
      Utilities.fatal ("Classfile error: Instruction requiring " +
                       "CONSTANT_Methodref entry got " +
                       (pEntry == null ? "null" : 
                       pEntry.getClass().getName()));
    }
    JCPE_Methodref pMethodEntry = (JCPE_Methodref) pEntry;
    JCPE_Class pClass = pMethodEntry.getClassEntry();
    ClassRecord pClassRecord = iBinary.getClassRecord (pClass.getName());
    if (pClassRecord == null)
    {
      Utilities.fatal ("Bug CU-4: Didn't find class " + pClass + 
                       " from class " + iCF.getThisClass().getName());
    }
    JCPE_NameAndType pNT = pMethodEntry.getNameAndType();
    Signature pSig = new Signature (pNT.getName(), pNT.getDescriptor());
    MethodRecord pMethod = pClassRecord.getMethodRecord (pSig);
    if (aSpecial)
    {
      int pClassIndex = iBinary.getClassIndex (pClassRecord);
      Utilities.assert (pClassIndex != -1 && pClassIndex < MAX_CLASSES);
      int pMethodIndex = pClassRecord.getMethodIndex (pMethod);
      Utilities.assert (pMethodIndex != -1 && pMethodIndex < MAX_METHODS);
      return (pClassIndex << 8) | (pMethodIndex & 0xFF);
    }
    else
    {
      int pNumParams = pMethod.getNumParameterWords() - 1;
      Utilities.assert (pNumParams < MAX_PARAMETER_WORDS);
      int pSignature = pMethod.getSignatureId();
      Utilities.assert (pSignature < MAX_SIGNATURES);
      return (pNumParams << M_ARGS_SHIFT) | pSignature;
    }
  }

  public byte[] processCode (byte[] aCode)
  {
    byte[] pOutCode = new byte[aCode.length];
    int i = 0;
    while (i < aCode.length)
    {
      pOutCode[i] =  aCode[i];    
      int pOpCode = pOutCode[i] & 0xFF;    
      i++;
      switch (pOpCode)
      {
        case OP_LDC:
          pOutCode[i] = (byte) processConstantIndex ((int) aCode[i]);
          i++;
          break;
        case OP_LDC2_W:
          int pIdx1 = processConstantIndex ((aCode[i] & 0xFF) << 8 | 
                                           aCode[i+1]);
          pOutCode[i++] = (byte) (pIdx1 >> 8);
          pOutCode[i++] = (byte) (pIdx1 | 0xFF);
          break;
        case OP_ANEWARRAY:
          // Opcode is changed: ANEWARRAY -> NEWARRAY
          pOutCode[i-1] = (byte) OP_NEWARRAY;
          pOutCode[i++] = (byte) T_REFERENCE;
          pOutCode[i++] = (byte) OP_NOP;
          break;
        case OP_MULTIANEWARRAY:
          int pIdx2 = processMultiArray ((aCode[i] & 0xFF) << 8 | 
                                        aCode[i+1]);
          // Write element type
          pOutCode[i++] = (byte) (pIdx2 >> 8);
          // Write total number of dimensions
          pOutCode[i++] = (byte) (pIdx2 | 0xFF);
          // Skip requested dimensions for allocation
          i++;
          break;           
        case OP_NEW:
        case OP_CHECKCAST:
        case OP_INSTANCEOF:
          int pIdx3 = processClassIndex ((aCode[i] & 0xFF) << 8 | 
                                        aCode[i+1]);
          Utilities.assert (pIdx3 < MAX_CLASSES);
          pOutCode[i++] = (byte) (pIdx3 >> 8);
          pOutCode[i++] = (byte) (pIdx3 | 0xFF);
          break;         
        case OP_PUTSTATIC:
        case OP_GETSTATIC:
          int pWord1 = processField ((aCode[i] & 0xFF) << 8 | 
                                     aCode[i+1], true);
          pOutCode[i++] = (byte) (pWord1 >> 8);
          pOutCode[i++] = (byte) (pWord1 | 0xFF);
          break;
        case OP_PUTFIELD:
        case OP_GETFIELD:
          int pWord2 = processField ((aCode[i] & 0xFF) << 8 | 
                                     aCode[i+1], false);
          pOutCode[i++] = (byte) (pWord2 >> 8);
          pOutCode[i++] = (byte) (pWord2 | 0xFF);
          break;
        case OP_INVOKEINTERFACE:
          // Opcode is changed:
          pOutCode[i-1]  = (byte) OP_INVOKEVIRTUAL;
          int pWord3 = processMethod ((aCode[i] & 0xFF) << 8 | 
                                     aCode[i+1], false);
          pOutCode[i++] = (byte) (pWord3 >> 8);
          pOutCode[i++] = (byte) (pWord3 | 0xFF);
          pOutCode[i++] = (byte) OP_NOP;
          break;
        case OP_INVOKESPECIAL:
        case OP_INVOKESTATIC:
          // Opcode is changed:
          int pWord4 = processMethod ((aCode[i] & 0xFF) << 8 | 
                                     aCode[i+1], true);
          pOutCode[i++] = (byte) (pWord4 >> 8);
          pOutCode[i++] = (byte) (pWord4 | 0xFF);
          break;
        case OP_INVOKEVIRTUAL:
          // Opcode is changed:
          int pWord5 = processMethod ((aCode[i] & 0xFF) << 8 | 
                                     aCode[i+1], false);
          pOutCode[i++] = (byte) (pWord5 >> 8);
          pOutCode[i++] = (byte) (pWord5 | 0xFF);
          break;
        case OP_LOOKUPSWITCH:
        case OP_TABLESWITCH:
        case OP_WIDE:
        case OP_GOTO_W:
        case OP_JSR_W:
        case OP_LDC_W:
        case OP_I2L:
        case OP_F2L:
        case OP_D2L:
        case OP_L2I:
        case OP_L2F:
        case OP_L2D:
        case OP_LADD:
        case OP_LSUB:
        case OP_LMUL:
        case OP_LDIV:
        case OP_LREM:
        case OP_LNEG:
        case OP_LCMP:
        case OP_FREM:
        case OP_DREM:
        case OP_LSHL:
        case OP_LSHR:
        case OP_LUSHR:
        case OP_LAND:
        case OP_LOR:
        case OP_LXOR:
          exitOnBadOpCode (pOpCode);
          break;
        case OP_BREAKPOINT:
          Utilities.fatal ("Invalid opcode detected: " + pOpCode + " " +
                           OPCODE_NAME[pOpCode]);
          break;
        default:
          int pArgs = OPCODE_ARGS[pOpCode];
          if (pArgs == -1)
	  {
            Utilities.fatal ("Bug CU-1: Got " + pOpCode + " in " +
                             iFullName + ".");
	  }
          for (int ctr = 1; ctr <= pArgs; ctr++)
            pOutCode[i+ctr] = aCode[i+ctr];
          i += pArgs;
          break;
      }
    }
    return pOutCode;
  }

  // Notes:
  // - NEWARRAY remains unchanged
  // 
}
