package js.tinyvm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.ByteWriter;
import js.tinyvm.util.HashVector;

/**
 * Abstraction for dumped binary.
 */
public class Binary
{
   /**
    * the stringwriter for signature writing.
    */
   private StringWriter stringWriter;

   /**
    * the signature writer.
    */
   private PrintWriter signatureWriter;

   // State that is written to the binary:
   final RecordTable iEntireBinary = new Sequence();

   // Contents of binary:
   final MasterRecord iMasterRecord = new MasterRecord(this);
   final EnumerableSet iClassTable = new EnumerableSet();
   final EnumerableSet iConstantTable = new EnumerableSet();
   final RecordTable iEntryClassIndices = new Sequence();
   final RecordTable iMethodTables = new Sequence();
   final RecordTable iInstanceFieldTables = new Sequence();
   final RecordTable iStaticFields = new Sequence();
   final RecordTable iExceptionTables = new EnumerableSet();
   final RecordTable iStaticState = new Sequence(true);
   final RecordTable iCodeSequences = new Sequence();
   final RecordTable iConstantValues = new Sequence();

   // Other state:
   final Hashtable iSpecialSignatures = new Hashtable();
   final Hashtable iClasses = new Hashtable();
   final HashVector iSignatures = new HashVector();

   /**
    * Default constructor, initializes some internal writer.
    */
   public Binary ()
   {
      this.stringWriter = new StringWriter();

      // TODO remove this
      // this.signatureWriter = new PrintWriter(this.stringWriter);
      this.signatureWriter = new PrintWriter(System.out);
   }

   /**
    * Dump.
    * 
    * @param writer
    * @throws TinyVMException
    */
   public void dump (ByteWriter writer) throws TinyVMException
   {
      iEntireBinary.dump(writer);
   }

   //
   // classes
   //

   /**
    * Add a class.
    * 
    * @param className class name with '/'
    * @param classRecord
    */
   protected void addClassRecord (String className, ClassRecord classRecord)
   {
      assert className != null: "Precondition: className != null";
      assert classRecord != null: "Precondition: classRecord != null";
      assert className.indexOf('.') == -1: "Precondition: className is in correct form";

      iClasses.put(className, classRecord);
      iClassTable.add(classRecord);
   }

   /**
    * Has class in binary a public static void main (String[] args) method?
    * 
    * @param className class name with '/'
    * @return
    */
   public boolean hasMain (String className)
   {
      assert className != null: "Precondition: className != null";
      assert className.indexOf('.') == -1: "Precondition: className is in correct form";

      ClassRecord pRec = getClassRecord(className);
      return pRec.hasMethod(new Signature("main", "([Ljava/lang/String;)V"),
         true);
   }

   /**
    * Get class record with given signature.
    * 
    * @param className class name with '/'
    * @return class record or null if not found
    */
   public ClassRecord getClassRecord (String className)
   {
      assert className != null: "Precondition: className != null";
      assert className.indexOf('.') == -1: "Precondition: className is in correct form";

      return (ClassRecord) iClasses.get(className);
   }

   /**
    * Get index of class in binary by its signature.
    * 
    * @param className class name with '/'
    * @return index of class in binary or -1 if not found
    */
   public int getClassIndex (String className)
   {
      assert className != null: "Precondition: className != null";
      assert className.indexOf('.') == -1: "Precondition: className is in correct form";

      return getClassIndex(getClassRecord(className));
   }

   /**
    * Get index of class in binary by its class record.
    * 
    * @param classRecord
    * @return index of class in binary or -1 if not found
    */
   public int getClassIndex (ClassRecord classRecord)
   {
      if (classRecord == null)
      {
         return -1;
      }

      return iClassTable.indexOf(classRecord);
   }

   //
   // constants
   //

   /**
    * Get constant record with given index.
    * 
    * @param index
    * @return constant record or null if not found
    */
   public ConstantRecord getConstantRecord (int index)
   {
      assert index >= 0: "Precondition: index >= 0";

      return (ConstantRecord) iConstantTable.elementAt(index);
   }

   /**
    * Get index of constant in binary by its constant record.
    * 
    * @param constantRecord
    * @return index of constant in binary or -1 if not found
    */
   public int getConstantIndex (ConstantRecord constantRecord)
   {
      if (constantRecord == null)
      {
         return -1;
      }

      return iConstantTable.indexOf(constantRecord);
   }

   //
   // processing
   //

