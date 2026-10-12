package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwitchAccount
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatFilter
import com.example.ui.NavigationTab
import com.example.ui.theme.MetaAiBlue
import com.example.ui.theme.MetaAiCyan
import com.example.ui.theme.MetaAiPurple
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppTopBar(
    currentTab: NavigationTab,
    selectedFilter: ChatFilter,
    onFilterSelected: (ChatFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMetaAiClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onNewAccountClick: () -> Unit,
    onLockedChatsClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main App Header or Search Bar
            if (isSearchExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Pesquisar ou perguntar à Meta AI...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Pesquisar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    onSearchQueryChange("")
                                } else {
                                    isSearchExpanded = false
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar busca")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_input")
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Verum Brand Title
                    Text(
                        text = when (currentTab) {
                            NavigationTab.CHATS -> "Verum"
                            NavigationTab.UPDATES -> "Atualizações"
                            NavigationTab.COMMUNITIES -> "Comunidades"
                            NavigationTab.CALLS -> "Chamadas"
                        },
                        color = WhatsAppGreen,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.testTag("app_title")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Camera icon
                        IconButton(
                            onClick = onCameraClick,
                            modifier = Modifier.testTag("camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Câmera",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Meta AI glowing ring button
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(MetaAiBlue, MetaAiPurple, MetaAiCyan, MetaAiBlue)
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { onMetaAiClick() }
                                .testTag("meta_ai_ring_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(MetaAiBlue.copy(alpha = 0.3f), MetaAiCyan.copy(alpha = 0.4f))
                                        )
                                    )
                            )
                        }

                        // Search icon
                        IconButton(
                            onClick = { isSearchExpanded = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Pesquisar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Overflow menu
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("menu_more_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Mais opções",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Meu Perfil") },
                                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onProfileClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Alternar Conta") },
                                    leadingIcon = { Icon(Icons.Outlined.SwitchAccount, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onAccountsClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cadastrar Nova Conta") },
                                    leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onNewAccountClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Conversas Bloqueadas") },
                                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onLockedChatsClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Novo grupo") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Aparelhos conectados") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mensagens favoritas") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Configurações") },
                                    onClick = {
                                        showMenu = false
                                        onProfileClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Chat Filters (Only shown in Chats tab)
            AnimatedVisibility(
                visible = currentTab == NavigationTab.CHATS && !isSearchExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        ChatFilter.ALL to "Tudo",
                        ChatFilter.UNREAD to "Não lidas",
                        ChatFilter.FAVORITES to "Favoritos",
                        ChatFilter.GROUPS to "Grupos"
                    )

                    items(filters) { (filter, label) ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterSelected(filter) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsAppGreen.copy(alpha = 0.18f),
                                selectedLabelColor = WhatsAppGreen,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = null,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("filter_chip_${label.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}
