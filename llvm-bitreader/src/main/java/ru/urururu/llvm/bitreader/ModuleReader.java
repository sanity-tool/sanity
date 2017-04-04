package ru.urururu.llvm.bitreader;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.mutable.MutableObject;
import ru.urururu.llvm.bitreader.codes.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ModuleReader {
    private Map<Integer, SortedMap<Integer, Abbrev>> abbrevsForOtherBlocks = new HashMap<>();

    private Boolean useRelativeIds;

    private List<Type> types = new ArrayList<>();

    private List<GlobalVariable> globalVariables = new ArrayList<>();
    private Map<Integer, List<GlobalVariable>> initializersForGlobals = new HashMap<>();

    private List<Function> functionsWithBodies = new ArrayList<>();
    private int bodies = 0;

    private Map<String, Object> namedMetadata = new HashMap<>();

    static {
        // ensure init
        ModuleCodes.values();
        TypeCodes.values();
        ConstantsCodes.values();
        ValueSymtabCodes.values();
        MetadataCodes.values();
    }

    private Module readModule(ByteBuffer body) {
        body.getShort();
        body.getShort();

        BitstreamReader reader = new BitstreamReader(body);

        List<Value> globalValues = new ArrayList<>();
        List<Object> metadata = new ArrayList<>();

        Context ctx = new Context(Collections.emptySortedMap(), 2) {
            @Override
            List<Value> getValues() {
                return globalValues;
            }

            @Override
            List<Object> getMetadata() {
                return metadata;
            }

            @Override
            public String toString() {
                return "<global context>";
            }

            @Override
            public Context getParent() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void finish() {
                throw new UnsupportedOperationException();
            }
        };

        readBlock(reader, ctx, body.limit());

        System.out.println("globalValues = " + globalValues);

        for (int i = 0; i < globalValues.size(); i++) {
            System.out.println("globalValues.get(" + i + ") = " + globalValues.get(i));
        }

        MapUtils.debugPrint(System.out, "namedMetadata", namedMetadata);

        System.out.println("metadata = " + metadata);
        for (int i = 0; i < metadata.size(); i++) {
            System.out.println("metadata.get(" + i + ") = " + metadata.get(i));
        }

        for (int i = 0; i < types.size(); i++) {
            System.out.println("types.get(" + i + ") = " + types.get(i));
        }


        return new Module(functionsWithBodies, globalVariables, namedMetadata);
    }

    private void readBlock(BitstreamReader reader, final Context ctx, int exitPos) {
        try {
            while (reader.hasMore()) {
                int code = reader.read(ctx.abbrevLen);
                //System.out.println("code = " + code);

                if (code == 0) {
                    // END_BLOCK
                    reader.skipToFourByteBoundary();
                    ctx.finish();
                    return;
                } else if (code == 1) {
                    // ENTER_SUBBLOCK
                    int blockId = reader.readVBR(8);
                    log("blockId = " + Utils.toBlockId(blockId));
                    int abbrevLen = reader.readVBR(4);
                    log("abbrevLen = " + abbrevLen);
                    reader.skipToFourByteBoundary();
                    int blockLen = reader.read(32);
                    log("blockLen = " + blockLen);

                    if (skipBlock(blockId)) {
                        log("SKIPPING");
                        for (int i = 0; i < blockLen; i++) {
                            reader.read(32);
                        }
                    } else {
                        log("PUSHING");
                        INDENT += 3;
                        readBlock(reader, getContext(ctx, blockId, abbrevLen), reader.position() + blockLen * 4);
                    }
                } else if (code == 2) {
                    log("DEFINE_ABBREV");
                    // DEFINE_ABBREV

                    Abbrev.AbbrevBuilder abbrev = Abbrev.builder();

                    try {
                        int numabbrevops = reader.readVBR(5);

                        for (int i = 0; i < numabbrevops; i++) {
                            int literalFlag = reader.read(1);

                            if (literalFlag == 0) {
                                int encoding = reader.read(3);

                                switch (encoding) {
                                    case 1:
                                        int fixedWidth = reader.readVBR(5);
                                        abbrev.onFixed(fixedWidth);
                                        break;
                                    case 2:
                                        int vbrWidth = reader.readVBR(5);
                                        abbrev.onVbr(vbrWidth);
                                        break;
                                    case 3:
                                        abbrev.onArray();
                                        break;
                                    case 4:
                                        abbrev.onChar6();
                                        break;
                                    case 5:
                                        abbrev.onBlob();
                                        break;
                                    default:
                                        throw new IllegalStateException("encoding: " + encoding);
                                }
                            } else {
                                int litvalue = reader.readVBR(8);
                                abbrev.onLiteral(litvalue);
                            }
                        }

                        ctx.define(abbrev.build());
                    } catch (Throwable e) {
                        throw new IllegalStateException("Can't process abbrev definition, builder: " + abbrev, e);
                    }
                } else if (code == 3) {
                    // UNABBREV_RECORD
                    List<Object> fields = new ArrayList<>();

                    int recordCode = reader.readVBR(6);

                    //fields.add(recordCode);

                    int numops = reader.readVBR(6);
                    for (int i = 0; i < numops; i++) {
                        fields.add(reader.readVBR(6));
                    }

                    ctx.onUnabbrevRecord(recordCode, fields);
                } else {
                    Abbrev abbrev = ctx.abbrevs.get(code);
                    if (abbrev != null) {
                        try {
                            List<Object> record = abbrev.readRecord(reader);
                            ctx.onAbbrevRecord((Integer) record.get(0), record.subList(1, record.size()));
                        } catch (Throwable e) {
                            throw new IllegalStateException("Can't read abbrev record with abbrev id " + code, e);
                        }
                    } else {
                        throw new IllegalStateException("code = " + code + ", abbrevs = " + ctx.abbrevs);
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("Can't parse block: " + ctx);
            e.printStackTrace(System.err);
            reader.position(exitPos);
        } finally {
            log("POPING");
            INDENT -= 3;

            if (exitPos != reader.position()) {
                throw new IllegalStateException("exitPos: " + exitPos + ", but pos: " + reader.position());
            }
        }
    }

    private static void log(String msg) {
        for (int i = 0; i < INDENT; i++) {
            System.out.print(' ');
        }
        System.out.println(msg);
    }

    private static boolean skipBlock(int blockId) {
        return blockId < 0;
//        if (Arrays.asList(0,8,12,15).contains(blockId) || true) {
//            return false;
//        }
//        return true;
    }

    private BlockContext getContext(Context parentCtx, int blockId, int abbrevLen) {
        SortedMap<Integer, Abbrev> abbrevs = abbrevsForOtherBlocks.compute(blockId, (key, oldMap) -> oldMap != null ? new TreeMap<>(oldMap) : new TreeMap<>());

        if (blockId == BlockId.BLOCKINFO.blockId) {
            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                Integer definedBlock = null; // or blockId?

                @Override
                public void define(Abbrev abbrev) {
                    SortedMap<Integer, Abbrev> abbrevs = abbrevsForOtherBlocks.computeIfAbsent(definedBlock, definedBlock -> new TreeMap<>());
                    push(abbrev, abbrevs);
                }

                @Override
                public void onAbbrevRecord(int codeValue, List<Object> record) {
                    throw new IllegalStateException("BLOCKINFO shouldn't contain any abbrev records");
                }

                @Override
                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    if (codeValue == 1) { // todo magic
                        definedBlock = (Integer) record.get(0);
                        return;
                    }

                    throw new NotImplementedException("blockId: " + blockId + ", code = " + codeValue);
                }
            };
        }

        if (blockId == BlockId.TYPE_BLOCK.blockId) {
            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                String typeName;

                @Override
                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    switch ((TypeCodes) code) {
                        case TYPE_CODE_NUMENTRY:
                            // todo what to do?
                            break;
                        case TYPE_CODE_FLOAT:
                        case TYPE_CODE_DOUBLE:
                        case TYPE_CODE_X86_FP80:
                            types.add(new FloatType((TypeCodes) code));
                            break;
                        case TYPE_CODE_INTEGER:
                            types.add(new IntegerType((Integer) record.get(0), (TypeCodes) code));
                            break;
                        case TYPE_CODE_POINTER:
                            int baseTypeId = (Integer) record.get(0);
                            PointerType pointerType = new PointerType((TypeCodes) code);

                            putType(baseTypeId, pointerType::setElementType);

                            types.add(pointerType);
                            break;
                        case TYPE_CODE_ARRAY:
                            int size = recordReader.nextInt();
                            int elementTypeId = recordReader.nextInt();

                            ArrayType arrayType = new ArrayType((TypeCodes) code, size);

                            putType(elementTypeId, arrayType::setElementType);

                            types.add(arrayType);
                            break;
                        case TYPE_CODE_LABEL:
                            types.add(new LabelType((TypeCodes) code));
                            break;
                        case TYPE_CODE_VOID:
                            types.add(VoidType.INSTANCE);
                            break;
                        case TYPE_CODE_FUNCTION:
                            boolean vararg = recordReader.nextInt() != 0;
                            List<Integer> typeIds = (List<Integer>) record.get(1);
                            int returnTypeId = typeIds.get(0);
                            Type[] paramTypes = new Type[typeIds.size() - 1];
                            for (int i = 0; i < paramTypes.length; i++) {
                                paramTypes[i] = types.get(typeIds.get(i + 1));
                            }

                            FunctionType functionType = new FunctionType(paramTypes, (TypeCodes) code, vararg);

                            putType(returnTypeId, functionType::setReturnType);

                            types.add(functionType);
                            break;
                        case TYPE_CODE_OPAQUE:
                            types.add(new OpaqueType(typeName, (TypeCodes) code));
                            typeName = null;
                            break;
                        case TYPE_CODE_STRUCT_NAME:
                            typeName = recordReader.nextString();
                            break;
                        case TYPE_CODE_STRUCT_ANON:
                        case TYPE_CODE_STRUCT_NAMED:
                            List<Integer> fieldTypeIds = (List<Integer>) record.get(1);
                            List<Type> fieldTypes = fieldTypeIds.stream().map(typeId -> types.get(typeId)).collect(Collectors.toList());
                            Type structureType = new StructureType(typeName, fieldTypes, (TypeCodes) code);
                            types.add(structureType);
                            typeName = null;
                            break;
                        case TYPE_CODE_METADATA:
                            types.add(new MetadataType((TypeCodes) code));
                            break;
                        default:
                            throw new NotImplementedException("blockId: " + blockId + ", code = " + codeValue);
                    }
                }

                private void putType(int typeId, Consumer<Type> setter) {
                    if (typeId < types.size()) {
                        setter.accept(types.get(typeId));
                    } else {
                        resolutions.add(() -> setter.accept(types.get(typeId)));
                    }
                }
            };
        }

        if (blockId == BlockId.CONSTANTS_BLOCK.blockId) {
            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                Type currentType = null;

                @Override
                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    switch((ConstantsCodes)code) {
                        case CST_CODE_SETTYPE:
                            currentType = getTypeByID((Integer) record.get(0));
                            return;
                        case CST_CODE_NULL:
                            getValues().add(currentType instanceof PointerFamilyType ? new NullValue(currentType) : new IntegerValue(currentType, 0));
                            return;
                        case CST_CODE_INTEGER:
                            getValues().add(new IntegerValue(currentType, decodeSignRotatedValue((Integer)record.get(0))));
                            return;
                        case CST_CODE_CE_INBOUNDS_GEP: // [ty, n x operands]
                        case CST_CODE_CE_GEP: // [ty, n x operands]
                        case CST_CODE_CE_GEP_WITH_INRANGE_INDEX: { // [ty, flags, n x operands]
                            int OpNum = 0;
                            Type PointeeType = null;
                            if (code == ConstantsCodes.CST_CODE_CE_GEP_WITH_INRANGE_INDEX ||
                                    (record.size() % 2)!=0)
                                PointeeType = getTypeByID((Integer) record.get(OpNum++));

                            boolean InBounds = false;
                            int InRangeIndex; // Optional<>
                            if (code == ConstantsCodes.CST_CODE_CE_GEP_WITH_INRANGE_INDEX) {
                                int Op = (Integer) record.get(OpNum++);
                                InBounds = (Op & 1)!=0;
                                InRangeIndex = Op >> 1;
                            } else if (code == ConstantsCodes.CST_CODE_CE_INBOUNDS_GEP)
                                InBounds = true;

                            List<Value> Elts = new ArrayList<>();
                            while (OpNum != record.size()) {
                                Type ElTy = getTypeByID((Integer) record.get(OpNum++));
                                Elts.add(getValues().get((Integer) record.get(OpNum++)/*, ElTy*/));
                                //Elts.push_back(getConstantFwdRef((Integer) record.get(OpNum++), ElTy));
                            }

//                            if (PointeeType &&
//                                    PointeeType !=
//                                            cast<PointerType>(Elts[0]->getType()->getScalarType())
//                  ->getElementType())
//                            return error("Explicit gep operator type does not match pointee type "
//                                    "of pointer operand");

//                            if (Elts.size() < 1)
//                                return error("Invalid gep with no operands");

//                            ArrayRef<Constant *> Indices(Elts.begin() + 1, Elts.end());
//                            V = ConstantExpr::getGetElementPtr(PointeeType, Elts[0], Indices,
//                                    InBounds, InRangeIndex);
                            getValues().add(new ConstantExpression(FunctionCodes.FUNC_CODE_INST_GEP, PointeeType, Elts.toArray(new Value[0])));
                            return;
                        }
                    }

                    throw new NotImplementedException("blockId: " + blockId + ", code = " + codeValue);
                }

                int decodeSignRotatedValue(int V) {
                    if ((V & 1) == 0) {
                        return V >> 1;
                    }
                    if (V != 1) {
                        return -(V >> 1);
                    }

                    // There is no such thing as -0 with integers.  "-0" really means MININT.
                    return Integer.MIN_VALUE;
                }
            };
        }

        if (blockId == BlockId.FUNCTION_BLOCK.blockId) {
            List<Instruction> instructions = new ArrayList<>();
            Function definedFunction = functionsWithBodies.get(bodies++);
            definedFunction.setInstructions(instructions);

            List<Value> functionValues = new ArrayList<>(parentCtx.getValues());
            functionValues.addAll(definedFunction.getArguments());
            List<Object> functionMetadata = new ArrayList<>(parentCtx.getMetadata());

            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                private DebugLoc lastDebugLoc;

                @Override
                List<Value> getValues() {
                    return functionValues;
                }

                @Override
                List<Object> getMetadata() {
                    return functionMetadata;
                }

                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    switch ((FunctionCodes) code) {
                        case FUNC_CODE_DECLAREBLOCKS:

                            /*
                                  if (Record.size() < 1 || Record[0] == 0)
        return error("Invalid record");
      // Create all the basic blocks for the function.
      FunctionBBs.resize(Record[0]);

      // See if anything took the address of blocks in this function.
      auto BBFRI = BasicBlockFwdRefs.find(F);
      if (BBFRI == BasicBlockFwdRefs.end()) {
        for (unsigned i = 0, e = FunctionBBs.size(); i != e; ++i)
          FunctionBBs[i] = BasicBlock::Create(Context, "", F);
      } else {
        auto &BBRefs = BBFRI->second;
        // Check for invalid basic block references.
        if (BBRefs.size() > FunctionBBs.size())
          return error("Invalid ID");
        assert(!BBRefs.empty() && "Unexpected empty array");
        assert(!BBRefs.front() && "Invalid reference to entry block");
        for (unsigned I = 0, E = FunctionBBs.size(), RE = BBRefs.size(); I != E;
             ++I)
          if (I < RE && BBRefs[I]) {
            BBRefs[I]->insertInto(F);
            FunctionBBs[I] = BBRefs[I];
          } else {
            FunctionBBs[I] = BasicBlock::Create(Context, "", F);
          }

        // Erase from the table.
        BasicBlockFwdRefs.erase(BBFRI);
      }

      CurBB = FunctionBBs[0];
                            * */

                            return;
                        case FUNC_CODE_INST_BINOP:
                            addInstruction(new Instruction((FunctionCodes) code, null, null, null));
                            return;
                        case FUNC_CODE_INST_ALLOCA:
                            /*
                                  if (Record.size() != 4)
        return error("Invalid record");
      uint64_t AlignRecord = Record[3];
      const uint64_t InAllocaMask = uint64_t(1) << 5;
      const uint64_t ExplicitTypeMask = uint64_t(1) << 6;
      const uint64_t SwiftErrorMask = uint64_t(1) << 7;
      const uint64_t FlagMask = InAllocaMask | ExplicitTypeMask |
                                SwiftErrorMask;
      bool InAlloca = AlignRecord & InAllocaMask;
      bool SwiftError = AlignRecord & SwiftErrorMask;
      Type *Ty = getTypeByID(Record[0]);
      if ((AlignRecord & ExplicitTypeMask) == 0) {
        auto *PTy = dyn_cast_or_null<PointerType>(Ty);
        if (!PTy)
          return error("Old-style alloca with a non-pointer type");
        Ty = PTy->getElementType();
      }
      Type *OpTy = getTypeByID(Record[1]);
      Value *Size = getFnValueByID(Record[2], OpTy);
      unsigned Align;
      if (Error Err = parseAlignmentValue(AlignRecord & ~FlagMask, Align)) {
        return Err;
      }
      if (!Ty || !Size)
        return error("Invalid record");
      AllocaInst *AI = new AllocaInst(Ty, Size, Align);
      AI->setUsedWithInAlloca(InAlloca);
      AI->setSwiftError(SwiftError);
      I = AI;
      InstructionList.push_back(I);
                            * */
                            int instTypeId = recordReader.nextInt();

                            Type sizeType = types.get(recordReader.nextInt());
                            Value size = getFnValueByID(recordReader.nextInt(), sizeType);

                            int align = recordReader.nextInt();
                            addInstruction(new Instruction((FunctionCodes) code, types.get(instTypeId)));
                            return;
                        case FUNC_CODE_INST_STORE: {
//                        Integer pointer = (Integer) record.get(0);
//                        Integer value = (Integer) record.get(1);
//                        Integer maybeType = (Integer) record.get(2);
//
//                        Integer unk0 = (Integer) record.get(3);
//                        assertEquals(unk0, 0);

                            AtomicInteger opNum = new AtomicInteger(0);
                            MutableObject<Value> val = new MutableObject<>();
                            MutableObject<Value> ptr = new MutableObject<>();

                            if (getValueTypePair(record, opNum, getValues().size(), ptr) || getValueTypePair(record, opNum, getValues().size(), val) || opNum.intValue() + 2 != record.size()) {
                                error("Invalid record");
                            }

                            System.out.println("nextValueNo = " + getValues().size());
                            System.out.println("opNum = " + opNum);
                            System.out.println("val = " + val);
                            System.out.println("ptr = " + ptr);

//                        if (Error Err = typeCheckLoadStoreInst(val->getElementType(), ptr->getElementType()))
//                        return Err;
//                        unsigned Align;
//                        if (Error Err = parseAlignmentValue(Record[opNum], Align))
//                        return Err;
                            //I = new StoreInst(val, ptr, Record[opNum+1], Align);
                            //InstructionList.push_back(I);

                            addInstruction(new Instruction((FunctionCodes) code, VoidType.INSTANCE, val.getValue(), ptr.getValue()));
                            return;
                        }
                        case FUNC_CODE_INST_LOAD: {
                            AtomicInteger opNum = new AtomicInteger(0);
                            MutableObject<Value> op = new MutableObject<>();
                            if (getValueTypePair(record, opNum, getValues().size(), op) || (opNum.intValue() + 2 != record.size() && opNum.intValue() + 3 != record.size())) {
                                error("Invalid record");
                            }

                            Value value = op.getValue();
                            //PointerFamilyType opType = (PointerFamilyType) value.getType();
                            addInstruction(new Instruction((FunctionCodes) code, value.getType(), value));
                            return;
                        }
                        case FUNC_CODE_DEBUG_LOC:
                            int line = (int) record.get(0);
                            int col = (int) record.get(1);
                            int scopeId = (int) record.get(2);
                            int iaid = (int) record.get(3);

                            DISubprogram scope = (DISubprogram) getMDOrNull(scopeId);
                            Object ia = getMDOrNull(iaid);

                            lastDebugLoc = new DebugLoc(line, col, scope, ia);
                        case FUNC_CODE_DEBUG_LOC_AGAIN:
                            instructions.get(instructions.size() - 1).setDebugLoc(lastDebugLoc);
                            return;
                        case FUNC_CODE_INST_CAST:
                            Value op = getTypedValue(recordReader);
                            Type type = types.get(recordReader.nextInt());
                            int opcode = recordReader.nextInt();
                            addInstruction(new Instruction((FunctionCodes) code, type, op));
                            return;
                        case FUNC_CODE_INST_CALL:

                            // CALL: [paramattrs, cc, fmf, fnty, fnid, arg0, arg1...]
                            int attributesId = recordReader.nextInt();
                            //unsigned OpNum = 0;
                            //AttributeSet PAL = getAttributes(Record[OpNum++]);
                            int CCInfo = recordReader.nextInt();
                            //unsigned CCInfo = Record[OpNum++];

                            //FastMathFlags FMF;
                            if (((CCInfo >> CallMarkerFlags.CALL_FMF) & 1) != 0) {
                                recordReader.nextInt();
                                //FMF = getDecodedFastMathFlags(Record[OpNum++]);
                                //if (!FMF.any())
                                //    return error("Fast math flags indicator set for call with no FMF");
                            }

                            FunctionType FTy = null;
                            if (((CCInfo >> CallMarkerFlags.CALL_EXPLICIT_TYPE & 1) != 0) &&
                                    (FTy = (FunctionType) getTypeByID(recordReader.nextInt())) == null)
                                error("Explicit call type is not a function type");

                            Value Callee;
                            Callee = getValueTypePair(recordReader);

                            PointerType OpTy = (PointerType) Callee.getType();
                            if (FTy == null) {
                                FTy = (FunctionType) OpTy.getElementType();
                            } else if (OpTy.getElementType() != FTy)
                                error("Explicit call type does not match pointee type of callee operand");
//                            if (record.size() < FTy->getNumParams() + OpNum)
//                                return error("Insufficient operands to call");

                            if (FTy == null) {
                                FTy = (FunctionType) Callee.getType();
                            }

                            List<Value> Args = new ArrayList<>();
                            // Read the fixed params.
                            for (Type paramType : FTy.getParamTypes()) {
                                if (paramType instanceof LabelType) {
                                    throw new NotImplementedException();
                                } else {
                                    Args.add(getValue(recordReader.nextInt(), paramType));
                                }
                            }

                            // Read type/value pairs for varargs params.
                            if (!FTy.isVararg()) {
                                if (recordReader.hasNext()) {
                                    error("Invalid record");
                                }
                            } else {
                                while (recordReader.hasNext()) {
                                    Args.add(getValueTypePair(recordReader));
                                }
                            }

                            Args.add(Callee);
                            addInstruction(new Instruction((FunctionCodes) code, FTy.getReturnType(), Args.toArray(new Value[0])));
                            return;

                        case FUNC_CODE_INST_INBOUNDS_GEP_OLD:
                        case FUNC_CODE_INST_GEP_OLD:
                        case FUNC_CODE_INST_GEP: { // GEP: type, [n x operands]
                            int OpNum = 0;

                            Type Ty;
                            boolean InBounds;

                            if (code == FunctionCodes.FUNC_CODE_INST_GEP) {
                                InBounds = recordReader.nextInt() != 0;
                                Ty = getTypeByID(recordReader.nextInt());
                            } else {
                                InBounds = code == FunctionCodes.FUNC_CODE_INST_INBOUNDS_GEP_OLD;
                                Ty = null;
                            }

                            Value BasePtr = getValueTypePair(recordReader);

//                            if (Ty == null)
//                                Ty = cast<PointerType>(BasePtr->getType()->getScalarType())->getElementType();
//      else if (Ty !=
//                                    cast<PointerType>(BasePtr->getType()->getScalarType())
//                   ->getElementType())
//                            return error(
//                                    "Explicit gep type does not match pointee type of pointer operand");

                            List<Value> GEPIdx = new ArrayList<>();
                            GEPIdx.add(BasePtr);
                            while(recordReader.hasNext()) {
                                GEPIdx.add(getValueTypePair(recordReader));
                            }

                            addInstruction(new Instruction((FunctionCodes) code, Ty, GEPIdx.toArray(new Value[0])));
//                            if (InBounds)
//                                cast<GetElementPtrInst>(I)->setIsInBounds(true);
                            return;
                        }
                        case FUNC_CODE_INST_RET:
                            if (record.isEmpty()) {
                                addInstruction(new Instruction((FunctionCodes) code, VoidType.INSTANCE));
                                return;
                            }
                            break;
                    }
                    throw new NotImplementedException("blockId: " + blockId + ", code = " + codeValue);
                }

                private void addInstruction(Instruction instruction) {
                    if (!instructions.isEmpty()) {
                        instructions.get(instructions.size() - 1).setNext(instruction);
                    }
                    if (instruction.getType() != null && !(instruction.getType() instanceof VoidType)) {
                        getValues().add(instruction);
                    }

                    instructions.add(instruction);
                }

                Value getTypedValue(RecordReader reader) {
                    int valueId = reader.nextInt();

                    int valuesCount = getValues().size();
                    if (useRelativeIds) {
                        valueId = valuesCount - valueId;
                    }

                    if (valueId < valuesCount) {
                        // known value
                        return getValues().get(valueId);
                    }

                    int typeId = reader.nextInt();
                    throw new NotImplementedException("forward declaration");
                }

                /// Read a value/type pair out of the specified record from slot 'Slot'.
                /// Increment Slot past the number of slots used in the record. Return true on
                /// failure.
                boolean getValueTypePair(List<Object> record, final AtomicInteger slot,
                                         int instNum, final MutableObject<Value> resVal) {
                    if (slot.intValue() == record.size()) {
                        throw new IllegalStateException("slot.intValue() == record.size()");
                    }
                    Integer valNo = (Integer) record.get(slot.getAndIncrement());
                    // Adjust the ValNo, if it was encoded relative to the InstNum.
                    if (useRelativeIds)
                        valNo = instNum - valNo;
                    if (valNo < instNum) {
                        // If this is not a forward reference, just return the value we already
                        // have.
                        resVal.setValue(getFnValueByID(valNo, null));
                        if (resVal.getValue() == null) {
                            throw new IllegalStateException("resVal.getValue() == null");
                        }
                        return resVal.getValue() == null;
                    }
                    if (slot.intValue() == record.size()) {
                        throw new IllegalStateException("slot.intValue() == record.size()");
                    }

                    Integer typeNo = (Integer) record.get(slot.getAndIncrement());
                    resVal.setValue(getFnValueByID(valNo, getTypeByID(typeNo)));

                    if (resVal.getValue() == null) {
                        throw new IllegalStateException("resVal.getValue() == null");
                    }
                    return resVal.getValue() == null;
                }

                Value getValueTypePair(RecordReader recordReader) {
                    int instNum = getValues().size();
                    Integer valNo = recordReader.nextInt();
                    // Adjust the ValNo, if it was encoded relative to the InstNum.
                    if (useRelativeIds)
                        valNo = instNum - valNo;
                    if (valNo < instNum) {
                        // If this is not a forward reference, just return the value we already
                        // have.
                        return getFnValueByID(valNo, null);
                    }

                    Integer typeNo = recordReader.nextInt();
                    return getFnValueByID(valNo, getTypeByID(typeNo));
                }

                /*Value getValue(List<Object> record, final AtomicInteger slot,
                               int instNum, Type Ty) {
                    if (slot.intValue() == record.size()) return null;
                    unsigned ValNo = (unsigned)Record[Slot];
                    // Adjust the ValNo, if it was encoded relative to the InstNum.
                    if (UseRelativeIDs)
                        ValNo = InstNum - ValNo;
                    return getFnValueByID(ValNo, Ty);
                }*/

                Value getValue(int valNo, Type type) {
                    if (useRelativeIds) {
                        valNo = getValues().size() - valNo;
                    }
                    return getFnValueByID(valNo, type);
                }

                private Value getFnValueByID(int valueNo, Type type) {
                    if (type instanceof MetadataType) {
                        return new Value(type);
                        //return (Value) getMetadata().get(valueNo);
                    }
                    if (type == null) {
                        return getValues().get(valueNo);
                    }

                    return getValues().get(valueNo);

                    //return new Value(type);
                }

                /*
                  Value *getFnValueByID(unsigned ID, Type *Ty) {
    if (Ty && Ty->isMetadataTy())
      return MetadataAsValue::get(Ty->getContext(), getFnMetadataByID(ID));
    return ValueList.getValueFwdRef(ID, Ty);
  }

  Value *BitcodeReaderValueList::getValueFwdRef(unsigned Idx, Type *Ty) {
  // Bail out for a clearly invalid value. This would make us call resize(0)
  if (Idx == std::numeric_limits<unsigned>::max())
    return nullptr;

  if (Idx >= size())
    resize(Idx + 1);

  if (Value *V = ValuePtrs[Idx]) {
    // If the types don't match, it's invalid.
    if (Ty && Ty != V->getType())
      return nullptr;
    return V;
  }

  // No type specified, must be invalid reference.
  if (!Ty)
    return nullptr;

  // Create and return a placeholder, which will later be RAUW'd.
  Value *V = new Argument(Ty);
  ValuePtrs[Idx] = V;
  return V;
}
                * */
            };
        }

        if (blockId == BlockId.VALUE_SYMTAB_BLOCK.blockId) {
            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    switch((ValueSymtabCodes)code) {
                        case VST_CODE_BBENTRY:
                            return;
                        case VST_CODE_FNENTRY: {
                            Value value = getValues().get((Integer) record.get(0));
                            value.setName((String) record.get(2));
                            return;
                        }
                        case VST_CODE_ENTRY: {
                            Value value = getValues().get((Integer) record.get(0));
                            value.setName((String) record.get(1));
                            return;
                        }
                    }

                    throw new NotImplementedException("blockId: " + blockId + ", code = " + codeValue);
                }
            };
        }

        if (blockId == BlockId.METADATA_BLOCK.blockId) {
            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                MetadataCodes lastCode;
                String metadataName;

                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    if (lastCode == MetadataCodes.METADATA_NAME && code != MetadataCodes.METADATA_NAMED_NODE) {
                        throw new IllegalStateException();
                    }

                    lastCode = (MetadataCodes) code;
                    System.out.println("metadata.size() = " + getMetadata().size());
                    switch ((MetadataCodes)code) {
                        case METADATA_STRING_OLD:
                            String stringOld = recordAsString(record);
                            getMetadata().add(stringOld);
                            return;
                        case METADATA_STRINGS:
                            int count = (int) record.get(0);
                            int offset = (int) record.get(1);
                            byte[] blob = (byte[]) record.get(2);

                            ByteBuffer buffer = ByteBuffer.wrap(blob, 0, offset);
                            buffer.order(ByteOrder.LITTLE_ENDIAN);
                            BitstreamReader blobReader = new BitstreamReader(buffer);

                            for (int i = 0; i < count; i++) {
                                int length = blobReader.readVBR(6);
                                getMetadata().add(new String(blob, offset, length));
                                offset += length;
                            }
                            return;
                        case METADATA_FILE: {
                            Integer distinct = (Integer) record.get(0);
                            Integer filename = (Integer) record.get(1);
                            Integer directory = (Integer) record.get(2);
                            getMetadata().add(new DIFile((String)getMDOrNull(filename), (String)getMDOrNull(directory)));
                            return;
                        }
                        case METADATA_NODE: {
                            List<Object> node = resolveMetadata(record);
                            System.out.println("node = " + node);
                            getMetadata().add(node);
                            return;
                        }
                        case METADATA_SUBPROGRAM: {
                            boolean distinct = ((((int) record.get(0)) & 1) != 0) || ((int) record.get(8) != 0);
                            int fileIndex = (Integer) record.get(4);
                            DISubprogram subprogram = new DISubprogram(
//                                    getDITypeRefOrNull(Record[1]),          // scope
//                                    getMDString(Record[2]),                 // name
//                                    getMDString(Record[3]),                 // linkageName
                                    null                 // file
//                                    Record[5],                              // line
//                                    getMDOrNull(Record[6]),                 // type
//                                    Record[7],                              // isLocal
//                                    Record[8],                              // isDefinition
//                                    Record[9],                              // scopeLine
//                                    getDITypeRefOrNull(Record[10]),         // containingType
//                                    Record[11],                             // virtuality
//                                    Record[12],                             // virtualIndex
//                                    HasThisAdj ? Record[19] : 0,            // thisAdjustment
//                                    static_cast<DINode::DIFlags>(Record[13] // flags
//                                    ),
//                                    Record[14],                       // isOptimized
//                                    HasUnit ? CUorFn : nullptr,       // unit
//                                    getMDOrNull(Record[15 + Offset]), // templateParams
//                                    getMDOrNull(Record[16 + Offset]), // declaration
//                                    getMDOrNull(Record[17 + Offset])  // variables
                            );

                            putMdOrNull(fileIndex, subprogram::setFile);

                            getMetadata().add(subprogram);
                            return;
                        }
                        case METADATA_VALUE:
                            int typeNum = (int) record.get(0);
                            int valueNum = (int) record.get(1);

                            if (valueNum >= getValues().size()) {
                                // todo wtf?
                                getMetadata().add(record);
                                return;
                            }

                            Type type = getTypeByID(typeNum);
                            Value value = getValues().get(valueNum);

                            if (!value.getType().equals(type)) {
                                System.out.println("!!! type mismatch? value = " + value + ", type = " + type);
                            }

                            getMetadata().add(value);
                            return;
                        case METADATA_NAME:
                            metadataName = recordAsString(record);
                            return;
                        case METADATA_NAMED_NODE: {
                            List<Object> node = record.stream().mapToInt(f -> (Integer) f).mapToObj(i -> getMetadata().get(i)).collect(Collectors.toList());
                            namedMetadata.put(metadataName, node);
                            return;
                        }
                        default:
                            getMetadata().add(record);
                    }
                }

                private <T> void putMdOrNull(int mdId, Consumer<T> setter) {
                    if (mdId == 0) {
                        return;
                    }

                    int index = mdId - 1;

                    if (index < getMetadata().size()) {
                        setter.accept((T) getMetadata().get(index));
                    } else {
                        resolutions.add(() -> setter.accept((T) getMetadata().get(index)));
                    }
                }

                private List<Object> resolveMetadata(List<Object> record) {
                    return record.stream().mapToInt(f -> (Integer) f).mapToObj(this::getMDOrNull).collect(Collectors.toList());
                }

                @Override
                public void finish() {
                    if (lastCode == MetadataCodes.METADATA_NAME) {
                        throw new IllegalStateException();
                    }

                    super.finish();
                }

                private String recordAsString(List<Object> record) {
                    List<Integer> data = (List<Integer>) record.get(0);
                    char[] chars = new char[data.size()];
                    for (int i = 0; i < chars.length; i++) {
                        chars[i] = (char) data.get(i).intValue();
                    }
                    return new String(chars);
                }
            };
        }

        if (blockId == BlockId.MODULE_BLOCK.blockId) {
            return new BlockContext(parentCtx, abbrevLen, blockId, abbrevs) {
                void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {
                    if (code == null) {
                        System.err.println("blockId: " + blockId + ", code = " + codeValue);
                        return;
                    }

                    switch ((ModuleCodes)code) {
                        case MODULE_CODE_VERSION:
                            Integer version = (Integer) record.get(0);
                            if (version == 1) {
                                useRelativeIds = true;
                                return;
                            }
                            throw new IllegalStateException("Unsupported version: " + version);
                        case MODULE_CODE_TRIPLE:
                        case MODULE_CODE_DATALAYOUT:
                        case MODULE_CODE_HASH:
                        case MODULE_CODE_METADATA_VALUES_UNUSED:
                        case MODULE_CODE_VSTOFFSET:
                        case MODULE_CODE_SOURCE_FILENAME:
                            // ignoring
                            break;
                        case MODULE_CODE_GLOBALVAR:
                            Type type = getTypeByID((Integer) record.get(0));
                            boolean isConstant = ((int)record.get(1) & 1) != 0;
                            boolean explicitType = ((int)record.get(1) & 2) != 0;

                            if (explicitType) {
                                int AddressSpace = (int)record.get(1) >> 2;
                            } else {
//                                if (!Ty->isPointerTy())
//                                    return error("Invalid type for value");
//                                AddressSpace = cast<PointerType>(Ty)->getAddressSpace();
                                type = ((PointerType)type).getElementType();
                            }

                            PointerType pointerToGlobalType = new PointerType(TypeCodes.TYPE_CODE_POINTER);
                            pointerToGlobalType.setElementType(type);

                            GlobalVariable globalVariable = new GlobalVariable(pointerToGlobalType);

                            int initializerId = (int) record.get(2);
                            if (initializerId != 0) {
                                initializerId--;
                                if (getValues().size() > initializerId) {
                                    globalVariable.setInitializer(getValues().get(initializerId));
                                } else {
                                    initializersForGlobals.computeIfAbsent(initializerId, ArrayList::new).add(globalVariable);
                                }
                            }

                            if (!globalVariables.isEmpty()) {
                                globalVariables.get(globalVariables.size() - 1).setNext(globalVariable);
                            }
                            globalVariables.add(globalVariable);

                            getValues().add(globalVariable);
                            break;
                        case MODULE_CODE_FUNCTION:
                            int typeId = recordReader.nextInt();
                            Integer isProto = (Integer) record.get(2);

                            Type functionType = types.get(typeId);
                            PointerType pointerToFunctionType;
                            if (functionType instanceof PointerType) {
                                pointerToFunctionType = (PointerType) functionType;
                            } else {
                                pointerToFunctionType = new PointerType(TypeCodes.TYPE_CODE_POINTER);
                                pointerToFunctionType.setElementType(functionType);
                            }

                            Function function = new Function(pointerToFunctionType);

                            if (isProto == 0) {
                                if (!functionsWithBodies.isEmpty()) {
                                    functionsWithBodies.get(functionsWithBodies.size() - 1).setNext(function);
                                }

                                functionsWithBodies.add(function);
                            }

                            getValues().add(function);
                            break;
                        default:
                            throw new NotImplementedException("blockId: " + blockId + ", code = " + codeValue);
                    }
                }

                @Override
                public void finish() {
                    for (Map.Entry<Integer, List<GlobalVariable>> globalsForInitializer : initializersForGlobals.entrySet()) {
                        Value initializer = getValues().get(globalsForInitializer.getKey());
                        for (GlobalVariable globalVariable : globalsForInitializer.getValue()) {
                            globalVariable.setInitializer(initializer);
                        }
                    }
                }
            };
        }

        return new BlockContext(parentCtx, abbrevLen, blockId, abbrevsForOtherBlocks.getOrDefault(blockId, new TreeMap<>()));
    }

    private Type getTypeByID(Integer typeNo) {
        return types.get(typeNo);
    }

    <T> T error(String s) {
        throw new IllegalStateException(s);
    }

    private void assertEquals(Integer actual, int expected) {
        if (actual != expected) {
            throw new IllegalStateException(actual + " != " + expected);
        }
    }

    private static int INDENT = 0;

    public Module readModule(String file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        ByteBuffer header = ByteBuffer.allocate(100);
        header.order(ByteOrder.LITTLE_ENDIAN);

        raf.getChannel().read(header);
        header.flip();

        if (header.getInt() == 0x0B17C0DE) {
            // Bitcode Wrapper Format
            //noinspection unused yet
            int version = header.getInt();
            int offset = header.getInt();
            int size = header.getInt();
            System.out.println("size = " + size);
            //noinspection unused yet
            int cpuType = header.getInt();

            ByteBuffer body = ByteBuffer.allocate(size);
            body.order(ByteOrder.LITTLE_ENDIAN);
            raf.getChannel().read(body, offset);
            body.flip();

            return readModule(body);
        } else {
            // optimistic
            ByteBuffer body = ByteBuffer.allocate(Math.toIntExact(new File(file).length()));
            body.order(ByteOrder.LITTLE_ENDIAN);
            raf.getChannel().read(body, 0);
            body.flip();

            return readModule(body);
        }
    }

    public abstract static class Context {
        protected final SortedMap<Integer, Abbrev> abbrevs;
        int abbrevLen;
        private Context parent;

        public Context(SortedMap<Integer, Abbrev> abbrevs, int abbrevLen) {
            this.abbrevs = abbrevs;
            this.abbrevLen = abbrevLen;
        }

        public void define(Abbrev abbrev) {
            push(abbrev, abbrevs);
        }

        static void push(Abbrev abbrev, SortedMap<Integer, Abbrev> abbrevs) {
            int newKey = abbrevs.isEmpty() ? 4 : abbrevs.lastKey() + 1;
            abbrevs.put(newKey, abbrev);
        }

        void onAbbrevRecord(int codeValue, List<Object> record) {
            throw new IllegalStateException("recordCode = " + codeValue + ", " + "fields = " + record);
        }

        void onUnabbrevRecord(int codeValue, List<Object> record) {
            throw new IllegalStateException("recordCode = " + codeValue + ", " + "fields = " + record);
        }

        abstract List<Value> getValues();

        abstract List<Object> getMetadata();

        public abstract void finish();

        public abstract Context getParent();
    }

    private static class BlockContext extends Context {
        final Context parentCtx;
        final int blockId;
        protected final List<Runnable> resolutions = new ArrayList<>();

        BlockContext(Context parentCtx, int abbrevLen, int blockId, SortedMap<Integer, Abbrev> abbrevs) {
            super(abbrevs, abbrevLen);
            this.parentCtx = parentCtx;
            this.blockId = blockId;
        }

        @Override
        public Context getParent() {
            return parentCtx;
        }

        @Override
        public void finish() {
            resolutions.forEach(Runnable::run);
        }

        void onAbbrevRecord(int codeValue, List<Object> record) {
            Codes codes = Codes.get(blockId, codeValue);
            log("recordCode = " + formatCode(codeValue) + ", " + "fields = " + record);
            onRecord(codes, codeValue, record, new RecordReader(record));
        }

        private String formatCode(int codeValue) {
            Codes codes = Codes.get(blockId, codeValue);
            return codeValue + " (" + codes + ')';
        }

        void onRecord(Codes code, int codeValue, List<Object> record, RecordReader recordReader) {

        }

        final void onUnabbrevRecord(int codeValue, List<Object> record) {
            Codes codes = Codes.get(blockId, codeValue);
            log("recordCode = " + formatCode(codeValue) + ", " + "fields = " + record);
            onRecord(codes, codeValue, record, new RecordReader(record));
        }

        @Override
        List<Value> getValues() {
            return parentCtx.getValues();
        }

        @Override
        List<Object> getMetadata() {
            return parentCtx.getMetadata();
        }

        protected Object getMDOrNull(int index) {
            if (index == 0) {
                return null;
            }
            return getMetadata().get(index - 1);
        }

        @Override
        public String toString() {
            return blockId + " (" + BlockId.get(blockId) + ')';
        }
    }
}
