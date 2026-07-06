#!/bin/bash
# TermuxMod Safety Check
# Verifikasi semua konstanta kritis tidak berubah
# Jalankan SEBELUM dan SESUDAH edit apapun
# Usage: bash scripts/safety-check.sh

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASS=0
FAIL=0

check() {
    local name="$1"
    local file="$2"
    local pattern="$3"
    if grep -q "$pattern" "$file" 2>/dev/null; then
        echo -e "${GREEN}[PASS]${NC} $name"
        ((PASS++))
    else
        echo -e "${RED}[FAIL]${NC} $name"
        echo -e "       File : $file"
        echo -e "       Cari : $pattern"
        ((FAIL++))
    fi
}

check_absent() {
    local name="$1"
    local file="$2"
    local pattern="$3"
    if ! grep -q "$pattern" "$file" 2>/dev/null; then
        echo -e "${GREEN}[PASS]${NC} $name"
        ((PASS++))
    else
        echo -e "${RED}[FAIL]${NC} $name — pattern ini TIDAK BOLEH ada: $pattern"
        echo -e "       File : $file"
        ((FAIL++))
    fi
}

echo "============================================"
echo " TermuxMod Safety Check"
echo "============================================"

echo ""
echo "--- [1] applicationId ---"
check \
    "applicationId harus com.termux" \
    "app/build.gradle" \
    'applicationId "com.termux"'

echo ""
echo "--- [2] AndroidManifest ---"
check \
    "package name harus com.termux" \
    "app/src/main/AndroidManifest.xml" \
    'package="com.termux"'
check \
    "sharedUserId harus ada" \
    "app/src/main/AndroidManifest.xml" \
    'android:sharedUserId'
check \
    "launchMode singleTask harus ada" \
    "app/src/main/AndroidManifest.xml" \
    'android:launchMode="singleTask"'
check \
    "TermuxActivity harus ada" \
    "app/src/main/AndroidManifest.xml" \
    '.app.TermuxActivity'
check \
    "TermuxService harus ada" \
    "app/src/main/AndroidManifest.xml" \
    '.app.TermuxService'
check \
    "RunCommandService harus ada" \
    "app/src/main/AndroidManifest.xml" \
    '.app.RunCommandService'
check \
    "FOREGROUND_SERVICE permission harus ada" \
    "app/src/main/AndroidManifest.xml" \
    'android.permission.FOREGROUND_SERVICE'
check \
    "WAKE_LOCK permission harus ada" \
    "app/src/main/AndroidManifest.xml" \
    'android.permission.WAKE_LOCK'

echo ""
echo "--- [3] TermuxConstants.java — Paths ---"
check \
    "TERMUX_PACKAGE_NAME harus com.termux" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    '"com.termux"'
check \
    "PREFIX_DIR_PATH harus ada" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    'TERMUX_PREFIX_DIR_PATH'
check \
    "HOME_DIR_PATH harus ada" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    'TERMUX_HOME_DIR_PATH'
check \
    "STAGING_PREFIX_DIR_PATH harus ada" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    'TERMUX_STAGING_PREFIX_DIR_PATH'

echo ""
echo "--- [4] TermuxConstants.java — Intent Actions ---"
check \
    "ACTION_SERVICE_EXECUTE harus ada" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    'service_execute'
check \
    "ACTION_STOP_SERVICE harus ada" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    'service_stop'
check \
    "ACTION_WAKE_LOCK harus ada" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    'service_wake_lock'
check \
    "EXTRA_PENDING_INTENT = pendingIntent" \
    "termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java" \
    '"pendingIntent"'

echo ""
echo "--- [5] JNI Native Bootstrap ---"
check \
    "loadLibrary termux-bootstrap harus ada" \
    "app/src/main/java/com/termux/app/TermuxInstaller.java" \
    'System.loadLibrary("termux-bootstrap")'
check \
    "native getZip() harus ada" \
    "app/src/main/java/com/termux/app/TermuxInstaller.java" \
    'native byte\[\] getZip'
