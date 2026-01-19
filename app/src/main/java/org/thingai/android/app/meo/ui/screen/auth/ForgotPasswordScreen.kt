package org.thingai.android.app.meo.ui.screen.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.thingai.android.app.meo.navigation.Route
import org.thingai.android.app.meo.ui.shared.appbar.BaseTopAppBar
import org.thingai.android.app.meo.ui.shared.dialog.ErrorDialog
import org.thingai.android.app.meo.ui.shared.dialog.SuccessDialog
import org.thingai.android.app.meo.ui.viewmodel.auth.VMForgotPassword

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    vm: VMForgotPassword = hiltViewModel(),
) {
    val ui = vm.uiState.collectAsStateWithLifecycle().value

    // Success dialog when code is sent — navigate to OTP when user confirms
    SuccessDialog(
        show = ui.isCodeSent,
        title = "OTP sent",
        message = "An OTP code has been sent to ${ui.email}.",
        onDismiss = { vm.clearCodeSent() },
        onConfirm = {
            vm.clearCodeSent()
            val route: String = Route.VERIFY_OTP + "?phone=${ui.email}"
            navController.navigate(route) {
                popUpTo(Route.FORGOT_PASSWORD) { inclusive = true }
            }
        }
    )

    // Error dialog
    ErrorDialog(
        show = ui.errorMessage != null,
        message = ui.errorMessage,
        onDismiss = { vm.clearError() }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 24.dp),
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
                    text = "Forgot password",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enter email to receive code.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = ui.email,
                    onValueChange = { vm.onEmailChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = { Text("Enter email") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = { vm.sendCode() },
                    enabled = ui.canSubmit && !ui.isLoading,
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
                            "Receive code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign up now",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}