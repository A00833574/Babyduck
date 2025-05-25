import lex.BabyDuckLexer;
import lex.BabyDuckParser;
import sem.SemanticVisitor;
import sem.exps.QuadrupleGenerator;
import sem.funcs.FunctionDirectory;
import mem.VirtualMemoryManager;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

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