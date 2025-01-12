package de.florianm.tool

import com.squareup.kotlinpoet.*
import de.florianm.klox.Token
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(
    arguments: Array<String>
) {
    if (3 != arguments.size) {
        println("AstGenerator outputPath packageName inputPath")
        exitProcess(64)
    } else {
        AstGenerator().generateAst(
            arguments[0],
            arguments[1],
            arguments[2]
        )
    }
}


class AstGenerator {
    fun generateAst(
        outputPath: String,
        packageName: String,
        inputPath: String,
    ) {

        val exprClassName = ClassName(packageName, "Expr")
        val fileSpec = FileSpec.builder(exprClassName)

        val exprInterface = TypeSpec.interfaceBuilder(exprClassName)
            .addModifiers(KModifier.SEALED)
            .build()


        val types = mapOf(
            "Any" to typeNameOf<Any>(),
            "Token" to typeNameOf<Token>(),
            "Expr" to exprClassName,
        )

        val exprClasses = mutableListOf<TypeSpec>()
        val lines = Files.readAllLines(Path.of(inputPath))
        for (line in lines) {
            val (exprName, variablesStr) = line.split(" - ").map(String::trim)

            val exprClass = TypeSpec.classBuilder(exprName)
                .addModifiers(KModifier.DATA)
                .addSuperinterface(exprClassName)

            val constructorSpec = FunSpec.constructorBuilder()

            val variables = variablesStr.split(",").map(String::trim)
            for (variable in variables) {
                val (varName, varType) = variable.split(":").map(String::trim)

                types[varType]?.let {
                    constructorSpec.addParameter(varName, it)

                    exprClass.addProperty(
                        PropertySpec
                            .builder(varName, it)
                            .initializer(varName)
                            .build()
                    )

                } ?: "Unknown type $varType"
            }

            exprClass.primaryConstructor(constructorSpec.build())
            exprClasses += exprClass.build()
        }


        fileSpec.addType(
            exprInterface.toBuilder()
                .addTypes(exprClasses)
                .build()
        )
        fileSpec.build().writeTo(Path.of(outputPath))
    }
}