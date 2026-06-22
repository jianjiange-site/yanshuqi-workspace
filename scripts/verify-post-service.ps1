# PostService stage 5 verification (Windows PowerShell)
# Usage: .\scripts\verify-post-service.ps1
# Chinese descriptions are emitted via Write-Host; keep this file ASCII-only for PS 5.1 compatibility.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$PostDir = Join-Path $Root "post-service"
$ProtoFile = Join-Path $Root "proto\post\post_service.proto"
$Pass = 0
$Fail = 0

function Pass([string]$msg) { Write-Host "[PASS] $msg"; $script:Pass++ }
function Fail([string]$msg) { Write-Host "[FAIL] $msg"; $script:Fail++ }

function Stop-IfFailed {
    if ($script:Fail -gt 0) {
        Write-Host "[ABORT] critical check failed"
        exit 1
    }
}

if (-not $env:JAVA_HOME) {
    $candidate = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
    if (Test-Path $candidate) { $env:JAVA_HOME = $candidate }
}

Write-Host "=== PostService verify (stage 5) ==="

if (-not (Test-Path $PostDir) -or -not (Test-Path (Join-Path $Root "proto"))) {
    Write-Host "[FAIL] run from repo root; need post-service and proto"
    exit 1
}

Write-Host "[1] post-service directory"
if (Test-Path $PostDir) { Pass "post-service dir" } else { Fail "post-service dir"; Stop-IfFailed }

Write-Host "[2] proto/post/post_service.proto"
if (Test-Path $ProtoFile) { Pass "proto file" } else { Fail "proto file"; Stop-IfFailed }

Write-Host "[3] nine RPC definitions"
$Rpcs = @("CreatePost","GetPostDetail","ListUserPosts","ActionLike","CreateComment","ListComments","DeleteComment","DeletePost","GetRecommendFeed")
$protoText = Get-Content $ProtoFile -Raw
$rpcMissing = $false
foreach ($rpc in $Rpcs) {
    if ($protoText -notlike "*rpc $rpc(*") {
        Write-Host "  missing rpc: $rpc"
        $rpcMissing = $true
    }
}
if (-not $rpcMissing) { Pass "proto rpc" } else { Fail "proto rpc"; Stop-IfFailed }

Write-Host "[4] Flyway V001/V002"
$V001 = Join-Path $PostDir "src\main\resources\db\migration\V001__create_post_core_tables.sql"
$V002 = Join-Path $PostDir "src\main\resources\db\migration\V002__create_post_interaction_tables.sql"
if ((Test-Path $V001) -and (Test-Path $V002)) { Pass "flyway scripts" } else { Fail "flyway scripts"; Stop-IfFailed }

Write-Host "[5] migration table names"
$migrationText = (Get-Content $V001 -Raw) + (Get-Content $V002 -Raw)
$Tables = @("posts","post_images","post_stats","post_likes","post_comments")
$tableMissing = $false
foreach ($table in $Tables) {
    if ($migrationText -notmatch $table) {
        Write-Host "  missing table: $table"
        $tableMissing = $true
    }
}
if (-not $tableMissing) { Pass "migration tables" } else { Fail "migration tables"; Stop-IfFailed }

Write-Host "[6] PostGrpcService.java"
$GrpcService = Join-Path $PostDir "src\main\java\com\dating\post\grpc\PostGrpcService.java"
if (Test-Path $GrpcService) { Pass "PostGrpcService" } else { Fail "PostGrpcService"; Stop-IfFailed }

Write-Host "[7] core services"
$Services = @("PostWriteService","PostReadService","PostLikeService","PostCommentService","FeedService")
$svcMissing = $false
foreach ($svc in $Services) {
    $path = Join-Path $PostDir "src\main\java\com\dating\post\service\$svc.java"
    if (-not (Test-Path $path)) { Write-Host "  missing: $svc"; $svcMissing = $true }
}
if (-not $svcMissing) { Pass "core services" } else { Fail "core services"; Stop-IfFailed }

Write-Host "[8] core jobs"
$Jobs = @("LikeFlushJob","CommentFlushJob","FeedScoreJob")
$jobMissing = $false
foreach ($job in $Jobs) {
    $path = Join-Path $PostDir "src\main\java\com\dating\post\job\$job.java"
    if (-not (Test-Path $path)) { Write-Host "  missing: $job"; $jobMissing = $true }
}
if (-not $jobMissing) { Pass "core jobs" } else { Fail "core jobs"; Stop-IfFailed }

Write-Host "[9] Redis prefix yanshuqi"
$RedisKeys = Join-Path $PostDir "src\main\java\com\dating\post\constant\PostRedisKeys.java"
if ((Test-Path $RedisKeys) -and ((Get-Content $RedisKeys -Raw) -match "yanshuqi")) { Pass "redis prefix" } else { Fail "redis prefix"; Stop-IfFailed }

Write-Host "[10] post-service stage must not modify mobile-gateway"
if (Test-Path (Join-Path $Root ".git")) {
    Push-Location $Root
    try {
        $scopes = @(
            "post-service/",
            "proto/post/",
            "scripts/verify-post-service.sh",
            "scripts/verify-post-service.ps1",
            "README.md",
            "post-service/README.md"
        )
        $gwTouched = $false
        foreach ($scope in $scopes) {
            $prevEap = $ErrorActionPreference
            $ErrorActionPreference = 'Continue'
            $statusLines = git status --porcelain -- $scope 2>&1 | Out-String
            $diffLines = git diff --name-only HEAD -- $scope 2>&1 | Out-String
            $ErrorActionPreference = $prevEap
            if ($statusLines -match "mobile-gateway/") { $gwTouched = $true }
            if ($diffLines -match "(?m)^mobile-gateway/") { $gwTouched = $true }
        }
        if (-not $gwTouched) { Pass "gateway not in post-service scope" } else { Fail "gateway touched by post-service"; Stop-IfFailed }
    } finally { Pop-Location }
} else { Write-Host "[SKIP] not a git repo" }

Write-Host "[11] mvn test"
try {
    Push-Location $PostDir
    & mvn -q test
    Pass "mvn test"
} catch { Fail "mvn test"; Stop-IfFailed } finally { Pop-Location }

Write-Host "[12] mvn package -DskipTests"
try {
    Push-Location $PostDir
    & mvn -q package -DskipTests
    Pass "mvn package"
} catch { Fail "mvn package"; Stop-IfFailed } finally { Pop-Location }

Write-Host "=== Summary PASS=$Pass FAIL=$Fail ==="
if ($Fail -gt 0) { exit 1 }
Write-Host "PostService verification OK"
