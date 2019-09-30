package stp;
import java.util.*;

public class ArgType extends General{
  MethodInfo method;
  String arg_types;

  public ArgType(MethodInfo m,String n){
    method = m;
    arg_types = n;
  }
  public MethodInfo get_method(){
    return method;
  }

  public void add_argtype(String n){
    arg_types += "," + n;
  }

  public String get_args(){
    return arg_types;
  }

}
