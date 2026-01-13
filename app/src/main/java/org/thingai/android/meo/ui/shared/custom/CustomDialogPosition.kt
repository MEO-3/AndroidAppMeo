package org.thingai.android.meo.ui.shared.custom

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import org.thingai.android.meo.ui.shared.custom.DialogPosition.*

enum class DialogPosition {
    TOP,
    BOTTOM,
    CENTER
}

@Deprecated("Old dialog modifier for bottom dialog, broken because unable to use outside space, use BaseDialog instead")
fun Modifier.customDialogModifier(position: DialogPosition) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints);
    layout(constraints.maxWidth, constraints.maxHeight){
        when(position) {
            BOTTOM -> placeable.place(0, constraints.maxHeight - placeable.height, 10f)
            TOP -> placeable.place(0,0,10f)
            CENTER -> Unit
        }
    }
}