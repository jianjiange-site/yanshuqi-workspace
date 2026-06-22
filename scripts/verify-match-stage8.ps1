# MatchService 阶段 8 联调前验收脚本
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$MatchDir = Join-Path $Root "match-service"
$GatewayDir = Join-Path $Root "mobile-gateway"
$Pass = 0
$Fail = 0

function Pass([string]$msg) { Write-Host "[PASS] $msg"; $script:Pass++ }
function Fail([string]$msg) { Write-Host "[FAIL] $msg"; $script:Fail++ }

Write-Host "=== MatchService Stage 8 Verification ==="

Write-Host "--- 1. match-service compile ---"
try {
    Push-Location $MatchDir
    & mvn -q clean compile
    Pass "match-service compile"
} catch {
    Fail "match-service compile"
} finally {
    Pop-Location
}

Write-Host "--- 2. match-service tests ---"
try {
    Push-Location $MatchDir
    & mvn -q clean test
    Pass "match-service tests"
} catch {
    Fail "match-service tests"
} finally {
    Pop-Location
}

Write-Host "--- 3. default client-mode is mock ---"
$TestYml = Join-Path $MatchDir "src\test\resources\application-test.yml"
$DevYml = Join-Path $MatchDir "src\main\resources\application-dev.yml"
$testContent = Get-Content $TestYml -Raw
$devContent = Get-Content $DevYml -Raw
if ($testContent -match "user-client-mode:\s*mock" -and $devContent -match "user-client-mode:\s*mock") {
    Pass "default client-mode mock in dev/test"
} else {
    Fail "default client-mode mock in dev/test"
}

Write-Host "--- 4. Redis key prefix yanshuqi ---"
$RedisConst = Join-Path $MatchDir "src\main\java\com\dating\match\constant\RedisKeyConstants.java"
$redisContent = Get-Content $RedisConst -Raw
if ($redisContent -match "yanshuqi:match:" -and $redisContent -match "yanshuqi:lock:match:") {
    Pass "Redis key prefix contains yanshuqi"
} else {
    Fail "Redis key prefix contains yanshuqi"
}

Write-Host "--- 5. no cross-service Java imports (allow proto) ---"
$javaFiles = Get-ChildItem -Path (Join-Path $MatchDir "src\main\java") -Recurse -Filter "*.java"
$bad = @()
foreach ($file in $javaFiles) {
    $lines = Select-String -Path $file.FullName -Pattern "import com\.dating\.(user|payment|im)\." -AllMatches
    foreach ($line in $lines) {
        if ($line.Line -notmatch "grpc\.proto") {
            $bad += "$($file.FullName):$($line.LineNumber):$($line.Line.Trim())"
        }
    }
}
if ($bad.Count -eq 0) {
    Pass "no forbidden cross-service imports"
} else {
    $bad | ForEach-Object { Write-Host $_ }
    Fail "forbidden cross-service imports found"
}

Write-Host "--- 6. gateway has no Match business logic keywords ---"
$gwSrc = Join-Path $GatewayDir "src\main\java"
$gwHits = @()
if (Test-Path $gwSrc) {
    $gwHits = Get-ChildItem -Path $gwSrc -Recurse -Filter "*.java" -ErrorAction SilentlyContinue |
        Select-String -Pattern "QuotaService|SwipeHistoryManager|MatchCreationService" -ErrorAction SilentlyContinue
}
if ($gwHits) {
    Fail "gateway contains Match business logic keywords"
} else {
    Pass "gateway has no Match business logic keywords"
}

Write-Host "=== Summary: PASS=$Pass FAIL=$Fail ==="
if ($Fail -gt 0) { exit 1 }
