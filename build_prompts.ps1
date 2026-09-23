# Inserts _header.md into every numbered prompt file. Safe to re-run after editing _header.md.
$root  = $PSScriptRoot
$start = '<!-- HEADER:START -->'
$end   = '<!-- HEADER:END -->'
$utf8  = New-Object System.Text.UTF8Encoding($false)

$header = [IO.File]::ReadAllText((Join-Path $root '_header.md'), $utf8).Trim()
$block  = "$start`n$header`n$end"
$regex  = [regex]::new([regex]::Escape($start) + '.*?' + [regex]::Escape($end), 'Singleline')

Get-ChildItem -Path $root -Recurse -File -Filter '*.md' | Where-Object { $_.Name -match '^\d\d_' -and $_.Name -notlike '00_*' } | Sort-Object Name | ForEach-Object {
    $text = [IO.File]::ReadAllText($_.FullName, $utf8)
    if ($text.Contains('{{HEADER}}')) {
        $text = $text.Replace('{{HEADER}}', $block)
    } elseif ($regex.IsMatch($text)) {
        $text = $regex.Replace($text, { param($m) $block })
    } else {
        Write-Output "skipped (no placeholder): $($_.Name)"
        return
    }
    [IO.File]::WriteAllText($_.FullName, $text, $utf8)
    Write-Output "updated: $($_.Name)"
}
