package com.nexora.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nexora.app.ui.theme.NexoraColors
import com.nexora.app.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileSetupScreen(viewModel: ProfileViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.updateAvatar(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NexoraColors.Amoled, NexoraColors.Ink, NexoraColors.Amoled),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh),
            border = BorderStroke(1.dp, NexoraColors.Stroke),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Crea tu perfil",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Configura el nombre y la foto que verán tus contactos.",
                    color = NexoraColors.TextMuted,
                )

                Surface(
                    modifier = Modifier.size(104.dp),
                    shape = CircleShape,
                    color = NexoraColors.PrimaryDeep,
                    border = BorderStroke(2.dp, NexoraColors.Primary.copy(alpha = 0.55f)),
                ) {
                    if (state.avatarUri == null) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(44.dp),
                                tint = NexoraColors.TextMain,
                            )
                        }
                    } else {
                        AsyncImage(
                            model = state.avatarUri,
                            contentDescription = "Avatar seleccionado",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    }
                }

                OutlinedButton(
                    enabled = !state.loading,
                    onClick = { picker.launch("image/*") },
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(if (state.avatarUri == null) "Elegir foto" else "Cambiar foto")
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Nombre de usuario") },
                    supportingText = { Text("Entre 2 y 80 caracteres") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                )

                Button(
                    enabled = !state.loading && state.name.trim().length >= 2,
                    onClick = viewModel::completeProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Guardar y entrar")
                }

                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = NexoraColors.Primary,
                    )
                }

                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = NexoraColors.Glass,
                    border = BorderStroke(1.dp, NexoraColors.Stroke),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Tu perfil queda preparado para el flujo privado de Nexora.",
                            color = NexoraColors.TextMuted,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
