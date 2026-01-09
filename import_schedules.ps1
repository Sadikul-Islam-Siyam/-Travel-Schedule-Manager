# Import all existing JSON schedules into the database
# This makes them editable through the UI

$dbPath = "data/travel_plans.db"
$trainJsonPath = "data/train_schedules.json"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Importing Existing Schedules to Database" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Load train schedules
if (Test-Path $trainJsonPath) {
    $trains = Get-Content $trainJsonPath -Raw | ConvertFrom-Json
    Write-Host "Found $($trains.Count) trains in JSON file"
    
    $imported = 0
    $skipped = 0
    
    foreach ($train in $trains) {
        # Escape single quotes
        $name = $train.trainName -replace "'","''"
        $orig = $train.start -replace "'","''"
        $dest = $train.destination -replace "'","''"
        
        # Check if exists
        $check = "SELECT COUNT(*) FROM routes WHERE route_name='$name' AND transport_type='TRAIN';"
        $exists = sqlite3 $dbPath $check
        
        if ($exists -eq 0) {
            # Parse duration
            $duration = 0
            if ($train.duration -match '(\d+):(\d+)') {
                $duration = [int]$matches[1] * 60 + [int]$matches[2]
            }
            
            # Build metadata
            $meta = "duration:$($train.duration);arrivalTime:$($train.arrivalTime)"
            if ($train.offDay) {
                $meta += ";offDay:$($train.offDay)"
            }
            if ($train.stops) {
                $stopsJson = ($train.stops | ConvertTo-Json -Compress) -replace "'","''"
                $meta += ";stops:$stopsJson"
            }
            $meta = $meta -replace "'","''"
            
            # Insert
            $insert = "INSERT INTO routes (route_name, origin, destination, transport_type, duration_minutes, price, schedule_time, metadata, status, created_date) VALUES ('$name', '$orig', '$dest', 'TRAIN', $duration, $($train.fare), '$($train.startTime)', '$meta', 'ACTIVE', datetime('now'));"
            sqlite3 $dbPath $insert
            $imported++
            Write-Host "  + $($train.trainName)" -ForegroundColor Green
        } else {
            $skipped++
        }
    }
    
    Write-Host ""
    Write-Host "Train Import Complete:" -ForegroundColor Cyan
    Write-Host "  Imported: $imported" -ForegroundColor Green
    Write-Host "  Skipped: $skipped" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Done! Restart the app to see all trains as ACTIVE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
