package com.wreulicke.lombok.javac

import java.util.Set

import scala.collection.JavaConverters.asScalaSetConverter

import com.sun.source.tree.VariableTree
import com.sun.source.util.TreeScanner
import com.sun.source.util.Trees

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.SourceVersion
import com.sun.source.util.TreePath
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.VariableElement
import com.sun.tools.javac.tree.JCTree.JCVariableDecl
import com.sun.tools.javac.tree.JCTree.JCExpression
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.Tag
import com.sun.source.tree.Tree
import com.sun.source.tree.Tree.Kind
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.code.Type.JCPrimitiveType
import com.sun.tools.javac.code.TypeTag
import javax.tools.Diagnostic
import com.sun.tools.javac.tree.JCTree.JCIdent
import scala.util.Success
import com.sun.source.tree.AssignmentTree
import com.sun.source.tree.CompilationUnitTree
import com.sun.tools.javac.tree.JCTree.JCNewClass
import com.sun.tools.javac.tree.JCTree.JCFieldAccess
import com.sun.source.tree.LiteralTree

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(Array("*"))
class ScalaProcessor extends AbstractProcessor {
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
    def string(): JCExpression = {
      maker.Select(maker.Select(maker.Ident(elements.getName("java")), elements.getName("lang")), elements.getName("String"))
    }
    type Message = String
    type SuccessOrFail = Either[Message, Option[JCExpression]]
    private def success(): SuccessOrFail = Right(None)
    private def success(exp: JCExpression): SuccessOrFail = Right(Some(exp))
    private def fail(msg: String): SuccessOrFail = Left(msg)

    private def convertType(implicit declaration: JCVariableDecl): SuccessOrFail = {
      if (declaration.vartype.toString == "var") {
        val initializer = declaration.getInitializer
        val tag = initializer.getTag
        tag match {
          case Tag.LITERAL => convertTypeFromLiteral(initializer.getKind)
          case Tag.NEWCLASS => convertTypeFromInitializer(initializer.asInstanceOf[JCNewClass])
          case _ => fail("cannot use here")
        }
      } else success()
    }
    
    private def convertTypeFromInitializer(initializer: JCNewClass): SuccessOrFail = {
      initializer.clazz match {
        case id: JCIdent => success(maker.Ident(elements.getName(id.name)))
        case fieldAccess: JCFieldAccess => success(fieldAccess)
        case _ => fail("cannot use here")
      }
    }
    
    private def convertTypeFromLiteral(kind: Kind)(implicit declaration: JCVariableDecl): SuccessOrFail = {
      kind match {
        case Kind.STRING_LITERAL => success(string())
        case Kind.INT_LITERAL => success(maker.Type(new JCPrimitiveType(TypeTag.INT, null)))
        case Kind.BOOLEAN_LITERAL => success(maker.Type(new JCPrimitiveType(TypeTag.BOOLEAN, null)))
        case Kind.DOUBLE_LITERAL => success(maker.Type(new JCPrimitiveType(TypeTag.DOUBLE, null)))
        case Kind.LONG_LITERAL => success(maker.Type(new JCPrimitiveType(TypeTag.LONG, null)))
        case Kind.FLOAT_LITERAL => success(maker.Type(new JCPrimitiveType(TypeTag.FLOAT, null)))
        case Kind.CHAR_LITERAL => success(maker.Type(new JCPrimitiveType(TypeTag.CHAR, null)))
        case Kind.NULL_LITERAL => fail("cannot use here")
        case _ => fail("cannot use here")
      }
    }

    private def modifyTypeIfNeeded(declaration: JCVariableDecl): Unit = {
      convertType(declaration) match {
        case Right(typ) => {
          typ.foreach { typ =>
            declaration.vartype = typ
          }
        }
        case Left(message) => {
          roundEnv.getMessager.printMessage(Diagnostic.Kind.ERROR, message)
        }
      }
    }
    override def visitVariable(node: VariableTree, _p: Unit): Unit = {
      node match {
        case declaration: JCVariableDecl => modifyTypeIfNeeded(declaration)
      }
    }
  }
}