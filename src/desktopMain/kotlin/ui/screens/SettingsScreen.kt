package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.repository.FinanceRepository
import ui.components.*
import ui.theme.ShadcnTheme
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    repository: FinanceRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var importJsonText by remember { mutableStateOf("") }
    var showImportField by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "설정 및 백업 (Settings & Backup)",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "로컬 SQLite 데이터베이스 관리, 통화 설정 및 테마 변경",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 13.sp
                )
            }
        }

        // Appearance & Currency
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "화면 및 통화 설정 (Display & Currency)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Theme toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "테마 모드 (Theme)", color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = if (isDarkTheme) "다크 모드 (Dark Zinc)" else "라이트 모드 (Light Slate)", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                        }

                        ShadcnButtonText(
                            text = if (isDarkTheme) "☀️ 라이트 모드로 전환" else "🌙 다크 모드로 전환",
                            variant = ButtonVariant.OUTLINE,
                            onClick = onToggleDarkTheme
                        )
                    }

                    // Currency (Korean Won ₩)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "기본 통화 (Currency)", color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "대한민국 원 (KRW - ₩)", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                        }

                        ShadcnBadge(text = "₩ Korean Won", variant = BadgeVariant.KRW)
                    }
                }
            }
        }

        // Local SQLite Database & Backup
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "로컬 데이터베이스 및 백업 (Local Database & Backup)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val dbPath = File(System.getProperty("user.home"), ".moneymanagement/moneymanagement.db").absolutePath

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "SQLite 파일 저장 위치:", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                        Text(text = dbPath, color = ShadcnTheme.colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    if (backupStatusMessage != null) {
                        ShadcnBadge(
                            text = backupStatusMessage!!,
                            variant = BadgeVariant.SUCCESS
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShadcnButtonText(
                            text = "📤 JSON 백업 파일로 내보내기",
                            variant = ButtonVariant.PRIMARY,
                            onClick = {
                                val json = repository.exportBackupJson()
                                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                val backupFile = File(System.getProperty("user.home"), ".moneymanagement/backup_$timestamp.json")
                                backupFile.writeText(json)
                                backupStatusMessage = "백업 성공! 파일 위치: ${backupFile.name}"
                            }
                        )

                        ShadcnButtonText(
                            text = "📥 JSON 백업 가져오기 / 복원",
                            variant = ButtonVariant.OUTLINE,
                            onClick = { showImportField = !showImportField }
                        )
                    }

                    if (showImportField) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShadcnInput(
                                value = importJsonText,
                                onValueChange = { importJsonText = it },
                                label = "JSON 백업 데이터 붙여넣기 (Paste JSON backup string)",
                                placeholder = "{\n  \"exportedAt\": ...\n}",
                                singleLine = false,
                                modifier = Modifier.height(120.dp)
                            )

                            ShadcnButtonText(
                                text = "복원 실행 (Confirm Import)",
                                variant = ButtonVariant.PRIMARY,
                                enabled = importJsonText.isNotBlank(),
                                onClick = {
                                    val success = repository.importBackupJson(importJsonText)
                                    backupStatusMessage = if (success) "성공적으로 데이터를 복원했습니다!" else "복원 실패: 올바른 JSON 형식이 아닙니다."
                                    if (success) {
                                        importJsonText = ""
                                        showImportField = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // App Information
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "앱 정보 (About)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Korean Won Money Management App v1.0.0",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• UI: Jetpack Compose Multiplatform (Desktop & Android)\n• Design: Shadcn Zinc Minimalist Design System\n• Currency: Korean Won (₩ KRW)\n• Database: 100% Offline SQLite Local Database",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
