package com.nexora.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.nexora.app.ui.viewmodel.AuthViewModel
import com.nexora.app.util.CountryDialCode
import com.nexora.app.util.CountryDialCodes
import com.nexora.app.util.buildInternationalPhone

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var selectedCountry by remember { mutableStateOf(CountryDialCodes.defaultMexico) }
    var countrySearch by remember { mutableStateOf("") }
    var showCountryPicker by remember { mutableStateOf(false) }

    val hasOtp = state.verificationId != null
    BackHandler(enabled = showCountryPicker || hasOtp) {
        if (showCountryPicker) showCountryPicker = false else viewModel.resetToPhoneEntry()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showCountryPicker) {
                CountryPickerPanel(
                    search = countrySearch,
                    onSearchChange = { countrySearch = it },
                    onClose = { showCountryPicker = false },
                    onSelect = { country ->
                        selectedCountry = country
                        countrySearch = ""
                        showCountryPicker = false
                        viewModel.updatePhone(buildInternationalPhone(country, state.phone))
                    },
                )
            } else if (hasOtp) {
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
            } else {
                PhoneStep(
                    country = selectedCountry,
                    phone = state.phone,
                    normalizedPhone = state.normalizedPhone,
                    loading = state.loading,
                    error = state.error,
                    activity = activity,
                    onCountryClick = { showCountryPicker = true },
                    onPhoneChange = { raw -> viewModel.updatePhone(buildInternationalPhone(selectedCountry, raw)) },
                    onRequestOtp = { activity?.let(viewModel::requestOtp) },
                )
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        BrandHeader()
        Spacer(Modifier.height(28.dp))
        Card(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Text("Verifica tu teléfono", fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(
                    "Elige tu país y escribe solo tu número. Nexora genera la lada internacional automáticamente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable(onClick = onCountryClick),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(country.flag)
                            Text(country.dialCode, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    OutlinedTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        label = { Text("Número telefónico") },
                        placeholder = { Text("6681234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (normalizedPhone.isNotBlank()) {
                    Text("Formato final: $normalizedPhone", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Button(
                    enabled = !loading && activity != null,
                    onClick = onRequestOtp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) { Text("Continuar") }
                if (loading) CircularProgressIndicator()
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Text("←", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
            Column {
                Text("Código de verificación", fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ingresa el código SMS", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Por seguridad, el código se captura en una pantalla separada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = { Text("Código de 6 dígitos") },
                    placeholder = { Text("123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    enabled = !loading,
                    onClick = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) { Text("Verificar y entrar") }
                Text("Cambiar número", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
                if (previewMode) Text("Preview: usa 123456 para entrar sin servidor.", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                if (loading) CircularProgressIndicator()
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Selecciona tu país", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Cerrar", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onClose))
            }
            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                label = { Text("Buscar país o lada") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(modifier = Modifier.height(430.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(countries, key = { it.iso + it.dialCode }) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country) }
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(country.flag, fontSize = 26.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(country.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(country.iso, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Text(country.dialCode, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
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
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 10.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("N", fontSize = 38.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("Nexora Messenger", fontSize = 32.sp, fontWeight = FontWeight.Black)
    Text("Mensajería privada para tu propio servidor", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
