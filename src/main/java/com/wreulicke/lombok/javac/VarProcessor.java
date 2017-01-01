package com.wreulicke.lombok.javac;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.tree.TreeMaker;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class VarProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    JavacProcessingEnvironment environment = (JavacProcessingEnvironment) processingEnv;
    Trees trees = Trees.instance(environment);
    roundEnv.getRootElements()
      .stream()
      .map(trees::getPath)
      .map(TreePath::getCompilationUnit)
      .forEach((t) -> {
        t.accept(new Visitor(environment), null);
      });
    return false;
  }

  private class Visitor extends TreeScanner<Void, Void> {
    TreeMaker maker;
    JavacElements elements;

    public Visitor(JavacProcessingEnvironment environment) {
      maker = TreeMaker.instance(environment.getContext());
      elements = JavacElements.instance(environment.getContext());
    }


    @Override
    public Void reduce(Void arg0, Void arg1) {
      // TODO Auto-generated method stub
      return super.reduce(arg0, arg1);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
      if (node instanceof JCVariableDecl) {
        JCVariableDecl declaration = (JCVariableDecl) node;
        if (declaration.vartype.toString()
          .equals("var")) {

          JCExpression expression = declaration.getInitializer();
          if (expression != null) {
            Kind kind = expression.getKind();
            if (Tag.LITERAL.equals(expression.getTag())) {
              if (Kind.STRING_LITERAL.equals(kind)) {
                JCExpression ex = maker.Ident(elements.getName("java"));
                ex = maker.Select(ex, elements.getName("lang"));
                ex = maker.Select(ex, elements.getName("String"));
                declaration.vartype = ex;
              }
              else if (Kind.INT_LITERAL.equals(kind)) {
                declaration.vartype = maker.Type(new Type.JCPrimitiveType(TypeTag.INT, null));
              }
            }
          }
        }
      }
      return null;
    }
  }
}
