package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

/**
 * Abstraction for a class record (see vmsrc/language.h).
 */
public class ClassRecord implements WritableData, Constants
{
  int iIndex = -1;
  String iName;
  /**
   * On-demand size of the class.
   */
  int iClassSize = -1;
  JClassFile iCF;
  Binary iBinary;
  final EnumerableSet iMethodTable = new EnumerableSet();
  final RecordTable iInstanceFields = new Sequence();
  final Hashtable iStaticValues = new Hashtable();
  final Hashtable iStaticFields = new Hashtable();
  final Hashtable iMethods = new Hashtable();
  final Vector iUsedMethods = new Vector();
  int iParentClassIndex;
  int iArrayElementType;
  int iFlags;
  boolean iUseAllMethods = false;

  public void useAllMethods() {
    iUseAllMethods = true;
  }

  public String getName()
  {
    return iCF.getName();
  }

  public int getLength()
  {
    return IOUtilities.adjustedSize (
				       2 + // class size
                                       2 + // method table offset
                                       2 + // instance field table offset
                                       1 + // number of fields
				       1 + // number of methods
				       1 + // parent class
				       1,  // flags
           2);
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    int pAllocSize = getAllocationSize();
    Assertion.test (pAllocSize != 0);
    aOut.writeU2 (pAllocSize);
    int pMethodTableOffset = iMethodTable.getOffset();
    aOut.writeU2 (pMethodTableOffset);
    aOut.writeU2 (iInstanceFields.getOffset());
    int pNumFields = iInstanceFields.size();
    if (pNumFields > MAX_FIELDS)
    {
      Assertion.fatal ("Class " + iName + ": No more than " + MAX_FIELDS + 
        " fields expected.");
    }
    aOut.writeU1 (pNumFields);
    int pNumMethods = iMethodTable.size();
    if (pNumMethods > MAX_METHODS)
    {
      Assertion.fatal ("Class " + iName + ": No more than " + MAX_METHODS + 
        " methods expected.");
    }
    aOut.writeU1 (pNumMethods);
    aOut.writeU1 (iParentClassIndex);
    //aOut.writeU1 (iArrayElementType);
    aOut.writeU1 (iFlags);
    IOUtilities.writePadding (aOut, 2);
  }

  public boolean isArray()
  {
    // TBD:
    return false;
  }

  public boolean isInterface()
  {
    return iCF.isInterface();
  }

  public boolean hasStaticInitializer()
  {
    Enumeration pEnum = iCF.getMethods().elements();
    while (pEnum.hasMoreElements())
    {
      JMethod pMethod = (JMethod) pEnum.nextElement();
      if (pMethod.getName().toString().equals ("<clinit>"))
        return true;
    }
    return false;
  }

  /**
   * (Call only after record has been processed).
   */
  public boolean hasMethod (Signature aSignature, boolean aStatic)
  {
    MethodRecord pRec = (MethodRecord) iMethods.get (aSignature);
    if (pRec == null)
      return false;
    return ((pRec.getFlags() & M_STATIC) == 0) ^ aStatic;
  }

  public void initFlags()
  {
    iFlags = 0;
    if (isArray())
      iFlags |= C_ARRAY;
    if (isInterface())
      iFlags |= C_INTERFACE;
    if (hasStaticInitializer())
      iFlags |= C_HASCLINIT;
  }

  /**
   * @return Number of words required for object allocation.
   */
  public int getAllocationSize()
  {
    return (getClassSize() + 5) / 2;
  }

  /**
   * @return Number of bytes occupied by instance fields.
   */
  public int getClassSize()
  {
    if (iClassSize != -1)
      return iClassSize;
    iClassSize = computeClassSize();
    return iClassSize;
  }
      
  /**
   * @return The size of the class in 2-byte words, including
   *         any VM space. This is the exact size required for
   *         memory allocation.
   */
  public int computeClassSize()
  {
    ClassRecord pParent = getParent();
    int pSize = (pParent != null) ? pParent.getClassSize() : 0;
    Enumeration pEnum = iInstanceFields.elements();
    while (pEnum.hasMoreElements())
    {
      InstanceFieldRecord pRec = (InstanceFieldRecord) pEnum.nextElement();
      pSize += pRec.getFieldSize();
    }
    return pSize;
  }

  public ClassRecord getParent()
  {
    JCPE_Class pParent = iCF.getSuperClass();
    if (pParent == null)
      return null;
    ClassRecord pRec = iBinary.getClassRecord (pParent.getName().toString());
    return pRec;
  }

  public void initParent()
  {
    ClassRecord pRec = getParent();
    if (pRec == null)
    {
      Assertion.test (iCF.getName().equals ("java/lang/Object"));
      iParentClassIndex = 0;
    }
    else
    {
      iParentClassIndex = iBinary.getClassIndex (pRec);
      Assertion.test (iParentClassIndex != -1);
    }
  }

