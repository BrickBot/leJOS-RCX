package js.tinyvm2;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.ByteWriter;
import js.tinyvm.io.IOUtilities;
import js.tinyvm.util.HashVector;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 * Abstraction for a class record (see vmsrc/language.h).
 */
public class ClassRecord implements WritableData
{
   int iIndex = -1;
   String iName;
   /**
    * On-demand size of the class.
    */
   int iClassSize = -1;
   JavaClass iCF;
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

   public void useAllMethods ()
   {
      iUseAllMethods = true;
   }

   public String getName ()
   {
      return iCF.getClassName();
   }

   public int getLength ()
   {
      return IOUtilities.adjustedSize(2 + // class size
         2 + // method table offset
         2 + // instance field table offset
         1 + // number of fields
         1 + // number of methods
         1 + // parent class
         1, // flags
         2);
   }

   public void dump (ByteWriter aOut) throws TinyVMException
   {
      try
      {
         int pAllocSize = getAllocationSize();
         assert pAllocSize != 0: "Check: alloc ok";
         aOut.writeU2(pAllocSize);
         int pMethodTableOffset = iMethodTable.getOffset();
         aOut.writeU2(pMethodTableOffset);
         aOut.writeU2(iInstanceFields.getOffset());
         int pNumFields = iInstanceFields.size();
         if (pNumFields > TinyVMConstants.MAX_FIELDS)
         {
            throw new TinyVMException("Class " + iName + ": No more than "
               + TinyVMConstants.MAX_FIELDS + " fields expected");
         }
         aOut.writeU1(pNumFields);
         int pNumMethods = iMethodTable.size();
         if (pNumMethods > TinyVMConstants.MAX_METHODS)
         {
            throw new TinyVMException("Class " + iName + ": No more than "
               + TinyVMConstants.MAX_METHODS + " methods expected");
         }
         aOut.writeU1(pNumMethods);
         aOut.writeU1(iParentClassIndex);
         //aOut.writeU1 (iArrayElementType);
         aOut.writeU1(iFlags);
         IOUtilities.writePadding(aOut, 2);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public boolean isArray ()
   {
      // TBD:
      return false;
   }

   public boolean isInterface ()
   {
      return iCF.isInterface();
   }

   public boolean hasStaticInitializer ()
   {
      Method[] methods = iCF.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method pMethod = (Method) methods[i];
         // TODO use constant
         if (pMethod.getName().toString().equals("<clinit>"))
            return true;
      }
      return false;
   }

   /**
    * (Call only after record has been processed).
    */
   public boolean hasMethod (Signature aSignature, boolean aStatic)
   {
      MethodRecord pRec = (MethodRecord) iMethods.get(aSignature);
      if (pRec == null)
         return false;
      return ((pRec.getFlags() & Constants.ACC_STATIC) == 0) ^ aStatic;
   }

   public void initFlags ()
   {
      iFlags = 0;
      if (isArray())
         iFlags |= TinyVMConstants.C_ARRAY;
      if (isInterface())
         iFlags |= TinyVMConstants.C_INTERFACE;
      if (hasStaticInitializer())
         iFlags |= TinyVMConstants.C_HASCLINIT;
   }

   /**
    * @return Number of words required for object allocation.
    * @throws TinyVMException
    */
   public int getAllocationSize () throws TinyVMException
   {
      return (getClassSize() + 5) / 2;
   }

   /**
    * @return Number of bytes occupied by instance fields.
    * @throws TinyVMException
    */
   public int getClassSize () throws TinyVMException
   {
      if (iClassSize != -1)
         return iClassSize;
      iClassSize = computeClassSize();
      return iClassSize;
   }

   /**
    * @return The size of the class in 2-byte words, including any VM space.
    *         This is the exact size required for memory allocation.
    * @throws TinyVMException
    */
   public int computeClassSize () throws TinyVMException
   {
      ClassRecord pParent = getParent();
      int pSize = (pParent != null)? pParent.getClassSize() : 0;
      Enumeration pEnum = iInstanceFields.elements();
      while (pEnum.hasMoreElements())
      {
         InstanceFieldRecord pRec = (InstanceFieldRecord) pEnum.nextElement();
         pSize += pRec.getFieldSize();
      }
      return pSize;
   }

   public ClassRecord getParent ()
   {
      JavaClass pParent = iCF.getSuperClass();
      if (pParent == null)
         return null;
      ClassRecord pRec = iBinary.getClassRecord(pParent.getClassName());
      return pRec;
   }

   public void initParent () throws TinyVMException
   {
      ClassRecord pRec = getParent();
      if (pRec == null)
      {
         iParentClassIndex = 0;
         if (!iCF.getClassName().equals("java/lang/Object"))
         {
            throw new TinyVMException("Expected java.lang.Object");
         }
      }
      else
      {
         iParentClassIndex = iBinary.getClassIndex(pRec);
         if (iParentClassIndex == -1)
         {
            throw new TinyVMException("Class not found");
         }
      }
   }

   public void storeReferredClasses (Hashtable aClasses,
      RecordTable aClassRecords, ClassPath aClassPath, Vector aInterfaceMethods)
      throws TinyVMException
   {
      _logger.log(Level.INFO, "Processing CONSTANT_Class entries in " + iName);

      ConstantPool pPool = iCF.getConstantPool();
      Constant[] constants = pPool.getConstantPool();
      for (int i = 0; i < constants.length; i++)
      {
         Constant pEntry = constants[i];
         if (pEntry instanceof ConstantClass)
         {
            String pClassName = ((ConstantClass) pEntry).getBytes(pPool);
            if (pClassName.startsWith("["))
            {
               _logger.log(Level.INFO, "Skipping array: " + pClassName);
               continue;
            }
            if (aClasses.get(pClassName) == null)
            {
               ClassRecord pRec = ClassRecord.getClassRecord(pClassName,
                  aClassPath, iBinary);
               aClasses.put(pClassName, pRec);
               aClassRecords.add(pRec);
            }
         }
         else if (pEntry instanceof ConstantMethodref)
         {
            String className = ((ConstantMethodref) pEntry).getClass(pPool);
            ClassRecord pClassRec = (ClassRecord) aClasses.get(className);
            if (pClassRec == null)
            {
               pClassRec = ClassRecord.getClassRecord(className, aClassPath,
                  iBinary);
               aClasses.put(className, pClassRec);
               aClassRecords.add(pClassRec);
            }
            // TODO fix this
            //            pClassRec.addUsedMethod(((ConstantMethodref)
            // pEntry).getNameAndType()
            //               .getName()
            //               + ":"
            //               + ((ConstantMethodref) pEntry).getNameAndType().getDescriptor());
         }
         else if (pEntry instanceof ConstantInterfaceMethodref)
         {
            // TODO fix this
            //            aInterfaceMethods.addElement(((ConstantInterfaceMethodref)
            // pEntry)
            //               .getNameAndType().getName()
            //               + ":"
            //               + ((ConstantInterfaceMethodref) pEntry).getNameAndType()
            //                  .getDescriptor());
         }
         else if (pEntry instanceof ConstantNameAndType)
         {
            if (((ConstantNameAndType) pEntry).getSignature(pPool).substring(0,
               1).equals("("))
            {
               if (!((ConstantNameAndType) pEntry).getName(pPool).substring(0,
                  1).equals("<"))
               {
                  aInterfaceMethods
                     .addElement(((ConstantNameAndType) pEntry).getName(pPool)
                        + ":"
                        + ((ConstantNameAndType) pEntry).getSignature(pPool));
               }
            }
         }
      }
   }

   public void addUsedMethod (String aRef)
   {
      iUsedMethods.addElement(aRef);
   }

   public static String cpEntryId (Constant aEntry)
   {
      String pClassName = aEntry.getClass().getName();
      int pDotIdx = pClassName.lastIndexOf('.');
      return pDotIdx == -1? pClassName : pClassName.substring(pDotIdx + 1);
   }

   MethodRecord getMethodRecord (Signature aSig)
   {
      return (MethodRecord) iMethods.get(aSig);
   }

   MethodRecord getVirtualMethodRecord (Signature aSig)
   {
      MethodRecord pRec = getMethodRecord(aSig);
      if (pRec != null)
         return pRec;
      ClassRecord pParent = getParent();
      if (pParent == null)
         return null;
      return pParent.getVirtualMethodRecord(aSig);
   }

   int getMethodIndex (MethodRecord aRecord)
   {
      return iMethodTable.indexOf(aRecord);
   }

   int getApparentInstanceFieldOffset (String aName) throws TinyVMException
   {
      ClassRecord pParent = getParent();
      int pOffset = (pParent != null)? pParent.getClassSize() : 0;
      Enumeration pEnum = iInstanceFields.elements();
      while (pEnum.hasMoreElements())
      {
         InstanceFieldRecord pRec = (InstanceFieldRecord) pEnum.nextElement();
         if (pRec.getName().equals(aName))
            return pOffset;
         pOffset += pRec.getFieldSize();
      }
      return -1;
   }

   public int getInstanceFieldOffset (String aName) throws TinyVMException
   {
      return getApparentInstanceFieldOffset(aName) + 4;
   }

   /**
    * @return Offset relative to the start of the static state block.
    * @throws TinyVMException
    */
   public int getStaticFieldOffset (String aName) throws TinyVMException
   {
      StaticValue pValue = (StaticValue) iStaticValues.get(aName);
      if (pValue == null)
         return -1;
      return pValue.getOffset() - iBinary.iStaticState.getOffset();
   }

   public int getStaticFieldIndex (String aName)
   {
      StaticFieldRecord pRecord = (StaticFieldRecord) iStaticFields.get(aName);
      if (pRecord == null)
         return -1;
      // TBD: This indexOf call is slow
      return ((Sequence) iBinary.iStaticFields).indexOf(pRecord);
   }

   public void storeConstants (RecordTable aConstantTable,
      RecordTable aConstantValues) throws TinyVMException
   {
      _logger.log(Level.INFO, "Processing other constants in " + iName);

      EnumerableSet pConstantSet = (EnumerableSet) aConstantTable;
      ConstantPool pPool = iCF.getConstantPool();
      Constant[] constants = pPool.getConstantPool();
      for (int i = 0; i < constants.length; i++)
      {
         Constant pEntry = constants[i];
         if (pEntry instanceof ConstantString
            || pEntry instanceof ConstantDouble
            || pEntry instanceof ConstantFloat
            || pEntry instanceof ConstantInteger
            || pEntry instanceof ConstantLong)
         {
            ConstantRecord pRec = new ConstantRecord(pPool, pEntry);
            if (!pConstantSet.contains(pRec))
            {
               ConstantValue pValue = new ConstantValue(pPool, pEntry);
               pRec.setConstantValue(pValue);
               pConstantSet.add(pRec);
               aConstantValues.add(pValue);
            }
         }
      }
   }

   public void storeMethods (RecordTable aMethodTables,
      RecordTable aExceptionTables, HashVector aSignatures, boolean aAll,
      PrintWriter aWriter) throws TinyVMException
   {
      _logger.log(Level.INFO, "Processing methods in " + iName);

      Method[] methods = iCF.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method pMethod = methods[i];
         Signature pSignature = new Signature(pMethod.getName(), pMethod
            .getSignature());
         String meth = pMethod.getName() + ":" + pMethod.getSignature();

         if (aAll || iUseAllMethods || iUsedMethods.indexOf(meth) >= 0
            || pMethod.getName().substring(0, 1).equals("<")
            || meth.equals("run:()V"))
         {
            MethodRecord pMethodRecord = new MethodRecord(pMethod, pSignature,
               this, iBinary, aExceptionTables, aSignatures);
            iMethodTable.add(pMethodRecord);
            iMethods.put(pSignature, pMethodRecord);
         }
         else
         {
            _logger.log(Level.INFO, "Omitting " + meth + " for class " + iName);
            aWriter.println("Omitting " + meth + " for class " + iName);
         }
      }
      aMethodTables.add(iMethodTable);
   }

