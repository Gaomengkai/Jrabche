package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.helper.Helper;
import icu.merky.jrabche.helper.InitList;
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

    public static IRValConstArray FromInitList(InitList il, ArrayType arrayType) {
        var arr = new IRValConstArray(arrayType);
        var gen = new ConstArrayGenerator(il,arr);
        gen.gen();
        arr = gen.arr;
        return arr;
    }

    public IRVal get(List<Integer> target) {
        if(valTypes.size()==0) {
            return null;
        }
        var t = valTypes.get(target.get(0));
        if (t == ValType.ZERO) {
            return null;
        } else if (t == ValType.VAL) {
            return childVals.get(target.get(0));
        } else {
            return childArrays.get(target.get(0)).get(target.subList(1, target.size()));
        }
    }

    /**
     * /// According to shape, add N to cur.
     * /// \param shape [2][3]
     * /// \param cur [1][2]
     * /// \param N
     * /// \param startsAt
     * /// \param reset
     * void ArrayPosPlusN(
     * const std::deque<size_t>& shape,
     * std::deque<size_t>&       cur,
     * size_t                    N,
     * int                       startsAt = -1,
     * bool                      reset    = true
     * )
     * {
     * if (startsAt == -1) startsAt = shape.size() - 1;
     * for (int i = startsAt; i >= 0; --i) {   // ATTENTION!!!!!!!!
     * cur[i] += N;
     * if (cur[i] < shape[i]) { break; }
     * N = cur[i] / shape[i];
     * cur[i] %= shape[i];
     * }
     * if (reset) {
     * for (int i = startsAt + 1; i < shape.size(); ++i) { cur[i] = 0; }
     * }
     * }
     */
    static void ArrayPosPlusN(List<Integer> shape, List<Integer> cur, int N, int startsAt) {
        if (startsAt == -1) startsAt = shape.size() - 1;
        for (int i = startsAt; i >= 0; --i) {
            cur.set(i, cur.get(i) + N);
            if (cur.get(i) < shape.get(i)) {
                break;
            }
            N = cur.get(i) / shape.get(i);
            cur.set(i, cur.get(i) % shape.get(i));
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
        throw new NotImplementedException();
    }

    public enum ValType {ZERO, ARR, VAL}

    static class ConstArrayGenerator {
        public IRValConstArray arr;
        List<Integer> cur, shape;
        InitList iList;
        IRBasicType basicType;

        public ConstArrayGenerator(InitList iList,IRValConstArray arr) {
            this.arr = arr;
            this.cur = new ArrayList<>();
            for (int i = 0; i < arr.shapes.size(); i++) {
                cur.add(0);
            }
            this.shape = arr.shapes;
            this.basicType = arr.getArrayType().getAtomType();
            this.iList=iList;
        }

        public void gen() {
            gen(iList, cur, 0);
        }

        private void gen(InitList val, List<Integer> pos, int d) {
            int pVal = 0, pArr = 0;
            for (int i = 0; i < val.witch.size(); i++) {
                if (val.witch.get(i) == InitList.ILType.CV) {
                    var cVal = val.constVals.get(pVal++);
                    cVal = Helper.DoCompileTimeConversion(basicType, cVal);
                    arr.set(pos, cVal);
                    ArrayPosPlusN(shape, pos, 1, -1);
                } else {
                    int before = pos.get(d);
                    var il = val.initLists.get(pArr++);
                    gen(il, pos, d + 1);
                    if (before == pos.get(d)) {
                        ArrayPosPlusN(shape, pos, 1, d);
                    }
                }
            }
        }
    }
}
