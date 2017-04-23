package com.wreulicke.lombok.javac

import java.util.Set

import scala.collection.JavaConverters.asScalaSetConverter

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.tree.MethodTree
import com.sun.source.util.TreeScanner
import com.sun.source.util.Trees
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree.JCExpression
import com.sun.tools.javac.tree.JCTree.JCMethodDecl
import com.sun.tools.javac.tree.JCTree.JCStatement
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.List

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import com.sun.tools.javac.tree.JCTree.JCLiteral

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(Array("*"))
class FizzBuzzProcessor extends AbstractProcessor {
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
    private def elements = JavacElements.instance(env.getContext)

    private def maker: TreeMaker = TreeMaker.instance(env.getContext)
    private def Literal(str: => String): JCLiteral = maker.Literal(str)
    private def convertFizzBuzzLiteral(n: Int): JCLiteral =
      Literal {
        if (n % 15 == 0) "FizzBuzz"
        else if (n % 3 == 0) "Fizz"
        else if (n % 5 == 0) "Buzz"
        else n.toString
      }

    private def createPrintMethod(): JCExpression =
      maker.Select(
        maker.Select(
          maker.Ident(
            elements.getName("System")),
          elements.getName("out")),
        elements.getName("println"))

    override def visitMethod(methodTree: MethodTree, p: Unit): Unit = {
      if (methodTree.getName contentEquals "main") {
        methodTree match {
          case method: JCMethodDecl => {
            val statements: Iterable[JCStatement] = (0 to 100) map convertFizzBuzzLiteral map { literal =>
              maker.Apply(List.nil(), createPrintMethod(), List.of(literal))
            } map maker.Exec _
            method.body = maker.Block(8L, List.from(statements.toArray))
          }
        }
      }
    }
  }

}