   public void storeFields (RecordTable aInstanceFieldTables,
      RecordTable aStaticFields, RecordTable aStaticState)
      throws TinyVMException
   {
      _logger.log(Level.INFO, "Processing methods in " + iName);

      Field[] fields = iCF.getFields();
      for (int i = 0; i < fields.length; i++)
      {
         Field pField = fields[i];
         if (pField.isStatic())
         {
            StaticValue pValue = new StaticValue(pField);
            StaticFieldRecord pRec = new StaticFieldRecord(pField, this);
            String pName = pField.getName().toString();
            assert !iStaticValues.containsKey(pName): "Check: value not static";
            iStaticValues.put(pName, pValue);
            iStaticFields.put(pName, pRec);
            aStaticState.add(pValue);
            aStaticFields.add(pRec);
         }
         else
         {
            iInstanceFields.add(new InstanceFieldRecord(pField));
         }
      }
      aInstanceFieldTables.add(iInstanceFields);
   }

   public void storeCode (RecordTable aCodeSequences, boolean aPostProcess)
      throws TinyVMException
   {
      Enumeration pMethods = iMethodTable.elements();
      while (pMethods.hasMoreElements())
      {
         MethodRecord pRec = (MethodRecord) pMethods.nextElement();
         if (aPostProcess)
            pRec.postProcessCode(aCodeSequences, iCF, iBinary);
         else
            pRec.copyCode(aCodeSequences, iCF, iBinary);
      }
   }

   public static ClassRecord getClassRecord (String aName, ClassPath aCP,
      Binary aBinary) throws TinyVMException
   {
      InputStream pIn = aCP.getInputStream(aName);
      if (pIn == null)
      {
         throw new TinyVMException("Class " + aName.replace('/', '.')
            + " (file " + aName + ".class) not found in CLASSPATH " + aCP);
      }

      ClassRecord pCR = new ClassRecord();
      try
      {
         pCR.iBinary = aBinary;
         pCR.iName = aName;
         InputStream pBufIn = new BufferedInputStream(pIn, 4096);
         // TODO aName correct?
         pCR.iCF = new ClassParser(pBufIn, aName).parse();
         pBufIn.close();
      }
      catch (Exception e)
      {
         // TODO refactor exceptions
         throw new TinyVMException(e.getMessage(), e);
      }

      return pCR;
   }

   public String toString ()
   {
      return iName;
   }

   public int hashCode ()
   {
      return iName.hashCode();
   }

   public boolean equals (Object aObj)
   {
      if (!(aObj instanceof ClassRecord))
         return false;
      ClassRecord pOther = (ClassRecord) aObj;
      return pOther.iName.equals(iName);
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

