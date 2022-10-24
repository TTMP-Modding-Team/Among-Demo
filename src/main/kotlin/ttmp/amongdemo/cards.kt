package ttmp.amongdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ttmp.among.AmongEngine
import ttmp.among.compile.CompileResult
import ttmp.among.compile.ReportType
import ttmp.among.compile.Source
import ttmp.among.definition.*
import ttmp.among.obj.Among
import ttmp.among.util.LnCol
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

sealed class Card {
    data class SuccessCard(val milliseconds: Long) : Card()
    data class FailureCard(val errors: Int, val warnings: Int) : Card()
    data class ReportCard(val type: ReportType, val reportMessage: String, val lnCol: LnCol? = null) : Card()
    data class AmongCard(val among: Among) : Card()
    data class MacroCard(val macro: Macro) : Card()
    data class OperatorCard(val operator: OperatorDefinition) : Card()
}

@Composable
fun CardWidget(card: Card) {
    when (card) {
        is Card.SuccessCard -> CardBorder {
            Text("Compilation succeed at ${card.milliseconds} ms", modifier = Modifier.padding(3.dp))
        }
        is Card.FailureCard -> CardBorder {
            Text(
                "Compilation failed with ${card.errors} errors and ${card.warnings} warnings",
                modifier = Modifier.padding(3.dp)
            )
        }
        is Card.ReportCard -> CardBorder {
            Text(card.type.toString(), modifier = Modifier.padding(3.dp))
            Divider(modifier = Modifier.fillMaxWidth().height(1.dp))
            Text(card.lnCol?.let { "[$it] ${card.reportMessage}" } ?: card.reportMessage,
                modifier = Modifier.padding(3.dp))
        }
        is Card.AmongCard -> CardBorder {
            AmongWidget(card.among)
        }
        is Card.MacroCard -> CardBorder {
            Row {
                Text(
                    if (card.macro.type().isFunctionMacro) "Function" else "Macro",
                    modifier = Modifier.padding(3.dp)
                )
                VerticalDiv()
                val notEmpty = !card.macro.parameter().isEmpty
                Text(
                    when (card.macro.type()) {
                        MacroType.CONST, MacroType.ACCESS -> card.macro.name() + ":"
                        MacroType.OBJECT, MacroType.OBJECT_FN -> card.macro.name() + if (notEmpty) "{" else "{}:"
                        MacroType.LIST, MacroType.LIST_FN -> card.macro.name() + if (notEmpty) "[" else "[]:"
                        MacroType.OPERATION, MacroType.OPERATION_FN -> card.macro.name() + if (notEmpty) "(" else "():"
                    }, modifier = Modifier.padding(3.dp)
                )
            }
            if (!card.macro.parameter().isEmpty) {
                val closing = when (card.macro.type()) {
                    MacroType.OBJECT -> "} :"
                    MacroType.LIST -> "] :"
                    MacroType.OPERATION -> ") :"
                    else -> error("Unreachable")
                }
                Parameters(card.macro.parameter())
                HorizontalDiv()
                Text(closing, modifier = Modifier.padding(3.dp))
            }
            if (card.macro is MacroDefinition)
                AmongWidget(card.macro.template())
            else Text("/* code-defined macro */")
        }
        is Card.OperatorCard -> CardBorder {
            Text("Operator", modifier = Modifier.padding(3.dp))
            HorizontalDiv()
            Text(card.operator.toString(), modifier = Modifier.padding(3.dp))
        }
    }
}

@Composable
private fun Parameters(params: MacroParameterList) {
    for (i in 0 until params.size()) {
        val p = params.paramAt(i)
        key(p) {
            HorizontalDiv()
            val defaultValue = p.defaultValue()
            if (defaultValue == null)
                Text(p.name(), modifier = Modifier.padding(3.dp))
            else Row {
                Text(p.name(), modifier = Modifier.padding(3.dp))
                VerticalDiv()
                AmongWidget(defaultValue)
            }
        }
    }
}

@Composable
private fun AmongWidget(among: Among) = amongWidget(among) { it() }

@Composable
private fun List(values: List<Among>) {
    Column(
        modifier = Modifier.padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for ((index, value) in values.withIndex()) {
            HorizontalDiv()
            key(index, value) {
                amongWidget(value) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(index.toString(), modifier = Modifier.padding(3.dp))
                        it()
                    }
                }
            }
        }
    }
}

@Composable
private fun Obj(properties: Map<String, Among>) {
    Column(
        modifier = Modifier.padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for ((property, value) in properties.entries) {
            HorizontalDiv()
            key(property, value) {
                amongWidget(value) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$property :", modifier = Modifier.padding(3.dp))
                        it()
                    }
                }
            }
        }
    }
}

@Composable
private fun amongWidget(value: Among, valueToWidget: @Composable (@Composable () -> Unit) -> Unit) {
    if (value.isPrimitive) valueToWidget {
        Text("\"${value.asPrimitive().value}\"", modifier = Modifier.padding(3.dp))
    } else CardBorder {
        val nameable = value.asNameable()
        val type = if (nameable.isList) "List" else "Object"
        valueToWidget {
            if (nameable.hasName()) {
                Row {
                    Text(type, modifier = Modifier.padding(3.dp))
                    VerticalDiv()
                    Text(nameable.name, modifier = Modifier.padding(3.dp))
                }
            } else Text(type, modifier = Modifier.padding(3.dp))
        }
        if (nameable.isObj) Obj(nameable.asObj().properties())
        else List(nameable.asList().values())
    }
}

@Composable
private fun CardBorder(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    compose: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
    ) { compose() }
}

private val engine = AmongEngine()

@OptIn(ExperimentalTime::class)
fun amongToCards(sourceString: String): List<Card> {
    val res: CompileResult
    val src = Source.of(sourceString)
    val time = measureTime {
        res = engine.read(src)
    }
    val list = mutableListOf<Card>()

    if (!res.isSuccess) {
        list += Card.FailureCard(
            res.reports().count { it.type() == ReportType.ERROR },
            res.reports().count { it.type() == ReportType.WARN })
    } else list += Card.SuccessCard(time.inWholeMilliseconds)
    for (r in res.reports()) {
        list += Card.ReportCard(r.type(), r.message(), r.getLineColumn(src))
    }
    if (res.isSuccess) {
        for (macro in res.definition().macros().allMacros()) {
            list += Card.MacroCard(macro)
        }
        for (operator in res.definition().operators().allOperators()) {
            list += Card.OperatorCard(operator)
        }
        for (o in res.root().objects()) {
            list += Card.AmongCard(o)
        }
    }
    return list
}