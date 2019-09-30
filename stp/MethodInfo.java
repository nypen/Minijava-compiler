package stp;
import java.util.*;

public class MethodInfo extends General{
  private String returnType;
  private String name;
  private int numArgs;
  ClassInfo my_class;
  private Map<String,String> args;
  private Map<String,String> vars;

  public MethodInfo(String rt,String nm,ClassInfo mc){
    returnType = rt;
    name = nm;
    my_class = mc;
    numArgs = 0;
    args = new LinkedHashMap<String,String>();
    vars = new LinkedHashMap<String,String>();
  }

  public String ll_type(String type){
    if(type.equals("i32") || type.equals("i1") || type.equals("i1")  || type.equals("i8") || type.equals("i8*") || type.equals("i32*") ){
        return type;
    }
    if(type=="int"){
        return "i32";
    }else if(type=="boolean"){
        return "i1";
    }else if(type=="int[]"){
        return "i32*";
    }else{
        return "i8*";
    }
  }
  public String get_method_def(){
    String rt = ll_type(returnType)+" (i8*";
    String type;
    for(Map.Entry<String,String> entry : args.entrySet()){
      type = entry.getValue();
      rt+= ",";
      rt+=ll_type(type);
    }
    rt+=")";
    return rt;
  }

  public MethodInfo get_method(){
    return this;
  }

  public String get_name(){
    return name;
  }
  public String get_returnType(){
    return returnType;
  }
  public int get_numArgs(){
    return numArgs;
  }
  public Map get_args(){
    return args;
  }
  public Map get_vars(){
    return vars;
  }

  public void add_vardecl(String var_type,String var_name) throws Exception{
    if(vars.containsKey(var_name))
      throw new Exception("variable " + var_name +" already declared");
    else{
      vars.put(var_name,var_type);
    }
  }

  public void polymorphic_method(MethodInfo method) throws Exception{
    if(returnType!=method.get_returnType() || !args.equals(method.get_args())){
      throw new Exception("Method '"+method.get_name()+"' is not inherently polymorphic.");
    }
  }

  public boolean var_declared(String var_name){
    if(vars.containsKey(var_name)){
      return true;
    }else if(args.containsKey(var_name)){
      return true;
    }else if(my_class.var_declared(var_name)){
      return true;
    }else{
      return false;
    }
  }

  public void add_arg(String arg_type,String arg_name) throws Exception{
    if(args.containsKey(arg_name)){
      throw new Exception("Another argument of method '"+this.get_name()+ "' has the name "+ arg_name +".");
    }else{
      args.put(arg_name,arg_type);
      numArgs += 1;
    }
  }

  public ClassInfo get_class(){
    return my_class;
  }

  public String type_of_var(String var_name){
    if(vars.containsKey(var_name)){
      return vars.get(var_name);
    }else if(args.containsKey(var_name)){
      return args.get(var_name);
    }else{
      return my_class.type_of_var(var_name);
    }
  }

  public boolean arguments_match(String[] args2){
    int count = 0;
    for(String var_type : args.values()){
      if(args2[count]!=var_type){
        return false;
      }
      count +=1;
    }
    return true;
  }

  public void print_alloca_args(){
    /*prints the allocas and stores of a method's parameterList*/
    for(Map.Entry<String,String> entry : args.entrySet()){
      String var_name =  entry.getKey();
      String var_type = entry.getValue();
      System.out.println("\t%"+var_name+" = alloca "+ll_type(var_type));
      System.out.println("\tstore "+ll_type(var_type) +" %."+var_name+", "+ll_type(var_type)+"* %"+var_name);
    }
  }

  public String var_exists_locally(String var_name){
    if(vars.containsKey(var_name)){
      return vars.get(var_name);
    }else if(args.containsKey(var_name)){
      return args.get(var_name);
    }else{
      return null;
    }

  }
  public String[] get_function_details(){
    String[] details = new String[3];
    details[0] = returnType;
    details[1] = get_method_def()+"*";
    return details;
  }
}
