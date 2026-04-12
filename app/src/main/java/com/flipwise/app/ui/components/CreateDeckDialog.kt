package com.flipwise.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt

@Composable
fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, subject: String, color: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val colors = listOf(
        "#7C3AED", "#F97316", "#10B981", "#EF4444",
        "#FBBF24", "#3B82F6", "#8B5CF6", "#F472B6"
    )
    var selectedColor by remember { mutableStateOf(colors[0]) }
    val defaultIcon = "📚"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Deck Name", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g., Spanish Vocabulary", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9FB),
                        unfocusedContainerColor = Color(0xFFF9F9FB),
                        disabledContainerColor = Color(0xFFF9F9FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Deck Color", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(Modifier.height(12.dp))
                
                // Color Grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colors.take(4).forEach { hex ->
                            ColorOption(
                                hex = hex,
                                isSelected = selectedColor == hex,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedColor = hex }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colors.drop(4).forEach { hex ->
                            ColorOption(
                                hex = hex,
                                isSelected = selectedColor == hex,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedColor = hex }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Preview Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F1F4)),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(selectedColor.toColorInt()), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(defaultIcon, fontSize = 28.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = name.ifBlank { "Deck Name" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = { onCreate(name, "", selectedColor, defaultIcon) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF7C3AED).copy(alpha = 0.5f)
                    ),
                    enabled = name.isNotBlank()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create Deck", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    hex: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 3.dp,
                color = if (isSelected) Color(0xFF7C3AED) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(hex.toColorInt()))
            .clickable { onClick() }
    )
}