  public void storeReferredClasses (Hashtable aClasses, RecordTable aClassRecords, ClassPath aClassPath, Vector aInterfaceMethods)
  throws Exception
  {
    Assertion.trace ("Processing CONSTANT_Class entries in " + iName);
    JConstantPool pPool = iCF.getConstantPool();
    Enumeration pEntries = pPool.elements();
    while (pEntries.hasMoreElements())
    {
      JConstantPoolEntry pEntry = (JConstantPoolEntry) pEntries.nextElement();
      //Utilities.trace ("  " + pEntry.getClass().getName() + ": " + pEntry);
      if (pEntry instanceof JCPE_Class)
      {
        String pClassName = ((JCPE_Class) pEntry).getName();
        if (pClassName.startsWith ("["))
	{
          Assertion.trace ("Skipping array: " + pClassName);
          continue;
	}
        if (aClasses.get (pClassName) == null)
	{
          ClassRecord pRec = ClassRecord.getClassRecord (pClassName, 
                             aClassPath, iBinary);
          aClasses.put (pClassName, pRec);
          aClassRecords.add (pRec);
	}
      } else if (pEntry instanceof JCPE_Methodref) {
        // System.out.println(iName + " calls " + pEntry); 
        JCPE_Class pClass = ((JCPE_Methodref) pEntry).getClassEntry();
        ClassRecord pClassRec = (ClassRecord) aClasses.get(pClass.getName());
        if (pClassRec == null) {
          pClassRec = ClassRecord.getClassRecord (pClass.getName(), 
                             aClassPath, iBinary);
          aClasses.put (pClass.getName(), pClassRec);
          aClassRecords.add (pClassRec);
        }
        pClassRec.addUsedMethod(((JCPE_Methodref) pEntry).getNameAndType().getName()+":"+((JCPE_Methodref) pEntry).getNameAndType().getDescriptor());
      } else if (pEntry instanceof JCPE_InterfaceMethodref) {
        // System.out.println(iName + " calls interface method " + pEntry);
        aInterfaceMethods.add (((JCPE_InterfaceMethodref) pEntry).getNameAndType().getName()+":"+((JCPE_InterfaceMethodref) pEntry).getNameAndType().getDescriptor());
      } else if (pEntry instanceof JCPE_NameAndType) {
        if (((JCPE_NameAndType) pEntry).getDescriptor().substring(0,1).equals("(")) {
          if (!((JCPE_NameAndType) pEntry).getName().substring(0,1).equals("<")) {
            // System.out.println("Method by variable: " + ((JCPE_NameAndType) pEntry).getName()+":"+((JCPE_NameAndType) pEntry).getDescriptor());
            aInterfaceMethods.add (((JCPE_NameAndType) pEntry).getName()+":"+((JCPE_NameAndType) pEntry).getDescriptor());
          }  
        }
      }
    }
  }

  public void addUsedMethod(String aRef) {
    iUsedMethods.add(aRef);
  }

  public static String cpEntryId (JConstantPoolEntry aEntry)
  {
    String pClassName = aEntry.getClass().getName();
    int pDotIdx = pClassName.lastIndexOf ('.');
    return pDotIdx == -1 ? pClassName : pClassName.substring (pDotIdx + 1);   
  }

  MethodRecord getMethodRecord (Signature aSig)
  {
    return (MethodRecord) iMethods.get (aSig);
  }

  MethodRecord getVirtualMethodRecord (Signature aSig)
  {
    MethodRecord pRec = getMethodRecord (aSig);
    if (pRec != null)
      return pRec;
    ClassRecord pParent = getParent();
    if (pParent == null)
      return null;
    return pParent.getVirtualMethodRecord (aSig);
  }

  int getMethodIndex (MethodRecord aRecord)
  {
    return iMethodTable.indexOf (aRecord);
  }

  int getApparentInstanceFieldOffset (String aName)
  {
    ClassRecord pParent = getParent();
    int pOffset = (pParent != null) ? pParent.getClassSize() : 0;
    Enumeration pEnum = iInstanceFields.elements();
    while (pEnum.hasMoreElements())
    {
      InstanceFieldRecord pRec = (InstanceFieldRecord) pEnum.nextElement();
      if (pRec.getName().equals (aName))
        return pOffset;
      pOffset += pRec.getFieldSize();
    }
    return -1;
  }

  public int getInstanceFieldOffset (String aName)
  {
    return getApparentInstanceFieldOffset (aName) + 4;
  }

  /**
   * @return Offset relative to the start of the
   *         static state block.
   */
  public int getStaticFieldOffset (String aName)
  {
    StaticValue pValue = (StaticValue) iStaticValues.get (aName);
    if (pValue == null)
      return -1;
    return pValue.getOffset() - iBinary.iStaticState.getOffset();
  }

  public int getStaticFieldIndex (String aName)
  {
    StaticFieldRecord pRecord = (StaticFieldRecord) iStaticFields.get (aName);
    if (pRecord == null)
      return -1;
    // TBD: This indexOf call is slow
    return ((Sequence) iBinary.iStaticFields).indexOf (pRecord);
  }

