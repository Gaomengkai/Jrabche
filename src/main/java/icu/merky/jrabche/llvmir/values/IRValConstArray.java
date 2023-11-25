package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.fe.helper.ConstInitList;
import icu.merky.jrabche.fe.helper.FEVisitorHelper;
import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.types.IRType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class IRValConstArray extends IRValConst {
    private final List<ValType> valTypes; // witch
    /**
     * Inner values may be null. Check first.
     */
    private final List<IRValConst> childVals;
    /**
     * Inner values may be null. Check first.
     */
    private final List<IRValConstArray> childArrays;
    private final List<Integer> shapes;

    /**
     * Create a new constant array value.
     *
     * @param ty The type of the array. Need initialize the shape of the array.
     */
    public IRValConstArray(ArrayType ty) {
        super(ty);
        this.shapes = new Vector<>();
        // from "ty" initialize "shapes"
        IRType cur = ty;
        while (cur instanceof ArrayType a) {
            this.shapes.add(a.getSize());
            cur = a.getElementType();
        }
        valTypes = new ArrayList<>(this.shapes.get(0));
        childVals = new ArrayList<>(this.shapes.get(0));
        childArrays = new ArrayList<>(this.shapes.get(0));
        for (int i = 0; i < this.shapes.get(0); i++) {
            valTypes.add(ValType.ZERO);
            childArrays.add(null);
            childVals.add(null);
        }
    }

    public IRValConstArray(ArrayType ty, List<ValType> valTypes, List<IRValConst> childVals, List<IRValConstArray> childArrays, List<Integer> shapes) {
        super(ty);
        this.valTypes = valTypes;
        this.childVals = childVals;
        this.childArrays = childArrays;
        this.shapes = shapes;
    }

    public IRValConstArray(IRValConstArray another) {
        super(another.getType());
        this.shapes = another.getShapes();
        this.valTypes = another.getValTypes();
        this.childVals = another.getChildVals();
        this.childArrays = another.getChildArrays();
    }

    public static IRValConstArray FromInitList(ConstInitList il, ArrayType arrayType) {
        var arr = new IRValConstArray(arrayType);
        var gen = new ConstArrayGenerator(il, arr);
        gen.gen();
        arr = gen.arr;
        arr.reduceZero();
        return arr;
    }

    public boolean reduceZero() {
        if (this.valTypes.size() == 0) {
            return true;
        }
        boolean allZero = true;
        for (int i = 0; i < valTypes.size(); i++) {
            ValType t = valTypes.get(i);
            if (t == ValType.ZERO) continue;
            if (t == ValType.VAL) {
                if (childVals.get(i).isZero()) {
                    valTypes.set(i, ValType.ZERO);
                } else {
                    allZero = false;
                }
            } else { // t==ValType.ARR
                if (childArrays.get(i).reduceZero()) {
                    valTypes.set(i, ValType.ZERO);
                } else {
                    allZero = false;
                }
            }
        }
        return allZero;
    }

    public IRValConst get(List<Integer> target) {
        if (valTypes.size() == 0) {
            return null;
        }
        var t = valTypes.get(target.get(0));
        if (t == ValType.ZERO) {
            return IRValConst.Zero(getArrayType().getAtomType());
        } else if (t == ValType.VAL) {
            return childVals.get(target.get(0));
        } else {
            return childArrays.get(target.get(0)).get(target.subList(1, target.size()));
        }
    }

    public List<ValType> getValTypes() {
        return valTypes;
    }

    public List<IRValConst> getChildVals() {
        return childVals;
    }

    public List<IRValConstArray> getChildArrays() {
        return childArrays;
    }

    public List<Integer> getShapes() {
        return shapes;
    }

    public void set(List<Integer> target, IRVal val) {
        setHelper(target, val, 0);
    }

    private void setHelper(List<Integer> target, IRVal val, int curDim) {
        if (curDim == target.size() - 1) {
            // last dim
            this.childVals.set(target.get(curDim), (IRValConst) val);
            this.valTypes.set(target.get(curDim), ValType.VAL);
        } else {
            if (this.childArrays.get(target.get(curDim)) == null) {
                this.childArrays.set(target.get(curDim), new IRValConstArray(
                        (ArrayType)
                                ((ArrayType) this.getType()).getElementType())
                );
                this.valTypes.set(target.get(curDim), ValType.ARR);
            }
            this.childArrays.get(target.get(curDim)).setHelper(target, val, curDim + 1);
        }
    }

    @Override
    public IRValConstArray clone() {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public String asValue() {
        // TODO
        StringBuilder sb = new StringBuilder();
        // sb.append(this.type.toString()).append(" ");
        if (this.valTypes.stream().allMatch(x -> x == ValType.ZERO)) {
            sb.append("zeroinitializer");
        } else {
            sb.append("[");
            for (int i = 0; i < valTypes.size(); i++) {
                ValType t = valTypes.get(i);
                IRType elemType = ((ArrayType) this.type).getElementType();
                sb.append(elemType.toString()).append(" ");
                if (t == ValType.ZERO) {
                    if (elemType.isFloat()) sb.append("0x");
                    sb.append(elemType.isScalar() ? "0" : "zeroinitializer");
                } else if (t == ValType.VAL) {
                    sb.append(childVals.get(i).asValue());
                } else {
                    sb.append(childArrays.get(i).asValue());
                }
                if (i != valTypes.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public enum ValType {ZERO, ARR, VAL}

    static class ConstArrayGenerator {
        public IRValConstArray arr;
        List<Integer> cur, shape;
        ConstInitList iList;
        IRBasicType basicType;

        public ConstArrayGenerator(ConstInitList iList, IRValConstArray arr) {
            this.arr = arr;
            this.cur = new ArrayList<>();
            for (int i = 0; i < arr.shapes.size(); i++) {
                cur.add(0);
            }
            this.shape = arr.shapes;
            this.basicType = arr.getArrayType().getAtomType();
            this.iList = iList;
        }

        public void gen() {
            gen(iList, cur, 0);
        }

        private void gen(ConstInitList val, List<Integer> pos, int d) {
            int pVal = 0, pArr = 0;
            for (int i = 0; i < val.witch.size(); i++) {
                if (val.witch.get(i) == ConstInitList.ILType.CV) {
                    var cVal = val.constVals.get(pVal++);
                    cVal = FEVisitorHelper.DoCompileTimeConversion(basicType, cVal);
                    arr.set(pos, cVal);
                    FEVisitorHelper.ArrayPosPlusN(shape, pos, 1, -1);
                } else {
                    int before = pos.get(d);
                    var il = val.initLists.get(pArr++);
                    gen(il, pos, d + 1);
                    if (before == pos.get(d)) {
                        FEVisitorHelper.ArrayPosPlusN(shape, pos, 1, d);
                    }
                }
            }
        }
    }
}
