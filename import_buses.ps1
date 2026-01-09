# Import bus schedules into database
$dbPath = "data/travel_plans.db"
$busJsonPath = "data/bus_schedules.json"

Write-Host "Importing bus schedules to database..." -ForegroundColor Cyan

$buses = Get-Content $busJsonPath -Raw | ConvertFrom-Json
$imported = 0

foreach ($bus in $buses) {
    # Parse duration to minutes
    $duration = 0
    if ($bus.duration -match '(\d+):(\d+)') {
        $duration = [int]$matches[1] * 60 + [int]$matches[2]
    }
    
    # Build metadata
    $meta = "duration:$($bus.duration);arrivalTime:$($bus.arrivalTime)"
    $meta = $meta -replace "'","''"
    
    # Escape single quotes
    $name = $bus.busName -replace "'","''"
    $orig = $bus.start -replace "'","''"
    $dest = $bus.destination -replace "'","''"
    
    # Insert
    $insert = "INSERT INTO routes (route_name, origin, destination, transport_type, duration_minutes, price, schedule_time, metadata, status, created_date) VALUES ('$name', '$orig', '$dest', 'BUS', $duration, $($bus.fare), '$($bus.startTime)', '$meta', 'ACTIVE', datetime('now'));"
    sqlite3 $dbPath $insert
    $imported++
    Write-Host "  + $($bus.busName)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Imported $imported buses successfully!" -ForegroundColor Green
Write-Host "Restart the app to see all buses!" -ForegroundColor Cyan