   /**
    * Create closure.
    * 
    * @param entryClassNames names of entry class with '/'
    * @param aClassPath class path
    * @param aAll do not filter classes?
    */
   public static Binary createFromClosureOf (String[] entryClassNames,
      ClassPath aClassPath, boolean aAll) throws TinyVMException
   {
      Binary pBin = new Binary();
      // From special classes and entry class, store closure
      pBin.processClasses(entryClassNames, aClassPath);
      // Store special signatures
      pBin.processSpecialSignatures();
      pBin.processConstants();
      pBin.processMethods(aAll);
      pBin.processFields();
      // Copy code as is (first pass)
      pBin.processCode(false);
      pBin.storeComponents();
      pBin.initOffsets();
      // Post-process code after offsets are set (second pass)
      pBin.processCode(true);
      // Do -verbose reporting
      pBin.report();

      return pBin;
   }

   public void processClasses (String[] entryClassNames, ClassPath classPath)
      throws TinyVMException
   {
      assert entryClassNames != null: "Precondition: entryClassNames != null";
      assert classPath != null: "Precondition: classPath != null";

      Vector pInterfaceMethods = new Vector();

      // Add special all classes first
      String[] specialClasses = SpecialClassConstants.CLASSES;
      _logger.log(Level.INFO, "Starting with " + specialClasses.length
         + " special classes.");
      for (int i = 0; i < specialClasses.length; i++)
      {
         String className = specialClasses[i];
         ClassRecord classRecord = ClassRecord.getClassRecord(className,
            classPath, this);
         addClassRecord(className, classRecord);
         // pRec.useAllMethods();
      }

      // Now add entry classes
      _logger.log(Level.INFO, "Starting with " + entryClassNames.length
         + " entry classes.");
      for (int i = 0; i < entryClassNames.length; i++)
      {
         String className = entryClassNames[i];
         ClassRecord pRec = ClassRecord.getClassRecord(className, classPath,
            this);
         addClassRecord(className, pRec);
         pRec.useAllMethods();
         // Update table of indices to entry classes
         iEntryClassIndices.add(new EntryClassIndex(this, className));
      }

      // Now add the closure.
      _logger.log(Level.INFO, "Starting with " + iClassTable.size()
         + " classes.");
      // Yes, call iClassTable.size() in every pass of the loop.
      for (int pIndex = 0; pIndex < iClassTable.size(); pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         _logger.log(Level.INFO, "Class " + pIndex + ": " + pRec.iName);
         appendSignature("Class " + pIndex + ": " + pRec.iName);
         pRec.storeReferredClasses(iClasses, iClassTable, classPath,
            pInterfaceMethods);
      }
      // Initialize indices and flags
      int pSize = iClassTable.size();
      for (int pIndex = 0; pIndex < pSize; pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         for (int i = 0; i < pInterfaceMethods.size(); i++)
            pRec.addUsedMethod((String) pInterfaceMethods.elementAt(i));
         pRec.iIndex = pIndex;
         pRec.initFlags();
         pRec.initParent();
      }
   }

   public void processSpecialSignatures ()
   {
      for (int i = 0; i < SpecialSignatureConstants.SIGNATURES.length; i++)
      {
         Signature pSig = new Signature(SpecialSignatureConstants.SIGNATURES[i]);
         iSignatures.addElement(pSig);
         iSpecialSignatures.put(pSig, SpecialSignatureConstants.SIGNATURES[i]);
      }
   }

   public boolean isSpecialSignature (Signature aSig)
   {
      return iSpecialSignatures.containsKey(aSig);
   }

   public void processConstants () throws TinyVMException
   {
      int pSize = iClassTable.size();
      for (int pIndex = 0; pIndex < pSize; pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         pRec.storeConstants(iConstantTable, iConstantValues);
      }
   }

   /**
    * Calls storeMethods on all the classes of the closure previously computed
    * with processClasses.
    * 
    * @throws TinyVMException
    */
   public void processMethods (boolean iAll) throws TinyVMException
   {
      int pSize = iClassTable.size();
      for (int pIndex = 0; pIndex < pSize; pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         pRec.storeMethods(iMethodTables, iExceptionTables, iSignatures, iAll,
            this.signatureWriter);
      }
   }

   public void processFields () throws TinyVMException
   {
      int pSize = iClassTable.size();
      for (int pIndex = 0; pIndex < pSize; pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         pRec.storeFields(iInstanceFieldTables, iStaticFields, iStaticState);
      }
   }

   public void processCode (boolean aPostProcess) throws TinyVMException
   {
      int pSize = iClassTable.size();
      for (int pIndex = 0; pIndex < pSize; pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         pRec.storeCode(iCodeSequences, aPostProcess);
      }
   }

