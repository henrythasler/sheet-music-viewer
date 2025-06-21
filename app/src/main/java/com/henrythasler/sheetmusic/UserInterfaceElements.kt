package com.henrythasler.sheetmusic

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Preview
@Composable
fun AboutDialog(
    visible: MutableState<Boolean> = mutableStateOf(false)
) {
    CustomAlertDialog(
        visible = visible,
        title = {
            Text(text = "Verovio Demo")
        },
        content = {
            Column {
                Text("by Henry Thasler")
                Spacer(modifier = Modifier.height(16.dp))

                val verovioLink = buildAnnotatedString {
                    append("See ")
                    pushStringAnnotation(tag = "URL", annotation = "https://www.verovio.org")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary)) {
                        append("www.verovio.org")
                    }
                    pop()
                }
                val context = LocalContext.current
                Text(
                    text = verovioLink,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable {
                            val annotation = verovioLink.getStringAnnotations("URL", 0, verovioLink.length)
                                .firstOrNull()
                            annotation?.let {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    it.item.toUri()
                                )
                                context.startActivity(intent)
                            }
                        }
                )
            }
        }
    )
}

@Preview
@Composable
fun CustomAlertDialog(
    title: @Composable () -> Unit = {Text("Title")},
    content: @Composable () -> Unit = {Text("Description")},
    visible: MutableState<Boolean> = mutableStateOf(false)
){
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.verovio_logo_big),
                contentDescription = "Verovio Logo"
            )
        },
        title = title,
        text = content,
        onDismissRequest = {
            visible.value = false
        },
        confirmButton = {
            TextButton(
                onClick = {
                    visible.value = false
                }
            ) {
                Text("OK")
            }
        },
    )
}