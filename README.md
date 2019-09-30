# Minijava-compiler
Semantic Analysis ,static checking and generation of intermediate code on Minijava.

Compilation: make compile
Execution: java [MainClassName] [file1] [file2] ... [fileN]

STPvisitor:
Is responsible for the semantic analysis of the input files

LLVMvisitor:
Generates the intermediate code after files pass semantic analysis
