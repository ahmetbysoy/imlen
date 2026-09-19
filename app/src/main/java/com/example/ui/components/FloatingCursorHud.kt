package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CharacterInspectorData
import com.example.ui.HudPositionMode
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary

/**
 * Anında ve net kontrol sağlayan Yüzen / Doğrudan İmleç Paneli.
 * Kullanıcı metne/kelimeye dokunduğu anda doğrudan SAĞ / SOL tuşları, kelime sıçrama
 * ve mikro-trackpad kaydırıcıyı en görünür ve kullanışlı boyutta sunar.
 */
@Composable
fun FloatingCursorHud(
    visible: Boolean,
    inspectorData: CharacterInspectorData,
    expandSelectionMode: Boolean,
    hudPositionMode: HudPositionMode,
    hapticEnabled: Boolean,
    onStepLeft: () -> Unit,
    onStepRight: () -> Unit,
    onWordLeft: () -> Unit,
    onWordRight: () -> Unit,
    onScrubberDrag: (Float) -> Boolean,
    onScrubberRelease: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onSelectWord: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isDraggingScrubber by remember { mutableStateOf(false) }

    fun triggerHaptic(type: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (hapticEnabled) view.performHapticFeedback(type)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = if (expandSelectionMode) {
                        listOf(AmberSecondary, CyanPrimary, AmberSecondary)
                    } else {
                        listOf(CyanPrimary, MaterialTheme.colorScheme.primary, CyanPrimary)
                    }
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .shadow(16.dp, shape = RoundedCornerShape(20.dp))
                .testTag("floating_cursor_hud")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // ROW 1: PRIMARY PROMINENT STEPPING BUTTONS (DIRECT ACCESS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // JUMP WORD LEFT (⏮)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        onClick = {
                            triggerHaptic()
                            onWordLeft()
                        },
                        modifier = Modifier
                            .size(width = 54.dp, height = 48.dp)
                            .testTag("btn_word_left")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Kelime Sol",
                                tint = CyanPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Kelime",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // STEP 1 CHAR LEFT - HUGE HERO BUTTON (◀ SOL 1)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(2.dp, CyanPrimary),
                        onClick = {
                            triggerHaptic()
                            onStepLeft()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("btn_step_left")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "1 Karakter Sol",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SOL",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = " 1",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // STEP 1 CHAR RIGHT - HUGE HERO BUTTON (1 SAĞ ▶)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(2.dp, CyanPrimary),
                        onClick = {
                            triggerHaptic()
                            onStepRight()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("btn_step_right")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = "1 ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyanPrimary
                            )
                            Text(
                                text = "SAĞ",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "1 Karakter Sağ",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // JUMP WORD RIGHT (⏭)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        onClick = {
                            triggerHaptic()
                            onWordRight()
                        },
                        modifier = Modifier
                            .size(width = 54.dp, height = 48.dp)
                            .testTag("btn_word_right")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Kelime Sağ",
                                tint = CyanPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Kelime",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ROW 2: SCRUBBER TRACKPAD + QUICK ACTIONS (WORD SELECT, SELECTION TOGGLE, CLOSE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Touch Scrubber Trackpad
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDraggingScrubber) CyanPrimary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDraggingScrubber) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDraggingScrubber = true },
                                    onDragEnd = {
                                        isDraggingScrubber = false
                                        onScrubberRelease()
                                    },
                                    onDragCancel = {
                                        isDraggingScrubber = false
                                        onScrubberRelease()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val stepped = onScrubberDrag(dragAmount.x)
                                        if (stepped && hapticEnabled) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                    }
                                )
                            }
                            .testTag("scrubber_trackpad")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = null,
                                tint = if (isDraggingScrubber) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDraggingScrubber) "◄ Hassas Kayıyor ►" else "◄ Parmağınla Kaydır ►",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isDraggingScrubber) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isDraggingScrubber) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Select Current Word Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        onClick = {
                            triggerHaptic()
                            onSelectWord()
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("quick_select_word_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Kelimeyi Seç",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Toggle Harf Harf Seçim Modu
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (expandSelectionMode) AmberSecondary else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (expandSelectionMode) AmberSecondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            triggerHaptic()
                            onToggleSelectionMode()
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("mode_toggle_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (expandSelectionMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (expandSelectionMode) "Seçim Aktif" else "Seçim Modu",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (expandSelectionMode) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Close Mini Bar
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Gizle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
