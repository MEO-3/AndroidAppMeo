package org.thingai.android.meo.ui.screen.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.thingai.android.meo.navigation.Route
import org.thingai.android.meo.ui.shared.appbar.BaseTopAppBar
import org.thingai.android.meo.ui.viewmodel.auth.VMOtpVerify

@Composable
fun OtpVerifyScreen(
    navController: NavController,
    vm: VMOtpVerify = hiltViewModel()
) {
    val ui = vm.uiState.collectAsStateWithLifecycle().value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            BaseTopAppBar(onBack = {navController.popBackStack()}, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            ))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enter OTP code to verify",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enter the OTP code sent to ${ui.phone}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OtpCodeField(
                        value = ui.otp,
                        onValueChange = vm::onOtpChanged,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val resendText = if (ui.resendEnabled) "Re-send" else "Re-send (${ui.resendCountdownSeconds}s)"
                        Text(
                            text = "Not receive code? ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = resendText,
                            color = if (ui.resendEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = if (ui.resendEnabled) Modifier.clickable { vm.resendCode() } else Modifier
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (ui.errorMessage != null) {
                    Text(
                        text = ui.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { vm.verifyOtp({
                        navController.navigate(Route.RESET_PASSWORD+"?phone=${ui.phone}")
                    }) },
                    enabled = ui.canVerify && !ui.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    if (ui.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Verify",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun OtpCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 4,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = { v -> onValueChange(v.filter { it.isDigit() }.take(length)) },
        decorationBox = { _ ->
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(length) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        },
        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}