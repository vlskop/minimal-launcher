package com.example.minimallauncher.launcher.drawer

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

internal data class CircularRippleIndication(
    val color: Color
) : IndicationNodeFactory {

    override fun create(
        interactionSource: InteractionSource
    ): DelegatableNode {
        return CircularRippleNode(interactionSource, color)
    }
}

private class CircularRippleNode(
    private val interactionSource: InteractionSource,
    private val color: Color
) : Modifier.Node(), DrawModifierNode {
    private var isPressed by mutableStateOf(false)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release,
                        is PressInteraction.Cancel -> isPressed = false
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        if (isPressed) {
            drawCircle(
                color = color.copy(alpha = 0.18f),
                radius = size.minDimension / 2f
            )
        }
    }
}