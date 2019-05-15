package princeYang.mxcc.frontend;

import princeYang.mxcc.ast.*;
import princeYang.mxcc.errors.MxError;
import princeYang.mxcc.scope.*;

import java.util.ArrayList;
import java.util.List;

public class GlobalPreUseScanner extends ScopeScanner
{
    Scope globalScope;

    public GlobalPreUseScanner(Scope globalScope)
    {
        this.globalScope = globalScope;
    }

    private void entryCheck()
    {
        FuncEntity entity = globalScope.getFunc("main");
        if (entity == null)
            throw new MxError("Program entry \"main()\" not found!\n");
        if (entity.getFuncParas().size() != 0)
            throw new MxError("Program entry \"main\" shouldn't have args\n");
        if (!(entity.getRetType() instanceof IntType))
            throw new MxError("Program entry \"main\" should return int\n");
    }

    private void addBuildInFunc(Scope scope, String funcName, List<VarEntity> funcParas, Type retType)
    {
        FuncEntity funcEntity = new FuncEntity(funcName, new FuncType(funcName));
        funcEntity.setRetType(retType);
        funcEntity.setFuncParas(funcParas);
        funcEntity.setBuildIn(true);
        if (!scope.isGlobalScope())
            funcEntity.setInClass(true);
        scope.insertFunc(funcEntity);
    }

    private void buildInPrepare()
    {
        ClassEntity arrayEntity = new ClassEntity("__array", new ClassType("__array"), globalScope);
        Scope arrayScope = arrayEntity.getClassScope();
        globalScope.insertClass(arrayEntity);
        ClassEntity stringEntity = new ClassEntity("string", new ClassType("string"), globalScope);
        Scope stringScope = stringEntity.getClassScope();
        globalScope.insertClass(stringEntity);
        addBuildInFunc(arrayScope, "size", null, intType);
        addBuildInFunc(stringScope, "length",
                new ArrayList<VarEntity>(){{add(new VarEntity("this", stringEntity.getType()));}}, intType);
        addBuildInFunc(stringScope, "substring",
                new ArrayList<VarEntity>(){{add(new VarEntity("left", intType));
                add(new VarEntity("right", intType));add(new VarEntity("this", stringEntity.getType()));}}, stringType);
        addBuildInFunc(stringScope, "parseInt",
                new ArrayList<VarEntity>(){{add(new VarEntity("this", stringEntity.getType()));}}, intType);
        addBuildInFunc(stringScope, "ord",
                new ArrayList<VarEntity>(){{add(new VarEntity("pos", intType));
                add(new VarEntity("this", stringEntity.getType()));}}, intType);
        addBuildInFunc(globalScope, "print",
                new ArrayList<VarEntity>(){{add(new VarEntity("str", stringType));}}, voidType);
        addBuildInFunc(globalScope, "println",
                new ArrayList<VarEntity>(){{add(new VarEntity("str", stringType));}}, voidType);
        addBuildInFunc(globalScope, "getString", null, stringType);
        addBuildInFunc(globalScope, "getInt", null, intType);
        addBuildInFunc(globalScope, "toString",
                new ArrayList<VarEntity>(){{add(new VarEntity("i", intType));}}, stringType);
    }

    @Override
    public void visit(MxProgNode mxProgNode)
    {
        buildInPrepare();
        for (DeclNode declNode : mxProgNode.getDeclNodesList())
        {
            if (!(declNode instanceof VarDeclNode))
                declNode.accept(this);
        }
        entryCheck();
    }

    @Override
    public void visit(ClassDeclNode classDeclNode)
    {
        Entity entity = new ClassEntity(classDeclNode, globalScope);
        globalScope.insertClass(entity);
    }

    @Override
    public void visit(FuncDeclNode funcDeclNode)
    {
        Entity entity = new FuncEntity(funcDeclNode, globalScope);
        globalScope.insertFunc(entity);
    }
}
