package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PollData
import com.example.ui.theme.WhatsAppGreen

@Composable
fun PollCard(
    messageId: String,
    pollData: PollData,
    onVoteOption: (optionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = pollData.options.sumOf { it.votes }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .testTag("poll_card_$messageId")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Poll,
                contentDescription = null,
                tint = WhatsAppGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = pollData.question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (pollData.allowMultipleAnswers) "Selecione uma ou mais opções" else "Selecione uma opção",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Options
        pollData.options.forEach { option ->
            val hasVoted = option.voterIds.contains("user_me")
            val percentage = if (totalVotes > 0) (option.votes.toFloat() / totalVotes.toFloat()) else 0f
            val animatedProgress by animateFloatAsState(targetValue = percentage, label = "progress")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { onVoteOption(option.id) }
            ) {
                // Background fill progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .matchParentSize()
                        .background(
                            if (hasVoted) WhatsAppGreen.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                        )
                )

                // Option content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Checkbox / Radio indicator
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(if (pollData.allowMultipleAnswers) RoundedCornerShape(4.dp) else CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (hasVoted) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = if (pollData.allowMultipleAnswers) RoundedCornerShape(4.dp) else CircleShape
                                )
                                .background(if (hasVoted) WhatsAppGreen else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasVoted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Votado",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = option.text,
                            fontSize = 14.sp,
                            fontWeight = if (hasVoted) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Votes count
                    Text(
                        text = "${option.votes} votos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Total: $totalVotes votos",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
