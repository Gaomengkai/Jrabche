package icu.merky.jrabche.llvmir.types;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends IRType {

    private final List<IRType> paramsType;
    private IRType retType;

    public FunctionType() {
        super(IRBasicType.FUNCTION);
        paramsType = new ArrayList<>();
    }

    public FunctionType(IRType retType, List<IRType> paramsType) {
        super(IRBasicType.FUNCTION);
        this.retType = retType;
        this.paramsType = paramsType;
    }

    public IRType getRetType() {
        return retType;
    }

    public void setRetType(IRType retType) {
        this.retType = retType;
    }

    public List<IRType> getParamsType() {
        return paramsType;
    }

    public boolean addParamType(IRType type) {
        return paramsType.add(type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(retType.toString());
        sb.append(" (");
        for (int i = 0; i < paramsType.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramsType.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public IRType clone() {
        var newParamsType = List.copyOf(paramsType);
        return new FunctionType(retType.clone(), newParamsType);
    }
}
