param(
    [string]$AndroidSdk = "$env:LOCALAPPDATA\Android\Sdk"
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    $config = Get-Content -LiteralPath 'app/google-services.json' -Raw | ConvertFrom-Json
    if ($config.project_info.project_id -ne 'tavernquest-7684e') {
        throw 'O google-services.json deve pertencer ao Firebase de produção tavernquest-7684e.'
    }
    $client = @($config.client | Where-Object { $_.client_info.android_client_info.package_name -eq 'com.luminor.tavernquest' })
    if ($client.Count -ne 1 -or -not ($client[0].oauth_client | Where-Object { $_.client_type -eq 3 })) {
        throw 'Configuração de produção sem pacote Android ou cliente OAuth Web esperado.'
    }
    $buildTools = Get-ChildItem -LiteralPath (Join-Path $AndroidSdk 'build-tools') -Directory |
        Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } |
        Sort-Object { [version]$_.Name } -Descending | Select-Object -First 1
    if (-not $buildTools) { throw 'Android SDK Build Tools não encontrado. Informe -AndroidSdk.' }
    $signer = Join-Path $buildTools.FullName 'apksigner.bat'
    & .\gradlew.bat :app:lintProdRelease :app:testProdReleaseUnitTest :app:assembleProdRelease --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'A validação ou compilação de produção falhou; nenhum APK será exportado.' }

    $apk = 'app/build/outputs/apk/prod/release/app-prod-release.apk'
    $certificate = & $signer verify --print-certs $apk 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'A assinatura do APK é inválida.' }
    $sha1Line = @($certificate | Select-String '^Signer #1 certificate SHA-1 digest: ([0-9a-fA-F]+)$')
    if ($sha1Line.Count -ne 1) { throw 'Não foi possível identificar o certificado do APK.' }
    $sha1 = $sha1Line[0].Matches[0].Groups[1].Value
    if ($sha1 -ne 'a00bbe35d5c9aa987decb50fd805fc626c861acc') {
        throw 'O APK não foi assinado com o certificado de produção TavernQuest. Não distribua este arquivo.'
    }
    $registered = $client[0].oauth_client | Where-Object {
        $_.client_type -eq 1 -and
        $_.android_info.package_name -eq 'com.luminor.tavernquest' -and
        $_.android_info.certificate_hash -eq $sha1
    }
    if (-not $registered) { throw 'O certificado do APK não corresponde ao cliente Android no google-services.json.' }

    $metadata = Get-Content 'app/build/outputs/apk/prod/release/output-metadata.json' -Raw | ConvertFrom-Json
    if ($metadata.applicationId -ne 'com.luminor.tavernquest' -or $metadata.variantName -ne 'prodRelease') {
        throw 'O artefato gerado não corresponde à variante de produção.'
    }
    $version = $metadata.elements[0].versionName
    $code = $metadata.elements[0].versionCode
    $destination = Join-Path $projectRoot 'build/distribution'
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    $name = "TavernQuest-$version-$code-prodRelease.apk"
    $output = Join-Path $destination $name
    Copy-Item -LiteralPath $apk -Destination $output
    $hash = (Get-FileHash -LiteralPath $output -Algorithm SHA256).Hash.ToLowerInvariant()
    Set-Content -LiteralPath "$output.sha256" -Value "$hash  $name" -Encoding ascii
    Write-Host "APK de produção validado: $output"
    Write-Host "SHA-256: $hash"
    Write-Host 'A correspondência local do certificado não substitui o teste de login no celular nem a verificação do Firebase Console.'
} finally {
    Pop-Location
}
