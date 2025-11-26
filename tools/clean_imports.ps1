Get-ChildItem -Path . -Recurse -Filter *.java | ForEach-Object {
    $file = $_.FullName
    $content = Get-Content -Raw -Encoding UTF8 $file
    $lines = $content -split "\r?\n"
    $importLines = @()
    for ($i=0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i].Trim()
        if ($line -match '^import\s+([^;]+);' -and $line -notmatch 'static') {
            $importLines += [PSCustomObject]@{Index=$i;Text=$lines[$i];Qualified=$Matches[1]}
        }
    }
    if ($importLines.Count -eq 0) { continue }
    $toRemove = @()
    $seen = @{}
    # build body without imports
    $body = ($lines | Where-Object { $lines.IndexOf($_) -notin ($importLines | ForEach-Object { $_.Index }) }) -join "`n"
    foreach ($imp in $importLines) {
        $txt = $imp.Text.Trim()
        if ($seen.ContainsKey($txt)) { $toRemove += $imp.Index; continue }
        $seen[$txt] = $true
        $qual = $imp.Qualified
        if ($qual.EndsWith('.*')) { continue }
        $parts = $qual -split '\.'
        $simple = $parts[-1]
        # search in body
        if (-not (Select-String -InputObject $body -Pattern "\b$([regex]::Escape($simple))\b" -SimpleMatch)) {
            $toRemove += $imp.Index
        }
    }
    if ($toRemove.Count -eq 0) { continue }
    $newLines = @()
    for ($i=0; $i -lt $lines.Count; $i++) {
        if ($toRemove -contains $i) { continue }
        $newLines += $lines[$i]
    }
    $newContent = $newLines -join "`n"
    Set-Content -Path $file -Value $newContent -Encoding UTF8
    Write-Host "Patched $file - removed $($toRemove.Count) imports"
}
Write-Host 'Done'