package stp;
import java.util.*;

public class SymbolTable extends General{
  private Map<String,ClassInfo> stp;
  private Map<String,OffsetInfo> offsets;



  public void set_offsets(){
    OffsetInfo new_offset;
    for(ClassInfo class_ : stp.values()){
      ClassInfo parent = class_.get_parent();
      if(parent!=null){
        new_offset = new OffsetInfo(offsets.get(parent.get_name()));
      }else{
        new_offset = new OffsetInfo();
      }
      class_.set_offsets(new_offset);
      offsets.put(class_.get_name(),new_offset);
    }
  }
  public void create_Vtables(){
    String class_name;
    ClassInfo class_;
    int num_methods ;
    String method_defs ;
    for(Map.Entry<String,ClassInfo> entry : stp.entrySet()){
      class_name =  entry.getKey();
      class_ = entry.getValue();
      if(class_.method_exists("main")){
        System.out.println("@."+class_name+"_vtable = global [0 x i8*] []");
        continue;
      }
      class_.create_Vtable();
    }
  }

  public SymbolTable(){
    stp = new LinkedHashMap<String,ClassInfo>();
    offsets = new LinkedHashMap<String,OffsetInfo>();
  }

  public Map get_classes(){
    return stp;
  }
  public void undeclared_classes_check() throws Exception{
    for(Map.Entry<String,ClassInfo> entry : stp.entrySet()){
      String class_name =  entry.getKey();
      ClassInfo class_ = entry.getValue();
      class_.undeclared_classes_check(this);
    }
  }
  public boolean subtype_of(String superclass,String subclass){
    ClassInfo class_ = stp.get(subclass);
    if(class_!=null){
      if(class_.get_parent().get_name()==superclass){
        return true;
      }else{
        return subtype_of(superclass,class_.get_parent().get_name());
      }
    }
    return false;
  }

  public void add_class(ClassInfo new_class,int i) throws Exception{
    if(stp.containsKey(new_class.get_name())){
      throw new Exception("Class '"+new_class.get_name()+ "' already declared.");
    }else{
      stp.put(new_class.get_name(),new_class);
    }
  }
  public void add_class(ClassInfo new_class) throws Exception{
    if(stp.containsKey(new_class.get_name())){
      throw new Exception("Class '"+new_class.get_name()+ "' already declared.");
    }else{
      stp.put(new_class.get_name(),new_class);
    }
  }

  public boolean class_exists(String class_name){
    if(stp.containsKey(class_name)){
      return true;
    }else{
      return false;
    }
  }
  public ClassInfo get(String key){
    return stp.get(key);
  }
  public void print_alloca_args(String class_name,String method_name){
    ClassInfo cl = stp.get(class_name);
    cl.print_alloca_args(method_name);
  }

  public int get_object_size(String object_name){
    OffsetInfo obj_offset = offsets.get(object_name);

    return obj_offset.get_object_size(object_name);
  }

  public int get_all_methods_size(String classname){
    ClassInfo class_ = stp.get(classname);
    return class_.get_all_methods_size();
  }
  public String var_exists_locally(String classname,String methodname,String var_name){
    /*if variable with name var_name exists locally its type is returned , else null*/
    ClassInfo class_ = stp.get(classname);
    return class_.var_exists_locally(methodname,var_name);
  }

  public int get_var_offset(String classname,String var_name) {
    OffsetInfo offset = offsets.get(classname);
    int offset_num = offset.get_var_offset(var_name);
    if(offset_num==-1){
      ClassInfo class_ = stp.get(classname);
      return get_var_offset(class_.get_parent().get_name(),var_name);
    }
    return offset_num;
  }

  public String get_var_type(String classname,String var_name){
    ClassInfo class_ = stp.get(classname);
    return class_.get_var_type(var_name);
  }

  public String get_var_type(String classname,String methodname,String var_name){
    ClassInfo class_ = stp.get(classname);
    return class_.get_var_type(methodname,var_name);
  }

  public String[] get_function_details(String obj_called,String function_name){
    ClassInfo class_ = stp.get(obj_called);
    return class_.get_function_details(function_name);
  }
  public String get_returnType(String classname,String methodname){
    ClassInfo class_ = stp.get(classname);
    return class_.get_method_rt(methodname);
  }


}
