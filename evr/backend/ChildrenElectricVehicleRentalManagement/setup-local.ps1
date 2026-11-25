$target = Join-Path $PSScriptRoot "application-local.properties"
$gitignore = Join-Path $PSScriptRoot ".gitignore"

if (-not (Test-Path $gitignore)) {
    New-Item -ItemType File -Path $gitignore | Out-Null
}
if (-not (Select-String -Path $gitignore -Pattern "application-local.properties" -SimpleMatch -Quiet)) {
    Add-Content -Path $gitignore -Value "application-local.properties"
}

if (Test-Path $target) {
    Write-Host "application-local.properties da ton tai, giu nguyen."
    exit
}

$secure = Read-Host "Nhap mat khau PostgreSQL" -AsSecureString
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
$password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)

$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$secret = ($bytes | ForEach-Object { $_.ToString("x2") }) -join ""

$escaped = $password.Replace("\", "\\")
$lines = @("spring.datasource.password=$escaped", "jwt.secret=$secret")
[System.IO.File]::WriteAllLines($target, $lines, (New-Object System.Text.UTF8Encoding($false)))

Write-Host "Da tao application-local.properties"
