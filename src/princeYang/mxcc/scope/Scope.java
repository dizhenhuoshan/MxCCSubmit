package princeYang.mxcc.scope;

import princeYang.mxcc.errors.MxError;

import java.util.HashMap;

public class Scope
{
    private HashMap<String, Entity> entityMap = new HashMap<>();
    private Scope father;
    private boolean isGlobalScope;
    static private String varPrefix = "__var__";
    static private String funcPrefix = "__func__";
    static private String classPrefix = "__class__";

    public Scope()
    {
        father = null;
        isGlobalScope = true;
    }

    public Scope(Scope father)
    {
        this.father = father;
        isGlobalScope = false;
    }

    public void insertVar(Entity entity)
    {
        if (!(entity instanceof VarEntity))
            throw new MxError(String.format("Scope: inserVar entry type ERROR!"));
        String key = varPrefix + entity.getIdent();
        if (checkID(entity.getIdent()))
            entityMap.put(key, entity);
        else throw new MxError(String.format("Scope: identifier: %s already exists!\n", entity.getIdent()));
    }

    public void insertFunc(Entity entity)
    {
        if (!(entity instanceof FuncEntity))
            throw new MxError(String.format("Scope: inserFunc entry type ERROR!"));
        String key = funcPrefix + entity.getIdent();
        if (checkID(entity.getIdent()))
            entityMap.put(key, entity);
        else throw new MxError(String.format("Scope: identifier: %s already exists!\n", entity.getIdent()));
    }

    public void insertClass(Entity entity)
    {
        if (!(entity instanceof ClassEntity))
            throw new MxError(String.format("Scope: inserClass entry type ERROR!"));
        String key = classPrefix + entity.getIdent();
        if (checkID(entity.getIdent()))
            entityMap.put(key, entity);
        else throw new MxError(String.format("Scope: identifier: %s already exists!\n", entity.getIdent()));
    }

    public VarEntity getVar(String ident)
    {
        String key = varPrefix + ident;
        VarEntity entity = (VarEntity) entityMap.get(key);
        if (entity != null || isGlobalScope)
            return entity;
        else return father.getVar(ident);
    }

    public FuncEntity getFunc(String ident)
    {
        String key = funcPrefix + ident;
        FuncEntity entity = (FuncEntity) entityMap.get(key);
        if (entity != null || isGlobalScope)
            return entity;
        else return father.getFunc(ident);
    }

    public ClassEntity getClass(String ident)
    {
        String key = classPrefix + ident;
        ClassEntity entity = (ClassEntity) entityMap.get(key);
        if (entity != null || isGlobalScope)
            return entity;
        else return father.getClass(ident);
    }

    public Entity getVarOrFunc(String ident)
    {
        String varKey, funcKey;
        varKey = varPrefix + ident;
        funcKey = funcPrefix + ident;
        Entity varEntity = entityMap.get(varKey);
        Entity funcEntity = entityMap.get(funcKey);
        if (varEntity == null && funcEntity == null && !isGlobalScope)
            return father.getVarOrFunc(ident);
        else if (varEntity != null)
            return varEntity;
        else
            return funcEntity;
    }

    public boolean checkID(String ident)
    {
        return !(entityMap.containsKey(varPrefix + ident)
                || entityMap.containsKey(funcPrefix + ident)
                || entityMap.containsKey(classPrefix + ident));
    }


    public VarEntity getSelfVar(String ident)
    {
        return (VarEntity) entityMap.get(varPrefix + ident);
    }

    public FuncEntity getSelfFunc(String ident)
    {
        return (FuncEntity) entityMap.get(funcPrefix + ident);
    }

    public ClassEntity getSelfClass(String ident)
    {
        return (ClassEntity) entityMap.get(classPrefix + ident);
    }

    public Entity getSelfVarOrFunc(String ident)
    {
        String varKey = varPrefix + ident;
        String funcKey = funcPrefix + ident;
        Entity varEntity = entityMap.get(varKey);
        Entity funcEntity = entityMap.get(funcKey);
        if (varEntity != null)
            return varEntity;
        else
            return funcEntity;
    }

    public Scope getFather()
    {
        return father;
    }

    public boolean isGlobalScope()
    {
        return isGlobalScope;
    }
}
