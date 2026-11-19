$root = Join-Path $PSScriptRoot "src\main\java\org\group6\childrenelectricvehiclerentalmanagement"
$targets = @(
    "Config\SecurityConfig.java",
    "DTOs\VehicleController",
    "Utility\Validators\InputValidator.java"
)

foreach ($target in $targets) {
    $path = Join-Path $root $target
    if (Test-Path $path) {
        Remove-Item -Recurse -Force $path
        Write-Host "Da xoa $target"
    }
}

//powershell -ExecutionPolicy Bypass -File .\setup-local.ps1
