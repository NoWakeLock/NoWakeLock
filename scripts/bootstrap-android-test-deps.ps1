param(
    [string]$LocalRepo = "$env:USERPROFILE\.m2\repository",
    [string[]]$Coordinates = @(
        'androidx.test:core:1.7.0',
        'androidx.test:monitor:1.8.0',
        'androidx.test.ext:junit:1.3.0',
        'androidx.test:runner:1.7.0',
        'androidx.test:rules:1.7.0',
        'androidx.test.espresso:espresso-core:3.7.0',
        'androidx.test.espresso:espresso-idling-resource:3.7.0',
        'com.android.tools.utp:android-test-plugin-host-additional-test-output:31.9.2'
    )
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$repos = @(
    'https://dl.google.com/dl/android/maven2',
    'https://repo.maven.apache.org/maven2'
)
$visited = [System.Collections.Generic.HashSet[string]]::new()

function Resolve-PropertyValue($value, $properties) {
    if ($null -eq $value) { return $null }
    if ($value -match '^\$\{(.+)\}$') {
        $name = $matches[1]
        if ($properties.ContainsKey($name)) { $value = $properties[$name] }
    }
    if ($value -match '^[\[\(]([^,\]\)]+)(,[^\]\)]*)?[\]\)]$') {
        return $matches[1]
    }
    return $value
}

function Save-RemoteFile($relativePath, $targetPath) {
    foreach ($repo in $repos) {
        $url = "$repo/$relativePath"
        try {
            Invoke-WebRequest $url -OutFile $targetPath -UseBasicParsing
            return $true
        } catch {
        }
    }
    return $false
}

function Ensure-Artifact($group, $artifact, $version) {
    $key = "$group`:$artifact`:$version"
    if ($visited.Contains($key)) { return }
    [void]$visited.Add($key)

    $pathPart = ($group -replace '\.', '/')
    $basePath = Join-Path $LocalRepo "$pathPart\$artifact\$version"
    New-Item -ItemType Directory -Force -Path $basePath | Out-Null

    $pomName = "$artifact-$version.pom"
    $pomPath = Join-Path $basePath $pomName
    $pomRelativePath = "$pathPart/$artifact/$version/$pomName"

    if (-not (Test-Path $pomPath)) {
        if (-not (Save-RemoteFile $pomRelativePath $pomPath)) {
            throw "Failed to download $key POM"
        }
    }

    [xml]$pom = Get-Content $pomPath
    $properties = @{
        'project.version' = $version
        'pom.version' = $version
        'project.groupId' = $group
        'pom.groupId' = $group
        'project.artifactId' = $artifact
        'pom.artifactId' = $artifact
    }
    if ($pom.project.properties) {
        foreach ($child in $pom.project.properties.ChildNodes) {
            if ($child.Name -ne '#comment') {
                $properties[$child.Name] = $child.InnerText
            }
        }
    }

    $packaging = if ($pom.project.packaging) { $pom.project.packaging } else { 'jar' }
    $extension = if ($packaging -eq 'aar') { 'aar' } elseif ($packaging -eq 'pom') { 'pom' } else { 'jar' }
    if ($extension -ne 'pom') {
        $artifactName = "$artifact-$version.$extension"
        $artifactPath = Join-Path $basePath $artifactName
        $artifactRelativePath = "$pathPart/$artifact/$version/$artifactName"
        if (-not (Test-Path $artifactPath)) {
            if (-not (Save-RemoteFile $artifactRelativePath $artifactPath)) {
                throw "Failed to download $key artifact"
            }
        }
    }

    foreach ($dep in $pom.project.dependencies.dependency) {
        if ($dep.optional -eq 'true') { continue }
        if ($dep.scope -in @('test', 'provided', 'system')) { continue }

        $depVersion = Resolve-PropertyValue $dep.version $properties
        if ([string]::IsNullOrWhiteSpace($depVersion)) { continue }

        Ensure-Artifact $dep.groupId $dep.artifactId $depVersion
    }
}

foreach ($coordinate in $Coordinates) {
    $parts = $coordinate.Split(':')
    if ($parts.Length -ne 3) {
        throw "Invalid coordinate: $coordinate"
    }
    Ensure-Artifact $parts[0] $parts[1] $parts[2]
}

Write-Host "Bootstrapped $($visited.Count) artifacts into $LocalRepo"
