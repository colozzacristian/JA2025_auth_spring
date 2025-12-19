param(
    [string]$Path,
    [switch]$Verbose,
    [switch]$Remove,
    [switch]$RemoveQuotes
)

$variables = Select-String -Path $Path -Pattern '^\s*[^\s=#]+=[^\s]+$' -Raw

foreach(variables) {
    $keyVal = $var -split '=', 2
    keyVal[0].Trim()
    RemoveQuotes ? $keyVal[1].Trim("'").Trim('"') : $keyVal[1]
    [Environment]::SetEnvironmentVariable(Remove ? '' : Verbose) {
        "([Environment]::GetEnvironmentVariable($key))"
    }
}

mvn clean package jetty:run
   