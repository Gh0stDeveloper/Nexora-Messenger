package com.nexora.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.ui.theme.NexoraColors
import com.nexora.app.ui.viewmodel.AuthViewModel
import com.nexora.app.util.CountryDialCode
import com.nexora.app.util.CountryDialCodes
import com.nexora.app.util.buildInternationalPhone
import com.nexora.app.util.nationalPhoneInput

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var selectedCountry by remember { mutableStateOf(CountryDialCodes.defaultMexico) }
    var countrySearch by remember { mutableStateOf("") }
    var showCountryPicker by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }

    val hasOtp = state.verificationId != null
    BackHandler(enabled = showCountryPicker || hasOtp) {
        if (showCountryPicker) {
            showCountryPicker = false
        } else {
            viewModel.resetToPhoneEntry()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = NexoraColors.Amoled,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NexoraColors.Amoled,
                            NexoraColors.Ink,
                            NexoraColors.Amoled,
                        ),
                    ),
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                showCountryPicker -> {
                    CountryPickerPanel(
                        search = countrySearch,
                        onSearchChange = { countrySearch = it },
                        onClose = { showCountryPicker = false },
                        onSelect = { country ->
                            selectedCountry = country
                            phoneInput = nationalPhoneInput(country, phoneInput)
                            countrySearch = ""
                            showCountryPicker = false
                            viewModel.updatePhone(buildInternationalPhone(country, phoneInput))
                        },
                    )
                }

                hasOtp -> {
                    OtpStep(
                        phone = state.normalizedPhone,
                        code = state.code,
                        loading = state.loading,
                        error = state.error,
                        previewMode = state.previewMode,
                        onBack = viewModel::resetToPhoneEntry,
                        onCodeChange = viewModel::updateCode,
                        onVerify = viewModel::verifyCode,
                    )
                }

                else -> {
                    PhoneStep(
                        country = selectedCountry,
                        phone = phoneInput,
                        normalizedPhone = state.normalizedPhone,
                        loading = state.loading,
                        error = state.error,
                        activity = activity,
                        onCountryClick = { showCountryPicker = true },
                        onPhoneChange = { raw ->
                            phoneInput = nationalPhoneInput(selectedCountry, raw)
                            viewModel.updatePhone(buildInternationalPhone(selectedCountry, phoneInput))
                        },
                        onRequestOtp = { activity?.let(viewModel::requestOtp) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneStep(
    country: CountryDialCode,
    phone: String,
    normalizedPhone: String,
    loading: Boolean,
    error: String?,
    activity: Activity?,
    onCountryClick: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandHeader()
        Spacer(Modifier.height(26.dp))

        PremiumAuthCard {
            Text(
                "Verifica tu teléfono",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
            )
            Text(
                "Selecciona el país y escribe tu número nacional. Nexora construye el formato internacional automáticamente.",
                color = NexoraColors.TextMuted,
                lineHeight = 20.sp,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = NexoraColors.Glass,
                    border = BorderStroke(1.dp, NexoraColors.Stroke),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable(onClick = onCountryClick),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            country.iso,
                            fontWeight = FontWeight.Black,
                            color = NexoraColors.MintSoft,
                            fontSize = 12.sp,
                        )
                        Text(
                            country.dialCode,
                            fontWeight = FontWeight.Bold,
                            color = NexoraColors.TextMain,
                            fontSize = 13.sp,
                        )
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Número nacional") },
                    placeholder = {
                        Text(if (country.iso == "MX") "6681234567" else "Número local")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                )
            }

            if (normalizedPhone.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NexoraColors.Primary.copy(alpha = 0.10f),
                ) {
                    Text(
                        "Formato final: $normalizedPhone",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = NexoraColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }
            }

            Button(
                enabled = !loading && activity != null && normalizedPhone.isNotBlank(),
                onClick = onRequestOtp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Continuar")
            }

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = NexoraColors.Primary,
                )
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun OtpStep(
    phone: String,
    code: String,
    loading: Boolean,
    error: String?,
    previewMode: Boolean,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = NexoraColors.GlassHigh,
                border = BorderStroke(1.dp, NexoraColors.Stroke),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Cambiar número",
                        tint = NexoraColors.TextMain,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    "Código de verificación",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(phone, color = NexoraColors.TextMuted)
            }
        }

        Spacer(Modifier.height(22.dp))

        PremiumAuthCard {
            Text(
                "Ingresa el código SMS",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Text(
                "La validación se realiza en una pantalla separada para mantener el flujo limpio.",
                color = NexoraColors.TextMuted,
            )

            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("Código de 6 dígitos") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )

            Button(
                enabled = !loading && code.length >= 6,
                onClick = onVerify,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Verificar y entrar")
            }

            Text(
                "Cambiar número",
                color = NexoraColors.Primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBack),
            )

            if (previewMode) {
                Text(
                    "Preview local: usa 123456 para entrar sin servidor.",
                    color = NexoraColors.Primary,
                    fontSize = 12.sp,
                )
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = NexoraColors.Primary,
                )
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CountryPickerPanel(
    search: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit,
    onSelect: (CountryDialCode) -> Unit,
) {
    val countries = CountryDialCodes.search(search)

    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh),
        border = BorderStroke(1.dp, NexoraColors.Stroke),
    ) {
        Column(
            Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Selecciona tu país",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = NexoraColors.TextMuted,
                    )
                }
            }

            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                label = { Text("Buscar país o lada") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            )

            LazyColumn(
                modifier = Modifier.height(430.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(countries, key = { it.iso + it.dialCode }) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country) }
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = NexoraColors.Glass,
                            border = BorderStroke(1.dp, NexoraColors.Stroke),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    country.iso,
                                    fontWeight = FontWeight.Black,
                                    color = NexoraColors.MintSoft,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                country.name,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                country.iso,
                                color = NexoraColors.TextMuted,
                                fontSize = 12.sp,
                            )
                        }
                        Text(
                            country.dialCode,
                            color = NexoraColors.Primary,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Surface(
        modifier = Modifier.size(74.dp),
        shape = CircleShape,
        color = NexoraColors.PrimaryDeep,
        border = BorderStroke(1.dp, NexoraColors.Primary.copy(alpha = 0.45f)),
        shadowElevation = 12.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "N",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = NexoraColors.TextMain,
            )
        }
    }
    Spacer(Modifier.height(17.dp))
    Text(
        "Nexora Messenger",
        fontSize = 31.sp,
        fontWeight = FontWeight.Black,
    )
    Spacer(Modifier.height(5.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = NexoraColors.Primary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            "Privado, local-first y preparado para tu relay",
            color = NexoraColors.TextMuted,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun PremiumAuthCard(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh),
        border = BorderStroke(1.dp, NexoraColors.Stroke),
    ) {
        Column(
            modifier = Modifier.padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
