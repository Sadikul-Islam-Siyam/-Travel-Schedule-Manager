# Import all existing JSON schedules into the database
# This makes them editable through the UI

$dbPath = "data/travel_plans.db"
$trainJsonPath = "data/train_schedules.json"
$busJsonPath = "data/bus_schedules.json"

Write-Host "========================================"
Write-Host "Importing Existing Schedules to Database"
Write-Host "========================================"
Write-Host ""

# Load train schedules
if (Test-Path $trainJsonPath) {
    $trains = Get-Content $trainJsonPath -Raw | ConvertFrom-Json
    Write-Host "Found $($trains.Count) trains in JSON file"
    
    $imported = 0
    $skipped = 0
    
    foreach ($train in $trains) {
        # Check if already in database
        $checkSql = "SELECT COUNT(*) FROM routes WHERE route_name='$($train.trainName.Replace("'","''"))' AND transport_type='TRAIN';"
        $existing = sqlite3 $dbPath $checkSql
        
        if ($existing -eq 0) {
            # Parse duration to minutes
            $duration = 0
            if ($train.duration -match '(\d+):(\d+)h') {
                $hours = [int]$matches[1]
                $mins = [int]$matches[2]
                $duration = $hours * 60 + $mins
            }
            
            # Build metadata
            $metadata = "duration:$($train.duration);arrivalTime:$($train.arrivalTime)"
            if ($train.offDay) {
                $metadata += ";offDay:$($train.offDay)"
            }
            if ($train.stops) {
                $stopsJson = $train.stops | ConvertTo-Json -Compress
                $metadata += ";stops:$stopsJson"
            }
            
            # Escape single quotes for SQL
            $routeName = $train.trainName -replace "'", "''"
            $origin = $train.start -replace "'", "''"
            $destination = $train.destination -replace "'", "''"
            $metadataEscaped = $metadata -replace "'", "''"
            
            # Insert into database
            $sql = @"
INSERT INTO routes (route_name, origin, destination, transport_type, duration_minutes, price, schedule_time, metadata, status, created_date)
VALUES ('$routeName', '$origin', '$destination', 'TRAIN', $duration, $($train.fare), '$($train.startTime)', '$metadataEscaped', 'ACTIVE', datetime('now'));
"@
            
            sqlite3 $dbPath $sql
            $imported++
            Write-Host "  ✓ Imported: $($train.trainName)" -ForegroundColor Green
        } else {
            $skipped++
        }
    }
    
    Write-Host ""
    Write-Host "Train Import Complete:" -ForegroundColor Cyan
    Write-Host "  Imported: $imported" -ForegroundColor Green
    Write-Host "  Skipped (already exist): $skipped" -ForegroundColor Yellow
}

# Load bus schedules
if (Test-Path $busJsonPath) {
    $buses = Get-Content $busJsonPath -Raw | ConvertFrom-Json
    Write-Host ""
    Write-Host "Found $($buses.Count) buses in JSON file"
    
    $imported = 0
    $skipped = 0
    
    foreach ($bus in $buses) {
        # Check if already in database
        $checkSql = "SELECT COUNT(*) FROM routes WHERE route_name='$($bus.busName.Replace("'","''"))' AND transport_type='BUS';"
        $existing = sqlite3 $dbPath $checkSql
        
        if ($existing -eq 0) {
            # Parse duration to minutes
            $duration = 0
            if ($bus.duration -match '(\d+):(\d+)h') {
                $hours = [int]$matches[1]
                $mins = [int]$matches[2]
                $duration = $hours * 60 + $mins
            }
            
            # Build metadata
            $metadata = "duration:$($bus.duration);arrivalTime:$($bus.arrivalTime)"
            
            # Escape single quotes for SQL
            $routeName = $bus.busName -replace "'", "''"
            $origin = $bus.start -replace "'", "''"
            $destination = $bus.destination -replace "'", "''"
            $metadataEscaped = $metadata -replace "'", "''"
            
            # Insert into database
            $sql = @"
INSERT INTO routes (route_name, origin, destination, transport_type, duration_minutes, price, schedule_time, metadata, status, created_date)
VALUES ('$routeName', '$origin', '$destination', 'BUS', $duration, $($bus.fare), '$($bus.startTime)', '$metadataEscaped', 'ACTIVE', datetime('now'));
"@
            
            sqlite3 $dbPath $sql
            $imported++
            Write-Host "  ✓ Imported: $($bus.busName)" -ForegroundColor Green
        } else {
            $skipped++
        }
    }
    
    Write-Host ""
    Write-Host "Bus Import Complete:" -ForegroundColor Cyan
    Write-Host "  Imported: $imported" -ForegroundColor Green
    Write-Host "  Skipped (already exist): $skipped" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================"
Write-Host "Import Complete!" -ForegroundColor Green
Write-Host "All schedules are now editable in the UI"
Write-Host "========================================"
