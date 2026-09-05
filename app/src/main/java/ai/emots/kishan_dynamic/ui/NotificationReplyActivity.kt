package ai.emots.kishan_dynamic.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.service.NotificationActionRegistry
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme

class NotificationReplyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val actionId = intent.getStringExtra(EXTRA_ACTION_ID)
        if (notificationId.isNullOrBlank() || actionId.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            AppTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme()) {
                NotificationReplyScreen(
                    appName = intent.getStringExtra(EXTRA_APP_NAME).orEmpty().ifBlank { "Notification" },
                    sender = intent.getStringExtra(EXTRA_SENDER).orEmpty(),
                    message = intent.getStringExtra(EXTRA_MESSAGE).orEmpty(),
                    onDismiss = ::finish,
                    onSend = { reply ->
                        NotificationActionRegistry.sendReply(
                            context = applicationContext,
                            notificationId = notificationId,
                            actionId = actionId,
                            reply = reply
                        ).also { sent -> if (sent) finish() }
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_ACTION_ID = "action_id"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_SENDER = "sender"
        const val EXTRA_MESSAGE = "message"
    }
}

@Composable
private fun NotificationReplyScreen(
    appName: String,
    sender: String,
    message: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var reply by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    AppScreen(modifier = Modifier.imePadding()) {
        AppTopBar(
            title = "Reply",
            subtitle = appName,
            onBack = onDismiss
        )
        AppCard {
            AppText(
                text = sender.ifBlank { appName },
                style = AppTheme.typography.h3,
                color = AppTheme.colors.textPrimary
            )
            if (message.isNotBlank()) {
                AppText(
                    text = message,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = AppTheme.spacing.xs)
                )
            }
            BasicTextField(
                value = reply,
                onValueChange = {
                    reply = it
                    error = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .padding(top = AppTheme.spacing.lg)
                    .focusRequester(focusRequester)
                    .background(
                        AppTheme.colors.surfaceVariant,
                        RoundedCornerShape(AppTheme.radius.md)
                    )
                    .padding(AppTheme.spacing.md),
                textStyle = AppTheme.typography.body.copy(color = AppTheme.colors.textPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (reply.isBlank()) error = true else onSend(reply.trim())
                }),
                decorationBox = { innerTextField ->
                    if (reply.isBlank()) {
                        AppText("Write a reply…", style = AppTheme.typography.body, color = AppTheme.colors.textTertiary)
                    }
                    innerTextField()
                }
            )
            if (error) {
                AppText(
                    text = "Write a reply before sending.",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.error,
                    modifier = Modifier.padding(top = AppTheme.spacing.xs)
                )
            }
            AppButton(
                text = "Send reply",
                onClick = {
                    if (reply.isBlank()) error = true else onSend(reply.trim())
                },
                modifier = Modifier.padding(top = AppTheme.spacing.lg)
            )
        }
    }
}
