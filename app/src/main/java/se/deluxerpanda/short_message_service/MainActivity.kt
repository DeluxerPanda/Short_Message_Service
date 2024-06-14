package se.deluxerpanda.short_message_service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.deluxerpanda.short_message_service.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.padding(50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth() // Match parent width
                        )

                    }
                    Column(
                        modifier = Modifier.padding(150.dp),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Row() {
                        TextButton(
                            onClick = {
                                val intent = Intent(
                                this@MainActivity,
                                MainActivity::class.java)
                             startActivity(intent) },
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_save),
                                    contentDescription = "Send scheduled messages"
                                )

                                Text(text = "Scheduled")
                            }
                        }

                    TextButton(
                        onClick = {
                            val intent = Intent(
                                this@MainActivity,
                                MainActivity::class.java)
                            startActivity(intent) },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save),
                                contentDescription = "Send scheduled messages"
                            )

                            Text(text = "One time")
                        }
                    }
                        }





                        Row() {

                    TextButton(
                        onClick = {
                            val intent = Intent(
                                this@MainActivity,
                                MainActivity::class.java)
                            startActivity(intent) },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save),
                                contentDescription = "Send scheduled messages"
                            )

                            Text(text = "Scheduled")
                        }
                    }

                    TextButton(
                        onClick = {
                            val intent = Intent(
                                this@MainActivity,
                                MainActivity::class.java)
                            startActivity(intent) },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save),
                                contentDescription = "Send scheduled messages"
                            )

                            Text(text = "One time")
                        }
                    }
                    }
                    }
                }
            }
        }
    }
}
