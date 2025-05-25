package src;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import src.lex.BabyDuckLexer;
import src.lex.BabyDuckParser;
import src.sem.exps.QuadrupleGenerator;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length==0) {
            System.err.println("Debes proporcionar un archivo .bd");
            System.exit(1);
        }
        CharStream input = CharStreams.fromFileName(args[0]);
        BabyDuckLexer lexer = new BabyDuckLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BabyDuckParser parser = new BabyDuckParser(tokens);
        ParseTree tree = parser.programa();
        SemanticVisitor visitor = new SemanticVisitor();
        visitor.visit(tree);
        System.out.println("Análisis semántico exitoso.");
        System.out.println(visitor.getFunctionDirectory());
        QuadrupleGenerator.displayOnProgramEnd(visitor.getQuadrupleGenerator());
    }
}