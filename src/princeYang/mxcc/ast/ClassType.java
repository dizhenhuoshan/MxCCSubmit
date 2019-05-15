package princeYang.mxcc.ast;

public class ClassType extends Type
{
    String classIdent;

    public ClassType(String classIdent)
    {
        this.baseType = BaseType.STYPE_CLASS;
        this.classIdent = classIdent;
    }

    public String getClassIdent()
    {
        return classIdent;
    }

    @Override
    public boolean equals(Object obj)
    {
        return (obj.getClass() == ClassType.class) &&
                (((ClassType) obj).classIdent.equals(classIdent));
    }

    @Override
    public String toString()
    {
        return String.format("ClassType(%s)", classIdent);
    }
}
