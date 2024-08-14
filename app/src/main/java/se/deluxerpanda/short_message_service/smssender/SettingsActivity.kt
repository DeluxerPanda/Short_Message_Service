package se.deluxerpanda.short_message_service.smssender

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.ui.theme.AppTheme


class SettingsActivity  : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "screen1") {
                    composable("screen1") { entry ->

                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            text = stringResource(id = R.string.app_settings_name),
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp,
                                        )
                                    },
                                    navigationIcon = {

                                        IconButton(onClick = {
                                                finish()
                                        }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                                contentDescription = "Save button"
                                            )
                                        }
                                    },



                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->
                            val context = LocalContext.current
                            val areNotificationsEnabled = remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                              //  NotificationManagerCompat.from(context).activeNotifications
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Enable Notifications", modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = areNotificationsEnabled.value,
                                        onCheckedChange = { areNotificationsEnabled.value = it

                                            val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putBoolean("notifications_enabled", true)
                                                apply()
                                            }
                                        }


                                    )
                                }


                                Spacer(modifier = Modifier.weight(1f))

                                ClickableText(
                                    text = AnnotatedString("Github"),
                                    onClick = {
                                        // Handle link click
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),  // Updated for Material 3
                                    modifier = Modifier
                                        .padding(vertical = 16.dp)
                                        .align(Alignment.CenterHorizontally)
                                )

                                val pInfo: PackageInfo = getPackageManager()
                                    .getPackageInfo(getPackageName(), 0)
                                val version = pInfo.versionName
                                Text(
                                    text = "Version: $version",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}





