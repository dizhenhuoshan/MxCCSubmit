package princeYang.mxcc.scope;

import princeYang.mxcc.ast.ClassDeclNode;
import princeYang.mxcc.ast.ClassType;
import princeYang.mxcc.ast.FuncDeclNode;
import princeYang.mxcc.ast.Type;

public class ClassEntity extends Entity
{
    private Scope classScope;
    private int memSize = 0;

    public ClassEntity(String ident, Type type, Scope father)
    {
        super(ident, type);
        classScope = new Scope(father);
    }

    public ClassEntity(ClassDeclNode classDeclNode, Scope father)
    {
        super(classDeclNode.getIdentName(), new ClassType(classDeclNode.getIdentName()));
        classScope = new Scope(father);
        for (FuncDeclNode funcDeclNode : classDeclNode.getFuncDeclList())
        {
            FuncEntity funcEntity = new FuncEntity(classDeclNode.getIdentName(), funcDeclNode, classScope);
            classScope.insertFunc(funcEntity);
        }
    }

    public Scope getClassScope()
    {
        return classScope;
    }

    public int getMemSize()
    {
        return memSize;
    }

    public void setMemSize(int memSize)
    {
        this.memSize = memSize;
    }
}
