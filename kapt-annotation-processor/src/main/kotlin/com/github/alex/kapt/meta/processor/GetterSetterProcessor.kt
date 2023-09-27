@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package com.github.alex.kapt.meta.processor

import com.github.alex.kapt.meta.annotation.Getter
import com.sun.source.tree.Tree
import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.Type.JCVoidType
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import com.sun.tools.javac.util.Names
import java.lang.RuntimeException
import java.util.*
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.github.alex.kapt.meta.annotation.Getter", "com.github.alex.kapt.meta.annotation.Setter")
@com.google.auto.service.AutoService(Processor::class)
class GetterSetterProcessor: AbstractProcessor() {
    private var messager: Messager? = null // 编译时期输入日志的

    private var javacTrees: JavacTrees? = null // 提供了待处理的抽象语法树

    private var treeMaker: TreeMaker? = null // 封装了创建AST节点的一些方法

    private var names: Names? = null // 提供了创建标识符的方法


    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        javacTrees = JavacTrees.instance(processingEnv)
        val context = (processingEnv as JavacProcessingEnvironment).context
        treeMaker = TreeMaker.instance(context)
        names = Names.instance(context)
    }
    override fun process(annotations: Set<TypeElement?>?, roundEnv: RoundEnvironment): Boolean {
        val elementsAnnotatedWith: Set<Element?> = roundEnv.getElementsAnnotatedWith(Getter::class.java)
        elementsAnnotatedWith.forEach { e: Element? ->
            val tree = javacTrees!!.getTree(e)
            tree.accept(object : TreeTranslator() {
                override fun visitClassDef(jcClassDecl: JCClassDecl) {
                    var jcVariableDeclList =
                        List.nil<JCVariableDecl>()
                    // 在抽象树中找出所有的变量
                    for (jcTree in jcClassDecl.defs) {
                        if (jcTree.kind == Tree.Kind.VARIABLE) {
                            val jcVariableDecl = jcTree as JCVariableDecl
                            jcVariableDeclList = jcVariableDeclList.append(jcVariableDecl)
                        }
                    }

                    // 对于变量进行生成方法的操作
                    jcVariableDeclList.forEach { jcVariableDecl: JCVariableDecl ->
                        messager!!.printMessage(
                            Diagnostic.Kind.NOTE,
                            jcVariableDecl.getName().toString() + " has been processed"
                        )
                        jcClassDecl.defs = jcClassDecl.defs
                            .prepend(makeGetterMethodDecl(jcVariableDecl))
                            .append(makeSetterMethodDecl(jcVariableDecl))
                    }
                    super.visitClassDef(jcClassDecl)
                }
            })
        }
        return true
    }


    private fun makeGetterMethodDecl(jcVariableDecl: JCVariableDecl): JCMethodDecl {
        val statements = ListBuffer<JCStatement>()
        // 生成return this.<varName>
        val returnStatement = treeMaker!!.Return(
            treeMaker!!.Select(treeMaker!!.Ident(names!!._this), jcVariableDecl.getName())
        )
        statements.append(returnStatement)
        val block = treeMaker!!.Block(0, statements.toList())
        //返回值类型
        val methodType = treeMaker!!.Type(jcVariableDecl.getType().type)
        return treeMaker!!.MethodDef(
            treeMaker!!.Modifiers(Flags.PUBLIC.toLong()),
            addPrefix("get", jcVariableDecl.getName()), methodType, List.nil(), List.nil(), List.nil(), block, null
        )
    }


    private fun makeSetterMethodDecl(jcVariableDecl: JCVariableDecl): JCMethodDecl {
        val statements = ListBuffer<JCStatement>()
        val aThis = makeAssignment(
            treeMaker!!.Select(treeMaker!!.Ident(names!!.fromString("this")), jcVariableDecl.getName()),
            treeMaker!!.Ident(jcVariableDecl.getName())
        )
        statements.append(aThis)
        val block = treeMaker!!.Block(0, statements.toList())
        val param = treeMaker!!.VarDef(
            treeMaker!!.Modifiers(Flags.PARAMETER),
            jcVariableDecl.getName(),
            jcVariableDecl.vartype,
            null
        )
        val parameters = List.of(param)
        val methodType = treeMaker!!.Type(JCVoidType())
        return treeMaker!!.MethodDef(
            treeMaker!!.Modifiers(Flags.PUBLIC.toLong()),
            addPrefix("set", jcVariableDecl.getName()), methodType, List.nil(), parameters, List.nil(), block, null
        )
    }

    private fun addPrefix(prefix: String, name: Name): Name {
        val s = name.toString()
        return names!!.fromString(
            prefix + s.substring(0, 1).uppercase(Locale.getDefault()) + s.substring(
                1,
                name.length
            )
        )
    }

    private fun makeAssignment(lhs: JCExpression, rhs: JCExpression): JCExpressionStatement {
        return treeMaker!!.Exec(treeMaker!!.Assign(lhs, rhs))
    }

}