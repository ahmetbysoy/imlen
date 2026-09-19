package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CaseTransformMode
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.AmberSecondary

@Composable
fun DockedCursorBar(
    expandSelectionMode: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    hapticEnabled: Boolean,
    onStepLeft: () -> Unit,
    onStepRight: () -> Unit,
    onWordLeft: () -> Unit,
    onWordRight: () -> Unit,
    onLineStart: () -> Unit,
    onLineEnd: () -> Unit,
    onDocStart: () -> Unit,
    onDocEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteBefore: () -> Unit,
    onDeleteAfter: () -> Unit,
    onWrap: (String, String) -> Unit,
    onTransformCase: (CaseTransformMode) -> Unit,
    onDuplicate: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scrollState1 = rememberScrollState()
    val scrollState2 = rememberScrollState()

    fun haptic() {
        if (hapticEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("docked_cursor_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            // Row 1: Primary Stepping Bar (Hero Left/Right & Fast Navigation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState1)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Word Left (⏮)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    onClick = { haptic(); onWordLeft() },
                    modifier = Modifier.height(38.dp).testTag("btn_dock_word_left")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Kelime Sol",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Kelime Sol",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Step 1 Left (◀ SOL 1)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary),
                    onClick = { haptic(); onStepLeft() },
                    modifier = Modifier.height(40.dp).testTag("btn_dock_step_left")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Sol",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SOL",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = " 1",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }
                }

                // Step 1 Right (1 SAĞ ▶)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary),
                    onClick = { haptic(); onStepRight() },
                    modifier = Modifier.height(40.dp).testTag("btn_dock_step_right")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "1 ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "SAĞ",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Sağ",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Word Right (⏭)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    onClick = { haptic(); onWordRight() },
                    modifier = Modifier.height(38.dp).testTag("btn_dock_word_right")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Kelime Sağ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Kelime Sağ",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Line Start
                IconButton(
                    onClick = { haptic(); onLineStart() },
                    modifier = Modifier.size(38.dp).testTag("btn_line_start")
                ) {
                    Icon(
                        imageVector = Icons.Default.FirstPage,
                        contentDescription = "Satır Başı",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Line End
                IconButton(
                    onClick = { haptic(); onLineEnd() },
                    modifier = Modifier.size(38.dp).testTag("btn_line_end")
                ) {
                    Icon(
                        imageVector = Icons.Default.LastPage,
                        contentDescription = "Satır Sonu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Doc Start
                IconButton(
                    onClick = { haptic(); onDocStart() },
                    modifier = Modifier.size(38.dp).testTag("btn_doc_start")
                ) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignTop,
                        contentDescription = "Belge Başı",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Doc End
                IconButton(
                    onClick = { haptic(); onDocEnd() },
                    modifier = Modifier.size(38.dp).testTag("btn_doc_end")
                ) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignBottom,
                        contentDescription = "Belge Sonu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Backwards (Backspace)
                IconButton(
                    onClick = { haptic(); onDeleteBefore() },
                    modifier = Modifier.size(38.dp).testTag("btn_delete_before")
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Geri Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Forwards (Delete)
                IconButton(
                    onClick = { haptic(); onDeleteAfter() },
                    modifier = Modifier.size(38.dp).testTag("btn_delete_after")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "İleri Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Inspector Trigger
                IconButton(
                    onClick = { haptic(); onOpenInspector() },
                    modifier = Modifier.size(38.dp).testTag("btn_open_inspector")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Karakter Analizi",
                        tint = AmberSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Row 2: Secondary Quick Chips (Undo/Redo, Wrap, Brackets, Case transforms)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState2)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo
                IconButton(
                    onClick = { haptic(); onUndo() },
                    enabled = canUndo,
                    modifier = Modifier.size(32.dp).testTag("btn_undo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Geri Al",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = { haptic(); onRedo() },
                    enabled = canRedo,
                    modifier = Modifier.size(32.dp).testTag("btn_redo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Yinele",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Select All Chip
                AssistChip(
                    onClick = { haptic(); onSelectAll() },
                    label = { Text("Tümünü Seç", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.height(28.dp).testTag("chip_select_all")
                )

                // Quick Bracket Wrappers
                val brackets = listOf(
                    Pair("\"", "\""),
                    Pair("(", ")"),
                    Pair("[", "]"),
                    Pair("{", "}"),
                    Pair("'", "'"),
                    Pair("`", "`"),
                    Pair("*", "*")
                )

                brackets.forEach { (open, close) ->
                    AssistChip(
                        onClick = { haptic(); onWrap(open, close) },
                        label = { Text("$open $close", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.height(28.dp).testTag("chip_wrap_${open}")
                    )
                }

                // Case converters
                AssistChip(
                    onClick = { haptic(); onTransformCase(CaseTransformMode.UPPERCASE) },
                    label = { Text("BÜYÜK", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.height(28.dp)
                )

                AssistChip(
                    onClick = { haptic(); onTransformCase(CaseTransformMode.LOWERCASE) },
                    label = { Text("küçük", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.height(28.dp)
                )

                AssistChip(
                    onClick = { haptic(); onTransformCase(CaseTransformMode.TITLECASE) },
                    label = { Text("Baş Harfler", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.height(28.dp)
                )

                // Duplicate Line/Selection
                AssistChip(
                    onClick = { haptic(); onDuplicate() },
                    label = { Text("Çoğalt", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.height(28.dp).testTag("chip_duplicate")
                )
            }
        }
    }
}
