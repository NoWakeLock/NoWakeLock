param(
    [string]$DeviceSerial,
    [switch]$SkipBootstrap,
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
$bootstrapScript = Join-Path $PSScriptRoot 'bootstrap-android-test-deps.ps1'

function Invoke-ExternalCommand {
    param(
        [string]$Label,
        [string]$FilePath,
        [string[]]$Arguments
    )

    Write-Host "==> $Label"
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Label failed with exit code $LASTEXITCODE"
    }
}

function Get-AdbArgs {
    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        return @()
    }

    return @('-s', $DeviceSerial)
}

if (-not $SkipBootstrap) {
    Invoke-ExternalCommand -Label 'Bootstrap androidTest dependencies' -FilePath 'powershell.exe' -Arguments @(
        '-ExecutionPolicy', 'Bypass', '-File', $bootstrapScript
    )
}

Invoke-ExternalCommand -Label 'Assemble debug and androidTest APKs' -FilePath $gradleWrapper -Arguments @(
    '-PuseLocalMavenBootstrap=true', '--offline', ':app:assembleDebug', ':app:assembleDebugAndroidTest'
)

$appApk = Get-ChildItem (Join-Path $repoRoot 'app\build\outputs\apk\debug') -Filter 'NoWakeLock-*.apk' |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
$testApk = Join-Path $repoRoot 'app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk'

if (-not $appApk) {
    throw 'Debug APK not found under app\build\outputs\apk\debug'
}
if (-not (Test-Path $testApk)) {
    throw 'androidTest APK not found under app\build\outputs\apk\androidTest\debug'
}

$adbArgs = Get-AdbArgs
Invoke-ExternalCommand -Label 'Check connected device' -FilePath 'adb' -Arguments ($adbArgs + @('wait-for-device'))

if (-not $SkipInstall) {
    Invoke-ExternalCommand -Label 'Install debug APK' -FilePath 'adb' -Arguments ($adbArgs + @('install', '-r', $appApk.FullName))
    Invoke-ExternalCommand -Label 'Install androidTest APK' -FilePath 'adb' -Arguments ($adbArgs + @('install', '-r', $testApk))
}

$instrumentArgs = $adbArgs + @(
    'shell', 'am', 'instrument', '-w',
    'com.js.nowakelock.test/androidx.test.runner.AndroidJUnitRunner'
)

Write-Host '==> Run connected androidTest suite'
$instrumentOutput = & adb @instrumentArgs 2>&1
$instrumentExitCode = $LASTEXITCODE
$instrumentOutput | ForEach-Object { Write-Host $_ }

if ($instrumentExitCode -ne 0) {
    throw "Instrumentation failed with exit code $instrumentExitCode"
}
if (($instrumentOutput -join "`n") -notmatch 'OK \(\d+ tests?\)') {
    throw 'Instrumentation did not report a successful test count'
}

Write-Host 'Connected androidTest suite passed.'
