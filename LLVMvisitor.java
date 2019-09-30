import java.io.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import stp.*;

public class LLVMvisitor extends GJDepthFirst<String,String[]>{
  private SymbolTable stp;
  private int var_counter;
  private int label_counter;
  private int if_label_counter;
  private int and_label_counter;
  private int while_label_counter;
  private String args ;
  public LLVMvisitor(SymbolTable in_stp,String file_name) throws Exception{
    stp = in_stp;
    args = "";
    var_counter = 0;
    and_label_counter = 0;
    label_counter = 0;
    while_label_counter = 0;
    if_label_counter = 0;
    PrintStream file = new PrintStream(new FileOutputStream(file_name));
    System.setOut(file);
    stp.create_Vtables();
    System.out.println("declare i8* @calloc(i32, i32)\n"+
                      "declare i32 @printf(i8*, ...)\n"+
                      "declare void @exit(i32)\n"+
                      "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n"+
                      "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n"+
                      "define void @print_int(i32 %i) {\n"+
                          "\t%_str = bitcast [4 x i8]* @_cint to i8*\n"+
                          "\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n"+
                          "\tret void\n"+
                      "}\n"+
                      "define void @throw_oob() {\n"+
                          "\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n"+
                          "\tcall i32 (i8*, ...) @printf(i8* %_str)\n"+
                          "\tcall void @exit(i32 1)\n"+
                          "\tret void\n"+
                      "}\n");

  }

  public String next_local_var(){
    String var = "%_"+var_counter;
    var_counter++;
    return var;
  }
  public String next_if_label(){
    String label = "if"+if_label_counter;
    if_label_counter++;
    return label;
  }
  public String next_while_label(){
    String label = "while"+while_label_counter;
    while_label_counter++;
    return label;
  }

  public String next_oob_label(){
    String label = "oob"+label_counter;
    label_counter++;
    return label;
  }

  public String next_and_label(){
    String label = "and"+label_counter;
    and_label_counter++;
    return label;
  }

  public boolean is_int(String s){
    return(s.charAt(0)>='0' && s.charAt(0)<='9');
  }

  public String ll_type(String type){

    if(type.equals("i32") || type.equals("i1") || type.equals("i1*")  || type.equals("i8") || type.equals("i8*") || type.equals("i32*") ){
        return type;
    }
    if(type.equals("int")){
        return "i32";
    }else if(type.equals("boolean")){
        return "i1";
    }else if(type.equals("int[]")){
        return "i32*";
    }else{
        return "i8*";
    }
  }

  public String load_variable(String classname,String methodname,String var_name,boolean return_type){
    System.out.println(";loading with true :"+return_type);
    if(var_name.contains("this")){
      return "i8* "+var_name;
    }else if(var_name.charAt(0)=='%'){
      return var_name;
    }else if(var_name.contains(" ")){
      String[] tmp = var_name.split(" ");
      return ll_type(tmp[0])+" "+tmp[1];
    }else if(is_int(var_name)){
      return "i32 "+var_name;
    }else if(var_name.equals("true")){
      return "i1 1";
    }else if(var_name.equals("false")){
      return "i1 0";
    }else{
      String var_type = stp.var_exists_locally(classname,methodname,var_name);
      if(var_type!=null){
        String var0 = next_local_var();
        System.out.println("\t"+var0+" = load "+ll_type(var_type)+", "+ll_type(var_type)+"* %"+var_name);
        return ll_type(var_type)+" "+var0;
      }else{
        int offset = stp.get_var_offset(classname,var_name) + 8;
        var_type = stp.get_var_type(classname,var_name);
        String var1 = next_local_var();
        String var2 = next_local_var();
        String var3 = next_local_var();
        System.out.println("\t"+var1+" = getelementptr i8 , i8* %this , i32 "+offset);
        System.out.println("\t"+var2+" = bitcast i8* "+var1+" to "+ll_type(var_type)+"*");
        System.out.println("\t"+var3+" = load "+ll_type(var_type)+" , "+ll_type(var_type)+"* "+var2);
        return ll_type(var_type)+" "+var3;
      }
    }
  }

