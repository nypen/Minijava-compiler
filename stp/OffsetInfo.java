package stp;
import java.util.*;
public class OffsetInfo{

    Map<String,Integer> var_offsets;
    Map<String,Integer> method_offsets;
    int var_total_offset;
    int method_total_offset;

    public OffsetInfo(){
      var_total_offset = 0;
      method_total_offset = 0;
      var_offsets = new LinkedHashMap<String,Integer>();
      method_offsets = new LinkedHashMap<String,Integer>();
    }

    public OffsetInfo(OffsetInfo parent){
      var_total_offset = parent.get_var_total_offset();
      method_total_offset = parent.get_method_total_offset();
      var_offsets = new LinkedHashMap<String,Integer>();
      method_offsets = new LinkedHashMap<String,Integer>();
    }

    public void add_var(String var_name,String var_type){
      var_offsets.put(var_name,var_total_offset);
      if(var_type == "boolean")
        var_total_offset +=1;
      else if(var_type== "int")
        var_total_offset +=4;
      else
        var_total_offset +=8;
    }

    public void add_method(String method_name){
      method_offsets.put(method_name,method_total_offset);
      method_total_offset += 8;
    }

    public int get_var_total_offset(){
      return var_total_offset;
    }

    public int get_method_total_offset(){
      return method_total_offset;
    }

    public Map get_method_offsets(){
      return method_offsets;
    }

    public Map get_var_offsets(){
      return var_offsets;
    }

    public void print_offsets(String cn){
      for(Map.Entry<String, Integer> entry2 : var_offsets.entrySet()) {
        System.out.println(cn+"."+entry2.getKey()+" = "+entry2.getValue());
      }
      for(Map.Entry<String, Integer> entry2 : method_offsets.entrySet()) {
        System.out.println(cn+"."+entry2.getKey()+" = "+entry2.getValue());
      }

    }
    public int get_object_size(String object_name){
      return var_total_offset;
    }
    public int get_var_offset(String var_name){
      if(var_offsets.containsKey(var_name)){
        return var_offsets.get(var_name);
      }else{
        return -1;
      }
    }
}
