package stp;
import java.util.*;
public class General{
  public void add_arg(String arg_type,String arg_name) throws Exception{};
  public void add_argtype(String n){};
  public void add_vardecl(String var_type,String var_name) throws Exception{};
  public MethodInfo add_method(String returnType,String method_name) throws Exception{ return null; };
  public void polymorphic_method(MethodInfo method) throws Exception{};
  public boolean var_declared(String var_name){ return false; }
  public String get_name(){ return null; }
  public boolean basic_type(String type){
    return ( type == "int" || type=="boolean" || type=="int[]");
  }
  public Map get_vars(){ return null ;}
  public ClassInfo get_class(){ return null; }
  public String type_of_var(String var_name){ return null ;}
  public MethodInfo get_method(String method_name){ return null; }
  public MethodInfo get_method(){ return null; }
}
