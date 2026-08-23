package net.kdt.pojavlaunch.kotlin.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kdt.pojavlaunch.Logger
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

enum class LogSeverity(val color: Color) {
    INFO(Color(0xFF23A05D)),
    WARN(Color(0xFFFFB347)),
    ERROR(Color(0xFFFF5555)),
    DEBUG(Color(0xFFBEBEBE))
}

data class LogLine(
    val text: String,
    val severity: LogSeverity
)

fun parseSeverity(text: String): LogSeverity {
    val upper = text.uppercase()
    return when {
        "[ERROR]" in upper || "[FATAL]" in upper || "ERROR:" in upper -> LogSeverity.ERROR
        "[WARN]" in upper || "WARN:" in upper -> LogSeverity.WARN
        "[INFO]" in upper || "INFO:" in upper -> LogSeverity.INFO
        else -> LogSeverity.DEBUG
    }
}

enum class LogFilter(val label: String) {
    ALL("All"),
    INFO("Info"),
    WARN("Warn"),
    ERROR("Error");
}

private val obsidianBlack = Color(0xFF0D1117)
private val headerGrey = Color(0xFF24242C)
private val crimsonRed = Color(0xFFDC2626)
private val greenColor = Color(0xFF00CC44)
private val yellowColor = Color(0xFFFFC300)
private val redColor = Color(0xFFFF5F56)
private val mutedGrey = Color(0xFF6A737D)
private val textGrey = Color(0xFFBEBEBE)

@Composable
fun LoggerScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLogOutputEnabled by remember { mutableStateOf(true) }
    var isAutoScrollEnabled by rememberSaveable { mutableStateOf(true) }
    var activeFilter by rememberSaveable { mutableStateOf(LogFilter.ALL) }
    val logLines = remember { mutableStateListOf<LogLine>() }
    val listState = rememberLazyListState()
    val isPreview = LocalInspectionMode.current
    val hasBackground = LauncherPreferences.PREF_BACKGROUND_PATH_STATE.value != null ||
                        LauncherPreferences.PREF_BACKGROUND_VIDEO_PATH_STATE.value != null || isPreview

    DisposableEffect(isLogOutputEnabled) {
        if (isLogOutputEnabled) {
            val listener = Logger.eventLogListener { text ->
                Tools.runOnUiThread {
                    val severity = parseSeverity(text)
                    logLines.add(LogLine(text, severity))
                }
            }
            Logger.setLogListener(listener)
            onDispose {
                Logger.setLogListener(null)
            }
        } else {
            logLines.clear()
            Logger.setLogListener(null)
            onDispose {}
        }
    }

    LaunchedEffect(logLines.size) {
        if (isAutoScrollEnabled && logLines.isNotEmpty()) {
            listState.animateScrollToItem(logLines.size - 1)
        }
    }

    val filteredLines = remember(logLines, activeFilter) {
        if (activeFilter == LogFilter.ALL) {
            logLines
        } else {
            val targetSeverity = when (activeFilter) {
                LogFilter.INFO -> LogSeverity.INFO
                LogFilter.WARN -> LogSeverity.WARN
                LogFilter.ERROR -> LogSeverity.ERROR
                else -> return@remember logLines
            }
            logLines.filter { it.severity == targetSeverity }
        }
    }

    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = if (hasBackground) Color.Transparent else obsidianBlack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MacTerminalHeader(
                title = "Krox Runtime Engine Console",
                onClose = onClose,
                modifier = Modifier.height(32.dp)
            )

            FilterTabs(
                activeFilter = activeFilter,
                onFilterSelected = { activeFilter = it },
                onToggleLogCapture = { isLogOutputEnabled = !isLogOutputEnabled },
                isLogOutputEnabled = isLogOutputEnabled,
                modifier = Modifier.height(36.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF010409))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    items(filteredLines, key = { it.hashCode() }) { line ->
                        TerminalLogLine(line = line)
                    }
                }

                if (filteredLines.isEmpty() && isLogOutputEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting for output...",
                            color = mutedGrey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                } else if (!isLogOutputEnabled) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log output disabled",
                            color = mutedGrey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            TerminalToolbar(
                logLines = logLines,
                onCopyAll = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text = logLines.joinToString("\n") { it.text }
                    val clip = android.content.ClipData.newPlainText("Krox Log Output", text)
                    clipboard.setPrimaryClip(clip)
                },
                onClear = { logLines.clear() },
                isAutoScroll = isAutoScrollEnabled,
                onToggleAutoScroll = { isAutoScrollEnabled = !isAutoScrollEnabled },
                modifier = Modifier.height(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MacTerminalHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotSize = 12.dp
    val dotSpacing = 8.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(headerGrey),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(80.dp),
            horizontalArrangement = Arrangement.spacedBy(dotSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(redColor)
            ) {}
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(yellowColor)
            ) {}
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(greenColor)
            ) {}
        }

        Text(
            text = title,
            color = textGrey,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = textGrey,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun FilterTabs(
    activeFilter: LogFilter,
    onFilterSelected: (LogFilter) -> Unit,
    onToggleLogCapture: () -> Unit,
    isLogOutputEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val allFilters = LogFilter.entries.toTypedArray()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(headerGrey),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        allFilters.forEach { filter ->
            val isSelected = activeFilter == filter
            val tint = if (isSelected) crimsonRed else mutedGrey
            Text(
                text = filter.label,
                color = tint,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) Color(0xFF2A2A3A) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { onFilterSelected(filter) }
                    }
            )
            if (filter != allFilters.last()) {
                Text(
                    text = "|",
                    color = mutedGrey,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onToggleLogCapture,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isLogOutputEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (isLogOutputEnabled) "Disable log capture" else "Enable log capture",
                tint = if (isLogOutputEnabled) greenColor else mutedGrey,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TerminalLogLine(line: LogLine) {
    val color = line.severity.color
    Text(
        text = line.text,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TerminalToolbar(
    logLines: List<LogLine>,
    onCopyAll: () -> Unit,
    onClear: () -> Unit,
    isAutoScroll: Boolean,
    onToggleAutoScroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(headerGrey),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onCopyAll,
            enabled = logLines.isNotEmpty(),
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy all logs",
                tint = if (logLines.isNotEmpty()) textGrey else mutedGrey,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "Copy",
            color = if (logLines.isNotEmpty()) textGrey else mutedGrey,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        IconButton(
            onClick = onClear,
            enabled = logLines.isNotEmpty(),
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear logs",
                tint = if (logLines.isNotEmpty()) textGrey else mutedGrey,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "Clear",
            color = if (logLines.isNotEmpty()) textGrey else mutedGrey,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = if (isAutoScroll) "AUTO" else "PAUSED",
            color = if (isAutoScroll) greenColor else crimsonRed,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(end = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures { onToggleAutoScroll() }
                }
        )
    }
}
