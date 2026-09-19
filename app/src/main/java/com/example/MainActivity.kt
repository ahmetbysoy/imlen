package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HudPositionMode
import com.example.ui.PrecisionCursorUiState
import com.example.ui.PrecisionCursorViewModel
import com.example.ui.components.CharacterMatrixInspector
import com.example.ui.components.CursorSettingsDialog
import com.example.ui.components.DockedCursorBar
import com.example.ui.components.DocumentDrawerOrList
import com.example.ui.components.FloatingCursorHud
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PrecisionCursorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecisionCursorApp() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: PrecisionCursorViewModel = viewModel(
        factory = PrecisionCursorViewModel.provideFactory(application)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allDocs by viewModel.allDocuments.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PrecisionCursorTopBar(
                activeTitle = uiState.activeDocument?.title ?: "Hassas Metin Düzenleyici",
                onOpenDocs = { viewModel.toggleDocumentDrawer(true) },
                onOpenInspector = { viewModel.toggleInspectorSheet(true) },
                onOpenSettings = { viewModel.toggleSettingsDialog(true) }
            )
        },
        bottomBar = {
            DockedCursorBar(
                expandSelectionMode = uiState.expandSelectionMode,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                hapticEnabled = uiState.hapticFeedbackEnabled,
                onStepLeft = { viewModel.stepCursor(-1) },
                onStepRight = { viewModel.stepCursor(1) },
                onWordLeft = { viewModel.jumpWord(false) },
                onWordRight = { viewModel.jumpWord(true) },
                onLineStart = { viewModel.jumpLine(false) },
                onLineEnd = { viewModel.jumpLine(true) },
                onDocStart = { viewModel.jumpDocument(false) },
                onDocEnd = { viewModel.jumpDocument(true) },
                onSelectAll = { viewModel.selectAll() },
                onClearSelection = { viewModel.clearSelection() },
                onDeleteBefore = { viewModel.deleteCharBefore() },
                onDeleteAfter = { viewModel.deleteCharAfter() },
                onWrap = { open, close -> viewModel.wrapSelection(open, close) },
                onTransformCase = { mode -> viewModel.transformCase(mode) },
                onDuplicate = { viewModel.duplicate() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onOpenInspector = { viewModel.toggleInspectorSheet(true) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Floating mini HUD positioned ABOVE editor if configured
            if (uiState.hudPositionMode == HudPositionMode.FLOATING_ABOVE) {
                FloatingCursorHud(
                    visible = uiState.isHudVisible,
                    inspectorData = uiState.inspectorData,
                    expandSelectionMode = uiState.expandSelectionMode,
                    hudPositionMode = uiState.hudPositionMode,
                    hapticEnabled = uiState.hapticFeedbackEnabled,
                    onStepLeft = { viewModel.stepCursor(-1) },
                    onStepRight = { viewModel.stepCursor(1) },
                    onWordLeft = { viewModel.jumpWord(false) },
                    onWordRight = { viewModel.jumpWord(true) },
                    onScrubberDrag = { viewModel.onScrubberDrag(it) },
                    onScrubberRelease = { viewModel.resetScrubber() },
                    onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                    onSelectWord = { viewModel.selectCurrentWord() },
                    onDismiss = { viewModel.setHudVisibility(false) }
                )
            }

            // Selection Mode Indicator Banner
            AnimatedVisibility(
                visible = uiState.expandSelectionMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = AmberSecondary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(AmberSecondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hassas Seçim Modu Açık: Adım butonları veya kaydırıcı ile harf harf seçin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberSecondary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleSelectionMode() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Seçimi Tamamla",
                                tint = AmberSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Main Precision Text Editor Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = uiState.textFieldValue,
                    onValueChange = { viewModel.onTextChanged(it) },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        cursorColor = CyanPrimary,
                        selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
                            handleColor = CyanPrimary,
                            backgroundColor = CyanPrimary.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_text_editor")
                )
            }

            // Floating mini HUD positioned BELOW editor if configured
            if (uiState.hudPositionMode == HudPositionMode.FLOATING_BELOW) {
                FloatingCursorHud(
                    visible = uiState.isHudVisible,
                    inspectorData = uiState.inspectorData,
                    expandSelectionMode = uiState.expandSelectionMode,
                    hudPositionMode = uiState.hudPositionMode,
                    hapticEnabled = uiState.hapticFeedbackEnabled,
                    onStepLeft = { viewModel.stepCursor(-1) },
                    onStepRight = { viewModel.stepCursor(1) },
                    onWordLeft = { viewModel.jumpWord(false) },
                    onWordRight = { viewModel.jumpWord(true) },
                    onScrubberDrag = { viewModel.onScrubberDrag(it) },
                    onScrubberRelease = { viewModel.resetScrubber() },
                    onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                    onSelectWord = { viewModel.selectCurrentWord() },
                    onDismiss = { viewModel.setHudVisibility(false) }
                )
            }
        }
    }

    // Modal Bottom Sheets and Dialogs
    if (uiState.showInspectorSheet) {
        CharacterMatrixInspector(
            inspectorData = uiState.inspectorData,
            onDismiss = { viewModel.toggleInspectorSheet(false) }
        )
    }

    if (uiState.showDocumentDrawer) {
        DocumentDrawerOrList(
            documents = allDocs,
            activeDocument = uiState.activeDocument,
            onSelectDocument = { viewModel.loadDocument(it) },
            onSaveCurrent = { title, cat -> viewModel.saveCurrentDocument(title, cat) },
            onCreateNew = { title, cat -> viewModel.createNewDocument(title, cat) },
            onDeleteDocument = { viewModel.deleteDocument(it) },
            onDismiss = { viewModel.toggleDocumentDrawer(false) }
        )
    }

    if (uiState.showSettingsDialog) {
        CursorSettingsDialog(
            currentHudMode = uiState.hudPositionMode,
            currentSensitivity = uiState.scrubberSensitivity,
            hapticEnabled = uiState.hapticFeedbackEnabled,
            onSelectHudMode = { viewModel.setHudPositionMode(it) },
            onSelectSensitivity = { viewModel.setScrubberSensitivity(it) },
            onToggleHaptic = { viewModel.toggleHapticFeedback(it) },
            onDismiss = { viewModel.toggleSettingsDialog(false) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecisionCursorTopBar(
    activeTitle: String,
    onOpenDocs: () -> Unit,
    onOpenInspector: () -> Unit,
    onOpenSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Precision Cursor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = activeTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanPrimary,
                    maxLines = 1
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenInspector,
                modifier = Modifier.testTag("topbar_btn_inspector")
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analiz & Koordinatlar",
                    tint = AmberSecondary
                )
            }
            IconButton(
                onClick = onOpenDocs,
                modifier = Modifier.testTag("topbar_btn_documents")
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Belgeler",
                    tint = CyanPrimary
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("topbar_btn_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ayarlar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.testTag("app_topbar")
    )
}