  public String load_variable(String classname,String methodname,String var_name){
    if(var_name.contains("this")){
      return var_name;
    }else if(var_name.contains(" ")){
      return var_name;
    }else if(is_int(var_name)){
      return var_name;
    }else if(var_name.equals("true")){
      return "1";
    }else if(var_name.equals("false")){
      return "0";
    }else if(var_name.charAt(0)=='%'){
      return var_name;
    }else{
      String var_type = stp.var_exists_locally(classname,methodname,var_name);
      if(var_type!=null){
        String var0 = next_local_var();
        System.out.println("\t"+var0+" = load "+ll_type(var_type)+", "+ll_type(var_type)+"* %"+var_name);
        return var0;
      }else{
        int offset = stp.get_var_offset(classname,var_name) + 8;
        var_type = stp.get_var_type(classname,var_name);
        System.out.println(";loading "+var_name+" , return with type "+var_type);
        String var1 = next_local_var();
        String var2 = next_local_var();
        String var3 = next_local_var();
        System.out.println("\t"+var1+" = getelementptr i8 , i8* %this , i32 "+offset);
        System.out.println("\t"+var2+" = bitcast i8* "+var1+" to "+ll_type(var_type)+"*");
        System.out.println("\t"+var3+" = load "+ll_type(var_type)+" , "+ll_type(var_type)+"* "+var2);
        return var3;
      }
    }
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

  public String visit(MainClass n,String[] argu) throws Exception {
    String[] n_argu = {"","main"};
    String classname = n.f1.accept(this,n_argu);
    n_argu[0] = classname;
    System.out.println("define i32 @main() {\n");
    n.f14.accept(this, n_argu);
    n.f15.accept(this, n_argu);
    System.out.println("ret i32 0");
    System.out.println("}\n");
    return "";
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public String visit(ClassDeclaration n,String[] argu) throws Exception {
    String[] a ={"a"};
    String classname = n.f1.accept(this,a);
    String[] n_argu = {classname};
    n.f4.accept(this, n_argu);
    return "";
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
  public String visit(ClassExtendsDeclaration n,String[] argu) throws Exception {
    String[] a ={"a"};
    String classname = n.f1.accept(this,a);
    String[] n_argu = {classname};
    n.f6.accept(this, n_argu);
    return "";
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
  public String visit(MethodDeclaration n, String[] argu) throws Exception {
    var_counter = 0;
    String type = n.f1.accept(this, argu);
    String id = n.f2.accept(this, argu);
    String[] n_argu={argu[0],id};
    String formal_parameters = n.f4.accept(this,n_argu);
    String rt;
    if(formal_parameters!=null)
      rt = "define "+ type +" @"+ argu[0]+"."+ id+"(i8* %this, "+formal_parameters+"){";
    else
      rt = "define "+ type +" @"+ argu[0]+"."+ id+"(i8* %this){\n";
    System.out.println(rt);
    stp.print_alloca_args(argu[0],id);
    n.f7.accept(this, n_argu);
    n.f8.accept(this, n_argu);
    String return_expr = n.f10.accept(this, n_argu);
    System.out.println("; return = "+return_expr);
    if(return_expr.contains(" ")){
      System.out.println("; return contains space");
      System.out.println("\tret "+return_expr);
    }else{
      System.out.println("; return NOT contains space");
      String load = load_variable(argu[0],id,return_expr,true);
      System.out.println("\tret "+load);
    }
    System.out.println("\n}");
    return "";
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( ExpressionList() )?
   * f5 -> ")"
   */
  public String visit(MessageSend n,String[] argu) throws Exception {
    if(argu==null){
      System.out.println("\n;msgsend was called with null");
      return "msg";
    }
    String obj_called = n.f0.accept(this, argu);
    String class_of_object;
    String[] tmp;
    class_of_object = n.f0.accept(this,null);

    if(class_of_object==null){
      class_of_object = stp.get_var_type(argu[0],argu[1],obj_called);
      obj_called = load_variable(argu[0],argu[1],obj_called);
    }else if(class_of_object.equals("%this")){
      /*identifier is returned , ill get the class of object from SymbolTable*/
      class_of_object = argu[0];
    }else if(class_of_object.equals("msg")){
      if(obj_called.contains(" ")){
        tmp = obj_called.split(" ");
        class_of_object = tmp[0];
      }
    }

    System.out.println("\n;obj caleed = "+obj_called+".");
    String function_name = n.f2.accept(this, argu);
    System.out.println("\n;function_name = "+function_name+".");
    String prmtr_list = n.f4.accept(this, argu);
    System.out.println("\n;prmtr = "+prmtr_list+".");

    System.out.println("\n;"+obj_called+"."+function_name+"("+prmtr_list+")\n");
     String bitcast_var = next_local_var();
     String bitcast_var2 = next_local_var();
     String load_var = next_local_var();
     String load_var2 = next_local_var();
     String load_var3 = next_local_var();
     String call_var = next_local_var();
     String getelement_var = next_local_var();
     if(obj_called.contains(" ")){
       tmp = obj_called.split(" ");
       obj_called = tmp[1];
     }
     System.out.println("\t"+bitcast_var+" = bitcast i8* "+obj_called+" to i8***");
     System.out.println("\t"+load_var+" = load i8** , i8*** "+bitcast_var);
     String[] function_details = stp.get_function_details(class_of_object,function_name);
     String returnType = function_details[0];
     String prototype = function_details[1];
     String offset = function_details[2];
     System.out.println("\t"+getelement_var+" = getelementptr i8* , i8** "+load_var+" , i32 "+offset);
     System.out.println("\t"+load_var2+" = load i8* , i8** "+getelement_var);
     System.out.println("\t"+bitcast_var2+" = bitcast i8* "+load_var2+" to "+prototype);
     if(prmtr_list==null){
       System.out.println("\t"+call_var+" = call "+ll_type(returnType)+" "+bitcast_var2+"(i8* "+obj_called+")");
     }else{
       System.out.println("\t"+call_var+" = call "+ll_type(returnType)+" "+bitcast_var2+"(i8* "+obj_called+","+prmtr_list+")");
     }
     return returnType+" "+call_var;
  }

  /**
   * f0 -> Expression()
   * f1 -> ExpressionTail()
  */
  public String visit(ExpressionList n, String[] argu) throws Exception {
    String[] n_argu = {argu[0],argu[1],""};
    String expr = n.f0.accept(this, argu);
     n_argu[2] += load_variable(argu[0],argu[1],expr,true);
     n.f1.accept(this, n_argu);
     return n_argu[2];
  }

  /**
   * f0 -> ","
   * f1 -> Expression()
   */
  public String visit(ExpressionTerm n, String[] argu) throws Exception {
    String expr = n.f1.accept(this, argu);
    argu[2] += ","+load_variable(argu[0],argu[1],expr,true);
    return "";
  }

  /**
   * f0 -> "!"
   * f1 -> Clause()
   */
  public String visit(NotExpression n,String[] argu) throws Exception {
    String expr = n.f1.accept(this,argu);
    String xor_var = next_local_var();
    expr = load_variable(argu[0],argu[1],expr);
    if(expr.contains(" ")){
      String[] tmp = expr.split(" ");
      expr = tmp[1];
    }
    System.out.println("\t"+xor_var+" = xor i1 1 , "+expr);
    return "i1 "+xor_var;
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  public String visit(PrintStatement n,String[] argu) throws Exception {
     String print_var = n.f2.accept(this, argu);
     System.out.println("; print : "+print_var);
     String type = "";
     String[] tmp;
     print_var = load_variable(argu[0],argu[1],print_var,true);
     if(print_var.contains(" ")){
       tmp = print_var.split(" ");
       type = tmp[0];
     }else if(!is_int(print_var)){
       type = stp.get_var_type(argu[0],argu[1],print_var);
     }
     String var;
     if(type.equals("boolean") || type.equals("i1") ){
       if(print_var.contains(" ")){
         tmp = print_var.split(" ");
         print_var = tmp[1];
       }
       var = next_local_var();
       System.out.println("\t"+var+" = zext i1 "+ print_var+" to i32");
       print_var = "i32 "+var;
     }else{
       tmp = print_var.split(" ");
       System.out.println("; "+tmp[0]+" "+tmp[1]);
       print_var = ll_type(tmp[0]) + " " +tmp[1];
     }
     System.out.println("\tcall void (i32) @print_int("+print_var+")");
     return "";
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  public String visit(AssignmentStatement n, String[] argu) throws Exception {
     String id = n.f0.accept(this, argu);
     String var_expr = n.f2.accept(this, argu);
     String var_type = stp.var_exists_locally(argu[0],argu[1],id);
     String var_expr2 =load_variable(argu[0],argu[1],var_expr,true);
     if(var_type!=null){
       System.out.println(";AssignmentStatement var_expr ="+var_expr2);
       System.out.println("\tstore "+var_expr2+", "+ll_type(var_type)+"* %"+id);
     }else{
       int offset = stp.get_var_offset(argu[0],id) + 8;
       var_type = stp.get_var_type(argu[0],id);
       String var1 = next_local_var();
       String var2 = next_local_var();
       System.out.println("\t"+var1+" = getelementptr i8 , i8* %this , i32 "+offset);
       System.out.println("\t"+var2+" = bitcast i8* "+var1+" to "+ll_type(var_type)+"*");
       System.out.println("\tstore "+var_expr2+" , "+ll_type(var_type)+"* "+var2);
     }
     return "";
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   * f6 -> ";"
   */
  public String visit(ArrayAssignmentStatement n,String[] argu) throws Exception {
    String array_name = n.f0.accept(this, argu);
    String index = n.f2.accept(this, argu);
    String value = n.f5.accept(this, argu);
    String array_name_var = load_variable(argu[0],argu[1],array_name);
    index = load_variable(argu[0],argu[1],index);
    value = load_variable(argu[0],argu[1],value,true);
    String array_length = next_local_var();
    System.out.println("\t"+array_length+" = load i32 , i32* "+array_name_var);
    String compare_var = next_local_var();
    String compare_var2 = next_local_var();
    String new_index = next_local_var();
    String oob_label1 = next_oob_label();
    String oob_label2 = next_oob_label();
    /*if array length < index*/
    if(index.contains(" ")){
      String[] tmp = index.split(" ");
      index = tmp[1];
    }
    System.out.println("\t"+compare_var+" = icmp ult i32 "+array_length+" , "+index);
    System.out.println("\tbr i1 "+compare_var+" , label %"+oob_label1+" , label %"+oob_label2);
    System.out.println(oob_label1+":");
    /*throw oob*/
    System.out.println("\tcall void @throw_oob()");
    System.out.println("\tbr label %"+oob_label2);
    System.out.println(oob_label2+":");
    /*else , increase index by one and store the value*/
    System.out.println("\t"+new_index+" = add i32 "+index+" , 1");
    String array_ptr = next_local_var();
    System.out.println("\t"+array_ptr+" = getelementptr i32, i32* "+array_name_var+" , i32 "+new_index);
    System.out.println("\tstore "+value+" , i32 *"+array_ptr);
    return "";
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public String visit(CompareExpression n, String[] argu) throws Exception {
    String a2,b2;
    String a1 = n.f0.accept(this, argu);
    String b1 = n.f2.accept(this, argu);
    String rt;
    a1 = load_variable(argu[0],argu[1],a1);
    b1 = load_variable(argu[0],argu[1],b1);
    rt = next_local_var();
    if(a1.contains(" ")){
      String[] tmp = a1.split(" ");
      a1 = tmp[1];
    }
    if(b1.contains(" ")){
      String[] tmp = b1.split(" ");
      b1 = tmp[1];
    }
    System.out.println("\t"+rt+" = icmp slt i32 "+a1+"," +b1);
    return "i1 "+rt;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public String visit(PlusExpression n, String[] argu) throws Exception {
    String a2,b2;
    String a1 = n.f0.accept(this, argu);
    String b1 = n.f2.accept(this, argu);
    String rt;
    a1 = load_variable(argu[0],argu[1],a1);
    b1 = load_variable(argu[0],argu[1],b1);
    rt = next_local_var();
    if(a1.contains(" ")){
      String[] tmp = a1.split(" ");
      a1 = tmp[1];
    }
    if(b1.contains(" ")){
      String[] tmp = b1.split(" ");
      b1 = tmp[1];
    }
    System.out.println("\t"+rt+" = add i32 "+a1+"," +b1);
    return "i32 "+rt;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public String visit(MinusExpression n,String[] argu) throws Exception {
    String a2,b2;
    String a1 = n.f0.accept(this, argu);
    String b1 = n.f2.accept(this, argu);
    String rt;
    a1 = load_variable(argu[0],argu[1],a1);
    b1 = load_variable(argu[0],argu[1],b1);
    rt = next_local_var();
    if(a1.contains(" ")){
      String[] tmp = a1.split(" ");
      a1 = tmp[1];
    }
    if(b1.contains(" ")){
      String[] tmp = b1.split(" ");
      b1 = tmp[1];
    }
    System.out.println("\t"+rt+" = sub i32 "+a1+"," +b1);

    return "i32 "+rt;
  }

  /**
   * f0 -> Clause()
   * f1 -> "&&"
   * f2 -> Clause()
   */
  public String visit(AndExpression n, String[] argu) throws Exception {
     String[] tmp;
     String clause1 = n.f0.accept(this, argu);
     String label1 = next_and_label();
     String label2 = next_and_label();
     String label3 = next_and_label();
     clause1 = load_variable(argu[0],argu[1],clause1);
     if(clause1.contains(" ")){
       tmp = clause1.split(" ");
       clause1 = tmp[1];
     }
     System.out.println("\tbr i1 "+clause1+" , label %"+label1+" , label %"+label2);
     System.out.println(label1+":");
     String clause2 = n.f2.accept(this, argu);
     clause2 = load_variable(argu[0],argu[1],clause2);
     if(clause2.contains(" ")){
       tmp = clause2.split(" ");
       clause2 = tmp[1];
     }
     System.out.println("\tbr label %"+label3);
     System.out.println(label2+":");
     System.out.println("\tbr label %"+label3);
     System.out.println(label3+":");
     String ret = next_local_var();
     System.out.println("\t"+ret+" = phi i1 ["+clause2+", %"+label1+"], [0, %"+label2+"] ");
     return "i1 "+ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public String visit(TimesExpression n,String[] argu) throws Exception {
    String a2,b2;
    String a1 = n.f0.accept(this, argu);
    String b1 = n.f2.accept(this, argu);
    String rt;
    a1 = load_variable(argu[0],argu[1],a1);
    b1 = load_variable(argu[0],argu[1],b1);
    rt = next_local_var();
    if(a1.contains(" ")){
      String[] tmp = a1.split(" ");
      a1 = tmp[1];
    }
    if(b1.contains(" ")){
      String[] tmp = b1.split(" ");
      b1 = tmp[1];
    }
    System.out.println("\t"+rt+" = mul i32 "+a1+"," +b1);
    return "i32 "+rt;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public String visit(ArrayLookup n,String[] argu) throws Exception {
     String array = n.f0.accept(this, argu);
     String index = n.f2.accept(this, argu);
     String array_var = load_variable(argu[0],argu[1],array);
     String index_var = load_variable(argu[0],argu[1],index);
     String length_var = next_local_var();
     String compare_var = next_local_var();
     String compare_var2 = next_local_var();
     System.out.println("\t"+length_var+" = load i32, i32* "+array_var);
     System.out.println("\t"+compare_var+" = icmp ult i32 "+length_var+","+index_var);
     String iflabel = next_oob_label();
     String elselabel = next_oob_label();
     System.out.println("\tbr i1 "+compare_var+" , label %"+iflabel+" ,label %"+elselabel);
     System.out.println(iflabel+":");
     System.out.println("\tcall void @throw_oob()");
     System.out.println("\tbr label %"+elselabel);
     System.out.println(elselabel+":");
     String new_index = next_local_var();
     System.out.println("\t"+new_index+" = add i32 "+index_var+" , 1");
     String ptr_value = next_local_var();
     System.out.println("\t"+ptr_value+" = getelementptr i32 , i32* "+array_var+" , i32 "+new_index);
     String value = next_local_var();
     System.out.println("\t"+value+" = load i32 , i32* "+ptr_value);
     return "i32 "+value;
  }

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public String visit(ArrayAllocationExpression n,String[] argu) throws Exception {
     String array_size = n.f3.accept(this, argu);
     String[] tmp = array_size.split(" ");
     String comp_var = next_local_var();
     String oob_label = next_oob_label();
     String not_oob_label = next_oob_label();
     /*if index<0 throw_oob*/
     String array_size_var = load_variable(argu[0],argu[1],array_size,true);
     System.out.println("\t"+comp_var+" = icmp slt "+array_size_var+" , 0");
     System.out.println("\t br i1 "+comp_var+" , label %"+oob_label+" , label %"+not_oob_label);
     System.out.println(oob_label+":");
     System.out.println("\tcall void @throw_oob()");
     System.out.println("\tbr label %"+not_oob_label);
     System.out.println(not_oob_label+":");
     String new_size = next_local_var();
     String calloc_var = next_local_var();
     String bitcast_var = next_local_var();
     /*alloc memory for array , store its size */
     System.out.println("\t"+new_size+" = add "+array_size_var+" , 1");
     System.out.println("\t"+calloc_var+" = call i8* @calloc(i32 4, i32 "+new_size+")");
     System.out.println("\t"+bitcast_var+" = bitcast i8* "+calloc_var+" to i32*");
     System.out.println("\tstore "+array_size_var+" , i32* "+bitcast_var);
     return "i32* "+bitcast_var;
  }
  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  public String visit(ArrayLength n,String[] argu) throws Exception {

    String prim_expr = n.f0.accept(this, argu);
    String array = load_variable(argu[0],argu[1],prim_expr);
    //prepei na fortoso apo ton array tin proti thesi pou exei to length kai na griso autin
    String var = next_local_var(); //var pou tha apothikeuso to length
    return "i32 "+var;
   }

  /**
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  public String visit(AllocationExpression n, String[] argu) throws Exception {
    if(argu==null){
      String[] a = {"argu"};
      return n.f1.accept(this, a);
    }
     String id = n.f1.accept(this, argu);
     String classname = argu[0];
     String methodname = argu[1];
     int size = stp.get_object_size(id)+8;
     String var1 = next_local_var();
     String var2 = next_local_var();
     String var3 = next_local_var();
     System.out.println("\t"+var1 +" = call i8* @calloc(i32 1, i32 "+size+")");
     System.out.println("\t"+var2+" = bitcast i8* "+var1+" to i8***");
     int vtable_size = stp.get_all_methods_size(id);
     System.out.println("\t"+var3+" = getelementptr ["+vtable_size+" x i8*] , ["+vtable_size+" x i8*]* @."+id+"_vtable , i32 0 , i32 0");
     System.out.println("\tstore i8** "+var3+", i8*** "+var2);
     return "i8* "+var1;
  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  public String visit(IfStatement n,String[] argu) throws Exception {
     String expr = n.f2.accept(this, argu);
     String iflabel = next_if_label();
     String elselabel = next_if_label();
     String afteriflabel = next_if_label();
     expr = load_variable(argu[0],argu[1],expr);
     if(expr.contains(" ")){
       String[] tmp = expr.split(" ");
       expr = tmp[1];
     }
     System.out.println("\tbr i1 "+expr+", label %"+iflabel+", label %"+elselabel);
     System.out.println(iflabel+":");
     n.f4.accept(this, argu);
     System.out.println("\tbr label %"+afteriflabel);
     System.out.println(elselabel+":");
     n.f6.accept(this, argu);
     System.out.println("\tbr label %"+afteriflabel);
     System.out.println(afteriflabel+":");
     return "";
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public String visit(WhileStatement n,String[] argu) throws Exception {
    String firstlabel = next_while_label();
    String whilelabel = next_while_label();
    String afterwhilelabel = next_while_label();
    System.out.println("\tbr label %"+firstlabel);
    System.out.println(firstlabel+":");
    String expr = n.f2.accept(this, argu);
    expr = load_variable(argu[0],argu[1],expr);
    if(expr.contains(" ")){
      String[] tmp = expr.split(" ");
      expr = tmp[1];
    }
    System.out.println("\tbr i1 "+expr+", label %"+whilelabel+", label %"+afterwhilelabel);
    System.out.println(whilelabel+":");
    n.f4.accept(this,argu);
    System.out.println("\tbr label %"+firstlabel);
    System.out.println(afterwhilelabel+":");
    return "";

  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  public String visit(BracketExpression n, String[] argu) throws Exception {
     return n.f1.accept(this, argu);
  }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n,String[] argu) throws Exception {
      System.out.println(";formal prmtr in method : "+argu[1]);
      String[] n_argu={argu[0],argu[1],""};
      n_argu[2] += n.f0.accept(this, n_argu);
      String tail = n.f1.accept(this, n_argu);
      System.out.println(";formal prmtr in method : "+n_argu[2]);
      return n_argu[2];
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n,String[] argu) throws Exception {
       return n.f0.accept(this, argu) +" %."+n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n,String[] argu) throws Exception {
       argu[2] += ", "+n.f1.accept(this, argu);
       return "";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n,String[] argu) throws Exception {
      String type = n.f0.accept(this, argu);
      String id = n.f1.accept(this, argu);
      System.out.println("\t%"+id +"= alloca "+type);
      return "";
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n,String[] argu) throws Exception {
       return ll_type(n.f0.accept(this, argu));
    }

  /**
   * f0 -> "int"
   * f1 -> "["
   * f2 -> "]"
   */
  public String visit(ArrayType n, String[] argu) throws Exception {
     return "int[]";
  }

  /**
   * f0 -> "boolean"
   */
  public String visit(BooleanType n,String[] argu) throws Exception {
     return "boolean";
  }

  /**
   * f0 -> "int"
   */
  public String visit(IntegerType n,String[] argu) throws Exception {
     return "int";
  }


  /**
   * f0 -> "true"
   */
  public String visit(TrueLiteral n, String[] argu) throws Exception {
    return "i1 1";
  }

  /**
   * f0 -> "false"
   */
  public String visit(FalseLiteral n,  String[] argu) throws Exception {
     return "i1 0";
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public String visit(Identifier n, String[] argu) throws Exception {
    if(argu == null){
      return null;
    }
     return n.f0.toString();
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public String visit(IntegerLiteral n, String[] argu) throws Exception {
     return "i32 "+n.f0.toString();
  }
  /**
   * f0 -> "this"
   */
  public String visit(ThisExpression n,String[] argu) throws Exception {
     return "%"+n.f0.toString();
  }
}
