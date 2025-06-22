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
import android.os.Build
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight

@Preview
@Composable
fun AboutDialog(
    visible: MutableState<Boolean> = mutableStateOf(false),
    verovioVersion: String? = null,
    ) {

    val sdk = Build.VERSION.SDK_INT // SDK version
    val versionName = BuildConfig.VERSION_NAME // App version name from BuildConfig
    val packageName = BuildConfig.APPLICATION_ID // App package name from BuildConfig
    val buildType = BuildConfig.BUILD_TYPE
    val buildDate = BuildConfig.BUILD_DATE
    val commitHash = BuildConfig.COMMIT_HASH
    val branchName = BuildConfig.BRANCH_NAME
    val data = mapOf(
        "Package" to packageName,
        "Version" to versionName,
        "Date" to buildDate,
        "Branch" to branchName,
        "Commit" to "$commitHash${if (BuildConfig.GIT_LOCAL_CHANGES) "-dirty" else ""}",
        "Build" to buildType,
        "API" to sdk.toString(),
        "Verovio" to (verovioVersion?:""),
    )

    val context = LocalContext.current

    CustomAlertDialog(
        visible = visible,
        title = {
            Text(text = "Verovio Demo")
        },
        content = {
            Column {
                Text("Created by Henry Thasler")
                Spacer(modifier = Modifier.height(16.dp))

                val verovioDemoLink = buildAnnotatedString {
                    pushStringAnnotation(tag = "URL", annotation = "https://github.com/henrythasler/verovio-demo-app")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary)) {
                        append("github.com/henrythasler/verovio-demo-app")
                    }
                    pop()
                }

                Text(
                    text = verovioDemoLink,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable {
                            val annotation = verovioDemoLink.getStringAnnotations("URL", 0, verovioDemoLink.length)
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

                Spacer(modifier = Modifier.height(16.dp))

                val verovioLink = buildAnnotatedString {
                    append("Visit ")
                    pushStringAnnotation(tag = "URL", annotation = "https://www.verovio.org")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary)) {
                        append("www.verovio.org")
                    }
                    pop()
                    append(" for details about Verovio.")
                }
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

                data.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 2.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        it.forEach { (key, value) ->
                            Row {
                                Text(fontWeight = FontWeight.Bold, text = "$key: ")
                                Text(text = value)
                            }
                        }
                    }
                }
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