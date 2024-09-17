package com.bytecause.nautichart.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.NotificationPermissionDeniedAlertDialogLayoutBinding
import com.bytecause.presentation.theme.AppTheme
import com.bytecause.util.delegates.viewBinding

class NotificationPermissionDeniedAlertDialog :
    DialogFragment(R.layout.notification_permission_denied_alert_dialog_layout) {

    private val binding by viewBinding(NotificationPermissionDeniedAlertDialogLayoutBinding::inflate)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationPermissionDeniedComposeLayout.setContent {
            AppTheme {
                NotificationPermissionDeniedAlertDialogScreen(
                    onGrant = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        // opens Settings Screen(Activity) as new activity. Otherwise, it will be opened in currently running activity.
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri = Uri.fromParts("package", context?.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                        dismiss()
                    }
                )
            }
        }
    }

    @Composable
    fun NotificationPermissionDeniedAlertDialogScreen(
        onGrant: () -> Unit
    ) {
        NotificationPermissionDeniedAlertDialogContent(
            onGrant = onGrant
        )
    }

    @Composable
    fun NotificationPermissionDeniedAlertDialogContent(
        onGrant: () -> Unit
    ) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { dismiss() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Notification Denied Alert",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        Icon(
                            painter = painterResource(id = com.bytecause.core.resources.R.drawable.notification_off),
                            contentDescription = "Notifications off",
                            modifier = Modifier.size(120.dp)
                        )
                        Text(
                            text = "To stay informed about important background processes such as dataset downloading and updates, please grant the notification permission. Without this permission, you won't receive notifications about these critical updates.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }

                    Row(modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
                        ElevatedButton(onClick = onGrant) {
                            Text(text = stringResource(id = com.bytecause.core.resources.R.string.grant))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        ElevatedButton(onClick = { dismiss() }) {
                            Text(text = stringResource(id = com.bytecause.core.resources.R.string.cancel))
                        }
                    }
                }
            }
        }
    }

    @Composable
    @Preview
    fun NotificationPermissionDeniedAlertDialogContentPreview() {
        NotificationPermissionDeniedAlertDialogContent(
            onGrant = {}
        )
    }
}