   //
   // storing
   //

   public void storeComponents ()
   {
      // Master record and class table are always the first two:
      iEntireBinary.add(iMasterRecord);
      iEntireBinary.add(iClassTable);
      // 5 aligned components:
      iEntireBinary.add(iStaticState);
      iEntireBinary.add(iStaticFields);
      iEntireBinary.add(iConstantTable);
      iEntireBinary.add(iMethodTables);
      iEntireBinary.add(iExceptionTables);
      // 4 unaligned components:
      iEntireBinary.add(iInstanceFieldTables);
      iEntireBinary.add(iCodeSequences);
      iEntireBinary.add(iConstantValues);
      iEntireBinary.add(iEntryClassIndices);
   }

   public void initOffsets () throws TinyVMException
   {
      iEntireBinary.initOffset(0);
   }

   public int getTotalNumMethods ()
   {
      int pTotal = 0;
      int pSize = iMethodTables.size();
      for (int i = 0; i < pSize; i++)
      {
         pTotal += ((RecordTable) iMethodTables.elementAt(i)).size();
      }
      return pTotal;
   }

   public int getTotalNumInstanceFields ()
   {
      int pTotal = 0;
      int pSize = iInstanceFieldTables.size();
      for (int i = 0; i < pSize; i++)
      {
         pTotal += ((RecordTable) iInstanceFieldTables.elementAt(i)).size();
      }
      return pTotal;
   }

   //
   // reporting
   //

   public void report () throws TinyVMException
   {
      int pSize = iSignatures.size();
      for (int i = 0; i < pSize; i++)
      {
         Signature pSig = (Signature) iSignatures.elementAt(i);
         _logger.log(Level.INFO, "Signature " + i + ": " + pSig.getImage());
         appendSignature("Signature " + i + ": " + pSig.getImage());
      }
      _logger.log(Level.INFO, "Master record : " + iMasterRecord.getLength()
         + " bytes.");
      appendSignature("Master record : " + iMasterRecord.getLength()
         + " bytes.");
      _logger.log(Level.INFO, "Class records : " + iClassTable.size() + " ("
         + iClassTable.getLength() + " bytes).");
      appendSignature("Class records : " + iClassTable.size() + " ("
         + iClassTable.getLength() + " bytes).");
      _logger.log(Level.INFO, "Field records : " + getTotalNumInstanceFields()
         + " (" + iInstanceFieldTables.getLength() + " bytes).");
      appendSignature("Field records : " + getTotalNumInstanceFields() + " ("
         + iInstanceFieldTables.getLength() + " bytes).");
      _logger.log(Level.INFO, "Method records: " + getTotalNumMethods() + " ("
         + iMethodTables.getLength() + " bytes).");
      appendSignature("Method records: " + getTotalNumMethods() + " ("
         + iMethodTables.getLength() + " bytes).");
      _logger.log(Level.INFO, "Code          : " + iCodeSequences.size() + " ("
         + iCodeSequences.getLength() + " bytes).");
      appendSignature("Code          : " + iCodeSequences.size() + " ("
         + iCodeSequences.getLength() + " bytes).");

      _logger.log(Level.INFO, "Class table offset   : "
         + iClassTable.getOffset());
      appendSignature("Class table offset   : " + iClassTable.getOffset());
      _logger.log(Level.INFO, "Constant table offset: "
         + iConstantTable.getOffset());
      appendSignature("Constant table offset: " + iConstantTable.getOffset());
      _logger.log(Level.INFO, "Method tables offset : "
         + iMethodTables.getOffset());
      appendSignature("Method tables offset : " + iMethodTables.getOffset());
      _logger.log(Level.INFO, "Excep tables offset  : "
         + iExceptionTables.getOffset());
      appendSignature("Excep tables offset  : " + iExceptionTables.getOffset());
   }

   /**
    * append a signature to internal writers.
    * 
    * @param msg the message
    */
   public void appendSignature (String msg)
   {
      this.signatureWriter.println(msg);
      // TODO remove this
      this.signatureWriter.flush();
   }

   /**
    * Flush all signature information into a file.
    * 
    * @param aFile the file to flush for
    * @throws FileNotFoundException will be raised, if file cannot be opened
    */
   public void flushSignatureFile (File aFile) throws FileNotFoundException
   {
      PrintWriter pw = new PrintWriter(new FileOutputStream(aFile));
      pw.print(this.stringWriter.toString());
      pw.flush();
      pw.close();
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

