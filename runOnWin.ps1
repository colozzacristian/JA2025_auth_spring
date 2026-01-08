param(
    [string]$EnvFile = ".env",
    [switch]$Verbose,
    [switch]$Remove,
    [switch]$RemoveQuotes
)

# Check if .env file exists
if (-not (Test-Path $EnvFile)) {
    Write-Error "Environment file '$EnvFile' not found"
    exit 1
}

# Load variables from .env file
Get-Content $EnvFile | Where-Object { $_ -match '^\s*[^\s=#]+=' -and -not $_.StartsWith('#') } | ForEach-Object {
    $line = $_ -replace '^\s+' # Remove leading whitespace
    if ($line) {
        $keyVal = $line -split '=', 2
        $key = $keyVal[0].Trim()
        $value = if ($RemoveQuotes) { $keyVal[1].Trim().Trim('"').Trim("'") } else { $keyVal[1].Trim() }
        
        if ($Verbose) {
            Write-Host "Setting $key=$value"
        }
        
        if (-not $Remove) {
            [Environment]::SetEnvironmentVariable($key, $value)
        }
    }
}

mvn clean package jetty:run
   