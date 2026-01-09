# Sync database to JSON files
# This ensures JSON files match the database

$dbPath = "data/travel_plans.db"
$trainJsonPath = "data/train_schedules.json"
$busJsonPath = "data/bus_schedules.json"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Syncing Database to JSON Files" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get all trains from database
Write-Host "Loading trains from database..." -ForegroundColor Yellow
$trainData = sqlite3 $dbPath "SELECT route_name, origin, destination, schedule_time, metadata, price FROM routes WHERE transport_type='TRAIN';" -separator "|"

$trains = @()
foreach ($line in $trainData) {
    if ($line -match '(.+?)\|(.+?)\|(.+?)\|(.+?)\|(.+?)\|(.+)') {
        $trainName = $matches[1]
        $origin = $matches[2]
        $destination = $matches[3]
        $startTime = $matches[4]
        $metadata = $matches[5]
        $fare = [double]$matches[6]
        
        # Parse metadata
        $duration = "6:00h"
        $arrivalTime = "12:00"
        $offDay = "No off day"
        $stops = @()
        
        if ($metadata -match 'duration:([^;]+)') { $duration = $matches[1] }
        if ($metadata -match 'arrivalTime:([^;]+)') { $arrivalTime = $matches[1] }
        if ($metadata -match 'offDay:([^;]+)') { $offDay = $matches[1] }
        if ($metadata -match 'stops:(\[.+?\])') {
            try {
                $stopsJson = $matches[1]
                $stops = $stopsJson | ConvertFrom-Json
            } catch {}
        }
        
        if ($stops.Count -gt 0) {
            $train = [PSCustomObject]@{
                trainName = $trainName
                start = $origin
                destination = $destination
                startTime = $startTime
                arrivalTime = $arrivalTime
                fare = $fare
                duration = $duration
                offDay = $offDay
                stops = @($stops)  # Force array type
            }
        } else {
            $train = [PSCustomObject]@{
                trainName = $trainName
                start = $origin
                destination = $destination
                startTime = $startTime
                arrivalTime = $arrivalTime
                fare = $fare
                duration = $duration
                offDay = $offDay
            }
        }
        
        $trains += $train
    }
}

Write-Host "Found $($trains.Count) trains" -ForegroundColor Green

# Get all buses from database
Write-Host "Loading buses from database..." -ForegroundColor Yellow
$busData = sqlite3 $dbPath "SELECT route_name, origin, destination, schedule_time, metadata, price FROM routes WHERE transport_type='BUS';" -separator "|"

$buses = @()
foreach ($line in $busData) {
    if ($line -match '(.+?)\|(.+?)\|(.+?)\|(.+?)\|(.+?)\|(.+)') {
        $busName = $matches[1]
        $origin = $matches[2]
        $destination = $matches[3]
        $startTime = $matches[4]
        $metadata = $matches[5]
        $fare = [double]$matches[6]
        
        # Parse metadata
        $duration = "6:00h"
        $arrivalTime = "12:00"
        
        if ($metadata -match 'duration:([^;]+)') { $duration = $matches[1] }
        if ($metadata -match 'arrivalTime:([^;]+)') { $arrivalTime = $matches[1] }
        
        $bus = [PSCustomObject]@{
            busName = $busName
            start = $origin
            destination = $destination
            startTime = $startTime
            arrivalTime = $arrivalTime
            fare = $fare
            duration = $duration
        }
        
        $buses += $bus
    }
}

Write-Host "Found $($buses.Count) buses" -ForegroundColor Green
Write-Host ""

# Save to JSON
Write-Host "Writing to JSON files..." -ForegroundColor Yellow
$trains | ConvertTo-Json -Depth 10 | Out-File -FilePath $trainJsonPath -Encoding UTF8
$buses | ConvertTo-Json -Depth 10 | Out-File -FilePath $busJsonPath -Encoding UTF8

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Sync Complete!" -ForegroundColor Green
Write-Host "  Trains: $($trains.Count)" -ForegroundColor Cyan
Write-Host "  Buses: $($buses.Count)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Now restart the application!" -ForegroundColor Yellow
