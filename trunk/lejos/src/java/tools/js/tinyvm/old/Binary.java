package js.tinyvm.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.util.HashVector;

/**
 * Abstraction for dumped binary.
 */
public class Binary implements SpecialClassConstants, SpecialSignatureConstants
{
   /** the writer for signature writing. */
   private StringWriter stringWriter;

   /** the signature writer. */
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

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      iEntireBinary.dump(aOut);
   }

   public boolean hasMain (String aClassName)
   {
      ClassRecord pRec = getClassRecord(aClassName);
      return pRec.hasMethod(new Signature("main", "([Ljava/lang/String;)V"),
         true);
   }

   public ClassRecord getClassRecord (String aClassName)
   {
      return (ClassRecord) iClasses.get(aClassName);
   }

   public int getClassIndex (String aClassName)
   {
      return getClassIndex(getClassRecord(aClassName));
   }

   public int getClassIndex (ClassRecord aRec)
   {
      if (aRec == null)
         return -1;
      return iClassTable.indexOf(aRec);
   }

   public int getConstantIndex (ConstantRecord aRec)
   {
      if (aRec == null)
         return -1;
      return iConstantTable.indexOf(aRec);
   }

   public ConstantRecord getConstantRecord (int aIndex)
   {
      return (ConstantRecord) iConstantTable.elementAt(aIndex);
   }

   public void processClasses (Vector aEntryClasses, ClassPath aClassPath)
      throws TinyVMException
   {
      Vector pInterfaceMethods = new Vector();
      // Add special classes first
      for (int i = 0; i < CLASSES.length; i++)
      {
         String pName = CLASSES[i];
         ClassRecord pRec = ClassRecord.getClassRecord(pName, aClassPath, this);
         iClasses.put(pName, pRec);
         iClassTable.add(pRec);
         // pRec.useAllMethods();
      }
      // Now add entry classes
      int pEntrySize = aEntryClasses.size();
      for (int i = 0; i < pEntrySize; i++)
      {
         String pName = (String) aEntryClasses.elementAt(i);
         ClassRecord pRec = ClassRecord.getClassRecord(pName, aClassPath, this);
         iClasses.put(pName, pRec);
         iClassTable.add(pRec);
         pRec.useAllMethods();
         // Update table of indices to entry classes
         iEntryClassIndices.add(new EntryClassIndex(this, pName));
      }
      _logger.log(Level.INFO, "Starting with " + iClassTable.size()
         + " classes.");
      // Now add the closure.
      // Yes, call iClassTable.size() in every pass of the loop.
      for (int pIndex = 0; pIndex < iClassTable.size(); pIndex++)
      {
         ClassRecord pRec = (ClassRecord) iClassTable.elementAt(pIndex);
         _logger.log(Level.INFO, "Class " + pIndex + ": " + pRec.iName);
         appendSignature("Class " + pIndex + ": " + pRec.iName);
         pRec.storeReferredClasses(iClasses, iClassTable, aClassPath,
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
      for (int i = 0; i < SIGNATURES.length; i++)
      {
         Signature pSig = new Signature(SIGNATURES[i]);
         iSignatures.addElement(pSig);
         iSpecialSignatures.put(pSig, SIGNATURES[i]);
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

   public static Binary createFromClosureOf (Vector aEntryClasses,
      ClassPath aClassPath, boolean aAll) throws TinyVMException
   {
      Binary pBin = new Binary();
      // From special classes and entry class, store closure
      pBin.processClasses(aEntryClasses, aClassPath);
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

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

