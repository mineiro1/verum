package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab
import com.example.ui.theme.WhatsAppGreen

@Composable
fun WhatsAppBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    unreadChatsCount: Int = 2
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        // Conversas (Chats)
        NavigationBarItem(
            selected = currentTab == NavigationTab.CHATS,
            onClick = { onTabSelected(NavigationTab.CHATS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadChatsCount > 0) {
                            Badge(
                                containerColor = WhatsAppGreen,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = unreadChatsCount.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == NavigationTab.CHATS) Icons.Filled.Chat else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Conversas",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Conversas",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == NavigationTab.CHATS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = WhatsAppGreen,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_chats")
        )

        // Atualizações (Updates / Status & Channels)
        NavigationBarItem(
            selected = currentTab == NavigationTab.UPDATES,
            onClick = { onTabSelected(NavigationTab.UPDATES) },
            icon = {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = WhatsAppGreen,
                            modifier = Modifier.size(6.dp)
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == NavigationTab.UPDATES) Icons.Filled.Update else Icons.Outlined.Update,
                        contentDescription = "Atualizações",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Atualizações",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == NavigationTab.UPDATES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = WhatsAppGreen,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_updates")
        )

        // Comunidades (Communities)
        NavigationBarItem(
            selected = currentTab == NavigationTab.COMMUNITIES,
            onClick = { onTabSelected(NavigationTab.COMMUNITIES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == NavigationTab.COMMUNITIES) Icons.Filled.Groups else Icons.Outlined.Groups,
                    contentDescription = "Comunidades",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Comunidades",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == NavigationTab.COMMUNITIES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = WhatsAppGreen,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_communities")
        )

        // Chamadas (Calls)
        NavigationBarItem(
            selected = currentTab == NavigationTab.CALLS,
            onClick = { onTabSelected(NavigationTab.CALLS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == NavigationTab.CALLS) Icons.Filled.Call else Icons.Outlined.Call,
                    contentDescription = "Chamadas",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Chamadas",
                    fontSize = 12.sp,
                    fontWeight = if (currentTab == NavigationTab.CALLS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhatsAppGreen,
                selectedTextColor = WhatsAppGreen,
                indicatorColor = WhatsAppGreen.copy(alpha = 0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_calls")
        )
    }
}
