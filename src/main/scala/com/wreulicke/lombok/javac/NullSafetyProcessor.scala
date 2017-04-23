package com.wreulicke.lombok.javac

import java.util.Set
import scala.collection.JavaConverters.asScalaSetConverter

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.tree.MethodTree
import com.sun.source.util.Trees
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree.JCExpression
import com.sun.tools.javac.tree.JCTree.JCLiteral
import com.sun.tools.javac.tree.JCTree.JCMethodDecl
import com.sun.tools.javac.tree.JCTree.JCStatement
import com.sun.tools.javac.tree.TreeMaker
import com.sun.source.util.TreeScanner

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.element.TypeElement
import com.sun.tools.javac.util.List
import javax.lang.model.SourceVersion
import com.sun.source.tree.LiteralTree
import com.sun.tools.javac.tree.JCTree.Tag
import com.sun.tools.javac.code.TypeTag
import com.sun.source.tree.Tree.Kind
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(Array("*"))
class NullSafetyProcessor extends AbstractProcessor{
  
  override def process(annotations: Set[_ <: TypeElement], roundEnv: RoundEnvironment): Boolean = {
    val env = processingEnv.asInstanceOf[JavacProcessingEnvironment]
    val trees = Trees.instance(env)
    val elements = roundEnv.getRootElements.asScala
    elements.foreach { element =>
      val unit = trees.getPath(element).getCompilationUnit
      unit.accept(Visitor(processingEnv, env, unit), null.asInstanceOf[Unit])
    }
    false
  }
  

  case class Visitor(roundEnv: ProcessingEnvironment, env: JavacProcessingEnvironment, unit: CompilationUnitTree) extends TreeScanner[Unit, Unit] {
    override def visitLiteral(literalTree:LiteralTree, x$2:Unit):Unit={
      literalTree match {
        case literal:JCLiteral =>  {
          literal.getKind match{
            case Kind.NULL_LITERAL => roundEnv.getMessager.printMessage(Diagnostic.Kind.ERROR, "you cannot use null literal")
            case _ => 
          }
        }
      }
    }
  }
}