  public void storeConstants (RecordTable aConstantTable, RecordTable aConstantValues)
  {
    Assertion.trace ("Processing other constants in " + iName);
    EnumerableSet pConstantSet = (EnumerableSet) aConstantTable;
    JConstantPool pPool = iCF.getConstantPool();
    Enumeration pEntries = pPool.elements();
    while (pEntries.hasMoreElements())
    {
      JConstantPoolEntry pEntry = (JConstantPoolEntry) pEntries.nextElement();
      if (pEntry instanceof JCPE_String ||
          pEntry instanceof JCPE_Double ||
          pEntry instanceof JCPE_Float ||
          pEntry instanceof JCPE_Integer ||
          pEntry instanceof JCPE_Long)
      {
//         System.out.println ("$@ " + iName + " JCPE_String: " + pEntry);

        ConstantRecord pRec = new ConstantRecord (pEntry);
        if (!pConstantSet.contains (pRec))
	{
//           System.out.println ("$@ " + System.identityHashCode(pRec) + " Inserted value");
          ConstantValue pValue = new ConstantValue (pEntry);
          pRec.setConstantValue (pValue);
          pConstantSet.add (pRec);
          aConstantValues.add (pValue);
	}
      }
    }
  }

  public void storeMethods (RecordTable aMethodTables,
                            RecordTable aExceptionTables, 
                            HashVector aSignatures,
                            boolean aAll)
  {
    Assertion.trace ("Processing methods in " + iName);
    Enumeration pEntries = iCF.getMethods().elements();
    while (pEntries.hasMoreElements())
    {
      JMethod pMethod = (JMethod) pEntries.nextElement();
      Signature pSignature = new Signature (pMethod.getName(), 
                                         pMethod.getDescriptor());
      String meth = pMethod.getName() + ":" +pMethod.getDescriptor();

      if (aAll || iUseAllMethods || iUsedMethods.indexOf(meth) >= 0 || 
          pMethod.getName().substring(0,1).equals("<") || meth.equals("run:()V")) {
        //System.out.println("Adding Method " + meth + " for class " + iName + " length " + iName.length() + " used " + iUsedMethods.indexOf(meth)); 
        MethodRecord pMethodRecord = new MethodRecord (pMethod, pSignature, 
          this, iBinary, aExceptionTables, aSignatures);
        iMethodTable.add (pMethodRecord);
        iMethods.put (pSignature, pMethodRecord);
      } else Assertion.verbose(1, "Omitting " + meth + " for class " + iName);
    }
    aMethodTables.add (iMethodTable);
  }


  public void storeFields (RecordTable aInstanceFieldTables,
                           RecordTable aStaticFields,
                           RecordTable aStaticState)
  {
    Assertion.trace ("Processing methods in " + iName);
    Enumeration pEntries = iCF.getFields().elements();
    while (pEntries.hasMoreElements())
    {
      JField pField = (JField) pEntries.nextElement();
      if (pField.isStatic())
      {
        StaticValue pValue = new StaticValue (pField);
        StaticFieldRecord pRec = new StaticFieldRecord (pField, this);
        String pName = pField.getName().toString();
        Assertion.test (!iStaticValues.containsKey (pName));
        iStaticValues.put (pName, pValue);
        iStaticFields.put (pName, pRec);
        aStaticState.add (pValue);
        aStaticFields.add (pRec);
      }
      else
      {
        iInstanceFields.add (new InstanceFieldRecord (
          pField));
      }
    }
    aInstanceFieldTables.add (iInstanceFields);
  }

  public void storeCode (RecordTable aCodeSequences, boolean aPostProcess)
  {
    Enumeration pMethods = iMethodTable.elements();
    while (pMethods.hasMoreElements())
    {
      MethodRecord pRec = (MethodRecord) pMethods.nextElement();
      if (aPostProcess)
        pRec.postProcessCode (aCodeSequences, iCF, iBinary);
      else
        pRec.copyCode (aCodeSequences, iCF, iBinary);
    }
  }


  public static ClassRecord getClassRecord (String aName, ClassPath aCP,
                                            Binary aBinary)
  throws Exception
  {
    InputStream pIn = aCP.getInputStream (aName);
    if (pIn == null)
    {
      Assertion.fatal ("Class " + aName.replace ('/', '.') + 
        " (file " + aName + 
        ".class) not found in CLASSPATH: " + aCP);
    }
    ClassRecord pCR = new ClassRecord();
    pCR.iBinary = aBinary;
    pCR.iCF = new JClassFile();
    pCR.iName = aName;
    InputStream pBufIn = new BufferedInputStream (pIn, 4096);
    try {
      pCR.iCF.read (pBufIn);
    } catch (Throwable t) {
      System.err.println ("Exception reading " + aName);
      t.printStackTrace();
      System.exit(1);
    }
    pBufIn.close();
    return pCR;
  }
  
  public String toString()
  {
    return iName;
  }

  public int hashCode()
  {
    return iName.hashCode();
  }

  public boolean equals (Object aObj)
  {
    if (!(aObj instanceof ClassRecord))
      return false;
    ClassRecord pOther = (ClassRecord) aObj;
    return pOther.iName.equals (iName);
  }
}









