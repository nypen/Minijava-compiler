import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import stp.*;
/**Symbol table stores variable names , and their type**/


public class STPvisitor extends GJDepthFirst<String,General>{
  private SymbolTable stp;

  public STPvisitor(){
    stp = new SymbolTable();
  }
  public SymbolTable get_stp(){
    return stp;
  }
  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public String visit(Goal n,General argu) throws Exception {
     n.f0.accept(this, argu);
     n.f1.accept(this, argu);
     n.f2.accept(this, argu);
     stp.undeclared_classes_check();
     return null;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  public String visit(MainClass n,General argu) throws Exception{
     String classname = n.f1.accept(this,null);
     ClassInfo newclass = new ClassInfo(classname);
     stp.add_class(newclass);
     MethodInfo method = newclass.add_method("void","main");
    //add main as method
     String arg_name = n.f11.accept(this, argu);
     method.add_arg("String[]",arg_name);
     n.f14.accept(this, method);
     n.f15.accept(this, method);
     return null;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public String visit(ClassDeclaration n,General argu) throws Exception{
     String classname = n.f1.accept(this,null);
     ClassInfo newclass = new ClassInfo(classname);
     stp.add_class(newclass);
     if(n.f3.present())
          n.f3.accept(this,newclass);
     if(n.f4.present())
        n.f4.accept(this,newclass);
     return null;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  public String visit(ClassExtendsDeclaration n,General argu) throws Exception{
     String classname = n.f1.accept(this,null);
     String parentname = n.f3.accept(this,null);

     if(!stp.class_exists(parentname)){
       throw new Exception("Class '"+classname+ "'extends to undeclared class '"+parentname+"'.");
     }
     ClassInfo parent = stp.get(parentname);
     ClassInfo newclass = new ClassInfo(classname,parent);
     stp.add_class(newclass);
     n.f5.accept(this, newclass);
     n.f6.accept(this, newclass);
     return null;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  public String visit(VarDeclaration n, General argu) throws Exception{
     String var_type = n.f0.accept(this, argu);
     String var_name = n.f1.accept(this, argu);
     argu.add_vardecl(var_type,var_name);
     return null;
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  public String visit(MethodDeclaration n,General argu) throws Exception{
     String returnType = n.f1.accept(this, argu);
     String method_name = n.f2.accept(this, argu);
     MethodInfo method = argu.add_method(returnType,method_name);
     if(n.f4.present()){
       n.f4.accept(this, method);
     }
     if(n.f7.present()){
       n.f7.accept(this, method);
     }
     if(n.f8.present()){
       n.f8.accept(this, method);
     }
     argu.polymorphic_method(method);
     return null;
  }
  /**
   * f0 -> Type()
   * f1 -> Identifier()
   */
  public String visit(FormalParameter n,General argu) throws Exception{
     String arg_type = n.f0.accept(this, argu);
     String arg_name = n.f1.accept(this, argu);
     argu.add_arg(arg_type,arg_name);
     return null;
  }

  public String visit(Identifier n,General argu) throws Exception {
   return n.f0.toString();
  }

  /**
   * f0 -> "int"
   * f1 -> "["
   * f2 -> "]"
   */
  public String visit(ArrayType n,General argu) throws Exception {
     return "int[]";
  }

  /**
   * f0 -> "boolean"
   */
  public String visit(BooleanType n,General argu) throws Exception {
     return "boolean";
  }

  /**
   * f0 -> "int"
   */
  public String visit(IntegerType n,General argu) throws Exception {
    return "int";
  }


}
