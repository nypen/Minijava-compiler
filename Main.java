import syntaxtree.*;
import visitor.*;
import java.io.*;
import stp.*;

class Main {
public static void main (String [] args) throws Exception{
   MiniJavaParser parser;
   FileInputStream fis = null;
   STPvisitor stp;
   LLVMvisitor llvm;
   for(int i=0;i<args.length;i++){
     try{
       fis = new FileInputStream(args[i]);
       parser = new MiniJavaParser(fis);
       System.err.println("Program parsed successfully "+args[i]);
       stp = new STPvisitor();
       Goal root = parser.Goal();
       root.accept(stp, null);
       stp.get_stp().set_offsets();
       llvm = new LLVMvisitor(stp.get_stp(),String.format("file%d.ll",i+1));
       root.accept(llvm, null);

    }
    catch(ParseException ex){
      System.out.println(ex.getMessage());
    }
    catch(FileNotFoundException ex){
      System.err.println(ex.getMessage());
      continue;
    }
    catch(Exception ex){
      System.err.println(ex.getMessage());
      continue;
    }
    finally{
      try{
        if(fis != null) fis.close();
      }
      catch(IOException ex){
        System.err.println(ex.getMessage());
      }
    }
  }
  }
}