check \
    "JNI function name harus ada di .c" \
    "app/src/main/cpp/termux-bootstrap.c" \
    'Java_com_termux_app_TermuxInstaller_getZip'
check \
    "bootstrap zip incbin harus ada" \
    "app/src/main/cpp/termux-bootstrap-zip.S" \
    '.incbin'

echo ""
echo "--- [6] TermuxService.java ---"
check \
    "startForeground harus ada" \
    "app/src/main/java/com/termux/app/TermuxService.java" \
    'startForeground'
check \
    "WakeLock acquire harus ada" \
    "app/src/main/java/com/termux/app/TermuxService.java" \
    'mWakeLock.acquire'
check \
    "WakeLock release harus ada" \
    "app/src/main/java/com/termux/app/TermuxService.java" \
    'mWakeLock.release'

echo ""
echo "--- [7] TermuxShellUtils.java — Environment ---"
check \
    "HOME env var harus ada" \
    "termux-shared/src/main/java/com/termux/shared/shell/TermuxShellUtils.java" \
    '"HOME='
check \
    "PREFIX env var harus ada" \
    "termux-shared/src/main/java/com/termux/shared/shell/TermuxShellUtils.java" \
    '"PREFIX='
check \
    "PATH env var harus ada" \
    "termux-shared/src/main/java/com/termux/shared/shell/TermuxShellUtils.java" \
    '"PATH='
check \
    "TMPDIR env var harus ada" \
    "termux-shared/src/main/java/com/termux/shared/shell/TermuxShellUtils.java" \
    '"TMPDIR='
check \
    "TERM=xterm-256color harus ada" \
    "termux-shared/src/main/java/com/termux/shared/shell/TermuxShellUtils.java" \
    'xterm-256color'

echo ""
echo "--- [8] ExecutionCommand.java — State Machine ---"
check \
    "PRE_EXECUTION state harus ada" \
    "termux-shared/src/main/java/com/termux/shared/models/ExecutionCommand.java" \
    'PRE_EXECUTION'
check \
    "EXECUTING state harus ada" \
    "termux-shared/src/main/java/com/termux/shared/models/ExecutionCommand.java" \
    'EXECUTING'
check \
    "SUCCESS state harus ada" \
    "termux-shared/src/main/java/com/termux/shared/models/ExecutionCommand.java" \
    'SUCCESS'
check \
    "FAILED state harus ada" \
    "termux-shared/src/main/java/com/termux/shared/models/ExecutionCommand.java" \
    'FAILED'

echo ""
echo "--- [9] ShellUtils.java — PID Reflection ---"
check \
    "Refleksi PID harus ada" \
    "termux-shared/src/main/java/com/termux/shared/shell/ShellUtils.java" \
    'getDeclaredField("pid")'

echo ""
echo "--- [10] Bootstrap Installer — Urutan ---"
check \
    "STAGING dir digunakan di installer" \
    "app/src/main/java/com/termux/app/TermuxInstaller.java" \
    'TERMUX_STAGING_PREFIX_DIR'
check \
    "symlinks dibuat sebelum rename" \
    "app/src/main/java/com/termux/app/TermuxInstaller.java" \
    'Os.symlink'
check \
    "atomic renameTo harus ada" \
    "app/src/main/java/com/termux/app/TermuxInstaller.java" \
    'renameTo'

echo ""
echo "============================================"
echo -e " HASIL: ${GREEN}${PASS} PASS${NC} | ${RED}${FAIL} FAIL${NC}"
echo "============================================"

if [ $FAIL -gt 0 ]; then
    echo -e "${RED}⛔ STOP — Ada $FAIL check yang gagal!${NC}"
    echo -e "${YELLOW}Jangan lanjutkan edit. Periksa file yang FAIL di atas.${NC}"
    echo -e "Rollback: git checkout -- <file>"
    exit 1
else
    echo -e "${GREEN}✅ Semua check PASS — Aman untuk melanjutkan.${NC}"
    exit 0
fi
