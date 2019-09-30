package stp;
import java.util.*;

public class ClassInfo extends General{
  private ClassInfo parent;
  private String name;
  private Map<String,String> vars;
  private Map<String,MethodInfo> methods;
  private Map<String,MethodInfo> all_methods;

  public ClassInfo(String nm){
    name = nm;
    parent = null;
    methods = new LinkedHashMap<String,MethodInfo>();
    vars = new LinkedHashMap<String,String>();
  }

  public ClassInfo(String nm,ClassInfo pr){
    name = nm;
    parent = pr;
    methods = new LinkedHashMap<String,MethodInfo>();
    vars = new LinkedHashMap<String,String>();
  }

  public ClassInfo get_parent(){
    return parent;
  }

  public String get_name(){
    return name;
  }

  public void add_vardecl(String var_type,String var_name) throws Exception{
    if(vars.containsKey(var_name)){
      throw new Exception("Double declaration of variable "+var_name+" in class "+this.get_name());
      //?? prepei na blepo an einai orismeno kai se goneiki klasi?
    }else{
      vars.put(var_name,var_type);
    }
  }

  public boolean method_exists(String method_name){
    return methods.containsKey(method_name);
  }

  public boolean method_exists_inherently(String method_name){
    if( methods.containsKey(method_name)){
      return true;
    }else if(parent!= null && parent.method_exists_inherently(method_name)){
      return true;
    }else{
      return false;
    }
  }

  public MethodInfo get_method_inherently(String method_name){
    if( methods.containsKey(method_name)){
      return methods.get(method_name);
    }else if(parent!= null){
      return parent.get_method_inherently(method_name);
    }else{
      return null;
    }
  }

  public MethodInfo add_method(String returnType,String method_name) throws Exception{
    if(methods.containsKey(method_name)){
      throw new Exception("Method '"+method_name+"' already declared");
    }
    MethodInfo method= new MethodInfo(returnType,method_name,this);
    methods.put(method.get_name(),method);
    return method;
  }

  public MethodInfo get_method(String method_name){
    return methods.get(method_name);
  }

  public void polymorphic_method(MethodInfo method) throws Exception{
    MethodInfo parent_method;
    if(parent!=null && parent.method_exists(method.get_name())){
      parent_method = parent.get_method(method.get_name());
      method.polymorphic_method(parent_method);
    }
  }

  public boolean var_declared(String var_name){
    if(vars.containsKey(var_name)){
      return true;
    }else if(parent!=null && parent.var_declared(var_name)){
      return true;
    }else{
      return false;
    }
  }

  public void undeclared_classes_check(SymbolTable stp) throws Exception{
    for(String var_type : vars.values()){
      if(!basic_type(var_type) && !stp.class_exists(var_type)){
        throw new Exception("Class '"+this.get_name()+"' contains a member of undeclared class '"+var_type+"'");
      }
    }
  }

  public Map get_vars(){
    return vars;
  }

  public String type_of_var(String var_name){
    if(vars.containsKey(var_name)){
      return vars.get(var_name);
    }else if(parent!=null){
      return parent.type_of_var(var_name);
    }else{
      return null;
    }
  }

  public String get_method_rt(String method_name){
    if(methods.containsKey(method_name)){
      return methods.get(method_name).get_returnType();
    }else if(parent!=null){
      return parent.get_method_rt(method_name);
    }else{
      return null;
    }
  }

  public void set_offsets(OffsetInfo offset){
    for(Map.Entry<String, String> entry : vars.entrySet()) {
      offset.add_var(entry.getKey(),entry.getValue());
    }
    for(Map.Entry<String, MethodInfo> entry : methods.entrySet()) {
      if(parent!=null && parent.method_exists_inherently(entry.getKey())){
        continue;
      }
      offset.add_method(entry.getKey());
    }
  }

  public int get_num_methods(){
    return methods.size();
  }
  public Map get_methods(){
    return methods;
  }
  public Map get_all_methods(){
    Map<String,MethodInfo> all_methods = new LinkedHashMap<String,MethodInfo>();
    Map<String,MethodInfo> mp;
    if(parent!=null){
      mp = parent.get_all_methods();
      all_methods.putAll(mp);
    }
    all_methods.putAll(methods);
    return all_methods;
  }

  public String method_exists_in(String method_name){
    if(methods.containsKey(method_name)){
      return name;
    }else if(parent!=null){
      return parent.method_exists_in(method_name);
    }
    return null;
  }

  public void create_Vtable(){
    String defs = "";
    int num_defs=0;
    all_methods = get_all_methods();
    for(Map.Entry<String,MethodInfo> entry : all_methods.entrySet()){
      if(num_defs!=0) defs+=" , ";
      String method_name = entry.getKey();
      MethodInfo method_ = entry.getValue();
      defs += "i8* bitcast( "+ method_.get_method_def()+"* @"+method_exists_in(method_name)+"."+method_name+" to i8*)";
      num_defs++;
    }
    System.out.println("@."+name+"_vtable = global ["+all_methods.size()+" x i8*] ["+defs+"]");
  }

  public void print_alloca_args(String method_name){
    /*prints the allocas and stores of a method's parameterList*/
    MethodInfo m = methods.get(method_name);
    m.print_alloca_args();
  }

  public int get_all_methods_size(){
    return all_methods.size();
  }

  public String var_exists_locally(String methodname,String var_name){
    MethodInfo method_ = methods.get(methodname);
    return method_.var_exists_locally(var_name);
  }

  public String get_var_type(String var_name){
    if(vars.containsKey(var_name)){
      return vars.get(var_name);
    }else if(parent!=null){
      return parent.get_var_type(var_name);
    }else{
      return null;
    }
  }

  public String get_var_type(String methodname,String var_name){
    MethodInfo method_ = methods.get(methodname);
    String var_type = method_.type_of_var(var_name);
    return var_type;
  }

  public String[] get_function_details(String function_name){
    MethodInfo method_ = methods.get(function_name);
    String[] rt = method_.get_function_details();
    int n=0;
    String methodname;
    if(rt==null && parent!=null){
      return parent.get_function_details(function_name);
    }else{
      for(Map.Entry<String,MethodInfo> entry : methods.entrySet()){
        methodname = entry.getKey();
        if(methodname==function_name){
          rt[2] = String.valueOf(n);
          return rt;
        }
        n++;
      }
      return rt;
    }
  }

}
