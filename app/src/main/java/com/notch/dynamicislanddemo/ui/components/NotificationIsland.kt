package com.notch.dynamicislanddemo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notch.dynamicislanddemo.models.NotificationActionType
import com.notch.dynamicislanddemo.models.NotificationInfo
import com.notch.dynamicislanddemo.utils.HapticFeedback
import com.notch.dynamicislanddemo.utils.pressAnimation
import kotlinx.coroutines.delay

/**
 * Notification island showing app notifications with pager support
 */
@Composable
fun NotificationIsland(
    notifications: List<NotificationInfo>,
    onAction: (NotificationAction) -> Unit,
    onDismiss: () -> Unit,
    onHeightChange: (androidx.compose.ui.unit.Dp) -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { notifications.size })
    
    val currentNotification = notifications.getOrNull(pagerState.currentPage)
    LaunchedEffect(pagerState.currentPage, currentNotification) {
        if (currentNotification != null) {
            // Dynamic height calculation
            val baseHeight = 125.dp
            val actionsHeight = if (currentNotification.actions.isNotEmpty()) 55.dp else 0.dp
            val pictureHeight = if (currentNotification.picture != null) 140.dp else 0.dp
            val textHeight = if (currentNotification.picture != null) 0.dp else 0.dp // Reduce text space if picture shown
            
            // Basic logic: Standard (125) + Actions (55) + Picture (140)
            // If picture is present, we might want a taller island overall
            var totalHeight = baseHeight + actionsHeight
            
            if (currentNotification.picture != null) {
                totalHeight += 120.dp
            }
            
            onHeightChange(totalHeight)
        }
    }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                key = { page -> notifications.getOrNull(page)?.id ?: page }
            ) { page ->
                val notification = notifications.getOrNull(page)
                if (notification != null) {
                    NotificationPage(
                        notification = notification,
                        onAction = onAction,
                        onDismiss = onDismiss
                    )
                }
            }
            
            // Pager Indicator
            if (notifications.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(notifications.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) 
                            Color.White 
                        else 
                            Color.White.copy(alpha = 0.3f)
                        
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPage(
    notification: NotificationInfo,
    onAction: (NotificationAction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: App info, close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF5E5CE6),
                                    Color(0xFF7D7AFF)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notification.appName.take(1),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${notification.appName} · ${notification.timestamp}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Close button
            IconButton(
                onClick = {
                    HapticFeedback.light(context)
                    onDismiss()
                },
                modifier = Modifier
                    .size(26.dp)
                    .background(Color(0xFF2C2C2E), CircleShape)
                    .pressAnimation()
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Content (Picture or Text)
        if (notification.picture != null) {
            // Big Picture Style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                // Placeholder for picture
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.Center
                ) {
                     Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Overlay text
                Column(
                     modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if(notification.subText.isNotEmpty()) notification.subText else notification.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                 }
            }
        } else {
             // Standard Messaging Style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Sender icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notification.title.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Title and message
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (notification.subText.isNotEmpty()) {
                             Text(
                                text = " • ${notification.subText}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = if (notification.bigText.isNotEmpty()) notification.bigText else notification.message,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Bottom row: Action buttons
        if (notification.actions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                notification.actions.forEach { action ->
                    Button(
                        onClick = {
                            HapticFeedback.light(context)
                            onAction(NotificationAction(action.type, action.label))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3A3A3C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .pressAnimation()
                    ) {
                        Text(
                            text = action.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
             Spacer(modifier = Modifier.height(36.dp)) // Maintain height consistency
        }
    }
}

/**
 * Notification action data class
 */
data class NotificationAction(
    val type: NotificationActionType,
    val label: String
)
