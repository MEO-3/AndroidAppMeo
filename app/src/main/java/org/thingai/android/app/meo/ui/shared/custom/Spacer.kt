package org.thingai.android.app.meo.ui.shared.custom

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


object MeoSpacerSize {
    val TINY: Int = 4
    val SMALL: Int = 8
    val COMMON: Int = 16
    val LARGE: Int = 24
    val XLARGE: Int = 32
}

@Composable
fun MeoSpacer(size: Int) {
    Spacer(modifier = Modifier.padding(size.dp))
}