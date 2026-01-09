package org.thingai.android.meo.ui.component.dialog

import android.view.Gravity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import org.thingai.android.meo.ui.component.custom.DialogPosition
import org.thingai.android.meo.ui.component.custom.DialogPosition.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDialog(
    onDismiss: () -> Unit,
    position: DialogPosition,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        modifier = modifier
            .fillMaxWidth(),
        onDismissRequest = onDismiss,
        properties = properties,
    ) {
        when (position) {
            BOTTOM -> {
                val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
                dialogWindowProvider.window.setGravity(Gravity.BOTTOM)
            }

            TOP -> {
                val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
                dialogWindowProvider.window.setGravity(Gravity.TOP)
            }
            CENTER -> Unit
        }
        content()
    }
}