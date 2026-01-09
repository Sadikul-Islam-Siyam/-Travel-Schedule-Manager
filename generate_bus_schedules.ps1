# Generate Bus Schedules from provided data
# Uses operator name as bus name

$busData = @"
1,Dhaka to Cox's Bazar,Hanif Enterprise,08:30 PM,1050
2,Dhaka to Cox's Bazar,Shyamoli Paribahan,09:30 PM,1050
3,Dhaka to Cox's Bazar,S. Alam Service,10:45 PM,1000
4,Dhaka to Sylhet,Ena Paribahan,06:30 AM,700
5,Dhaka to Sylhet,Hanif Enterprise,10:00 AM,700
6,Dhaka to Sylhet,Shyamoli NR Travels,11:30 PM,700
7,Dhaka to Chattogram,Saudia Paribahan,07:30 AM,680
8,Dhaka to Chattogram,Unique Service,02:30 PM,680
9,Dhaka to Chattogram,Hanif Enterprise,11:30 PM,680
10,Dhaka to Rajshahi,National Travels,08:00 AM,650
11,Dhaka to Rajshahi,Desh Travels,10:30 AM,650
12,Dhaka to Rajshahi,Hanif Enterprise,11:15 PM,650
13,Dhaka to Khulna,Hanif Enterprise,07:45 AM,650
14,Dhaka to Khulna,Sohag Paribahan,09:15 AM,650
15,Dhaka to Khulna,Eagle Paribahan,10:30 PM,650
16,Dhaka to Rangpur,Nabil Paribahan,08:30 AM,700
17,Dhaka to Rangpur,S.R Travels,11:30 AM,700
18,Dhaka to Rangpur,Hanif Enterprise,11:00 PM,700
19,Dhaka to Barishal,Sakura Paribahan,07:15 AM,550
20,Dhaka to Barishal,Golden Line,11:30 AM,550
21,Dhaka to Kuakata,Hanif Enterprise,08:30 PM,750
22,Dhaka to Bogura,Shah Fateh Ali,09:30 AM,550
23,Chattogram to Cox's Bazar,Marsa Transport,07:00 AM,450
24,Chattogram to Cox's Bazar,S. Alam Service,09:00 AM,400
25,Chattogram to Cox's Bazar,Marsa Transport,11:00 AM,450
26,Chattogram to Cox's Bazar,S. Alam Service,03:00 PM,400
27,Chattogram to Sylhet,Ena Paribahan,08:30 AM,850
28,Chattogram to Sylhet,Saudia Paribahan,08:00 PM,850
29,Chattogram to Sylhet,Ena Paribahan,09:30 PM,850
30,Chattogram to Rangamati,Paharika Service,07:30 AM,180
31,Chattogram to Khagrachhari,Shanti Paribahan,08:00 AM,250
32,Chattogram to Cumilla,Tisha Plus,10:30 AM,350
33,Rajshahi to Khulna,Hanif Enterprise,06:45 AM,650
34,Rajshahi to Khulna,Desh Travels,02:15 PM,650
35,Rajshahi to Khulna,Hanif Enterprise,10:30 PM,650
36,Rajshahi to Rangpur,Nabil Paribahan,07:30 AM,450
37,Rajshahi to Rangpur,Hanif Enterprise,01:30 PM,450
38,Khulna to Barishal,Sakura Paribahan,08:00 AM,350
39,Khulna to Barishal,Local Direct,01:00 PM,300
40,Khulna to Rajshahi,Hanif Enterprise,07:15 AM,650
41,Khulna to Jashore,Local Direct,08:30 AM,150
42,Sylhet to Chattogram,Ena Paribahan,08:30 AM,850
43,Sylhet to Chattogram,Hanif Enterprise,09:30 PM,850
44,Sylhet to Cox's Bazar,Ena Paribahan,07:00 PM,1100
45,Sylhet to Cox's Bazar,Hanif Enterprise,08:30 PM,1100
46,Cumilla to Dhaka,Tisha Group,06:00 AM,300
47,Cumilla to Dhaka,Asia Line,11:30 AM,300
48,Mymensingh to Dhaka,Ena Paribahan,07:00 AM,300
49,Mymensingh to Dhaka,Soukhin Paribahan,02:00 PM,250
50,Feni to Dhaka,Star Line,07:30 AM,350
51,Feni to Dhaka,Star Line,09:00 PM,350
52,Bogura to Rangpur,S.R Travels,12:00 PM,250
"@

# Typical bus durations (in minutes) between cities
$durations = @{
    "Dhaka-Cox's Bazar" = 540     # 9 hours
    "Dhaka-Sylhet" = 360          # 6 hours
    "Dhaka-Chattogram" = 360      # 6 hours
    "Dhaka-Rajshahi" = 360        # 6 hours
    "Dhaka-Khulna" = 420          # 7 hours
    "Dhaka-Rangpur" = 420         # 7 hours
    "Dhaka-Barishal" = 360        # 6 hours
    "Dhaka-Kuakata" = 480         # 8 hours
    "Dhaka-Bogura" = 300          # 5 hours
    "Chattogram-Cox's Bazar" = 240 # 4 hours
    "Chattogram-Sylhet" = 480      # 8 hours
    "Chattogram-Rangamati" = 120   # 2 hours
    "Chattogram-Khagrachhari" = 180 # 3 hours
    "Chattogram-Cumilla" = 120     # 2 hours
    "Rajshahi-Khulna" = 300        # 5 hours
    "Rajshahi-Rangpur" = 240       # 4 hours
    "Khulna-Barishal" = 180        # 3 hours
    "Khulna-Rajshahi" = 300        # 5 hours
    "Khulna-Jashore" = 60          # 1 hour
    "Sylhet-Chattogram" = 480      # 8 hours
    "Sylhet-Cox's Bazar" = 600     # 10 hours
    "Cumilla-Dhaka" = 120          # 2 hours
    "Mymensingh-Dhaka" = 180       # 3 hours
    "Feni-Dhaka" = 180             # 3 hours
    "Bogura-Rangpur" = 180         # 3 hours
}

function Convert-TimeTo24Hour {
    param([string]$time)
    
    $time = $time.Trim()
    if ($time -match '(\d+):(\d+)\s*(AM|PM)') {
        $hour = [int]$matches[1]
        $minute = [int]$matches[2]
        $period = $matches[3]
        
        if ($period -eq "PM" -and $hour -ne 12) {
            $hour += 12
        } elseif ($period -eq "AM" -and $hour -eq 12) {
            $hour = 0
        }
        
        return "{0:D2}:{1:D2}" -f $hour, $minute
    }
    return $time
}

function Add-Minutes {
    param([string]$time, [int]$minutes)
    
    $parts = $time.Split(":")
    $hours = [int]$parts[0]
    $mins = [int]$parts[1]
    
    $totalMinutes = $hours * 60 + $mins + $minutes
    $newHours = [Math]::Floor($totalMinutes / 60) % 24
    $newMins = $totalMinutes % 60
    
    return "{0:D2}:{1:D2}" -f $newHours, $newMins
}

function Format-Duration {
    param([int]$minutes)
    $hours = [Math]::Floor($minutes / 60)
    $mins = $minutes % 60
    return "{0}:{1:D2}h" -f $hours, $mins
}

$buses = @()
$lines = $busData -split "`n" | Where-Object { $_.Trim() -ne "" }

foreach ($line in $lines) {
    $parts = $line.Split(",")
    if ($parts.Count -ge 5) {
        $no = $parts[0].Trim()
        $route = $parts[1].Trim()
        $operator = $parts[2].Trim()
        $departureTime = $parts[3].Trim()
        $fare = [int]($parts[4].Trim())
        
        # Parse route
        if ($route -match '(.+?)\s+to\s+(.+)') {
            $origin = $matches[1].Trim()
            $destination = $matches[2].Trim()
            
            # Get duration
            $routeKey = "$origin-$destination"
            $durationMins = $durations[$routeKey]
            if (-not $durationMins) {
                $durationMins = 360 # default 6 hours
            }
            
            # Convert time
            $startTime = Convert-TimeTo24Hour $departureTime
            $arrivalTime = Add-Minutes $startTime $durationMins
            $duration = Format-Duration $durationMins
            
            # Create unique bus name with operator and number
            $busName = "$operator (Bus-$no)"
            
            $bus = [PSCustomObject]@{
                busName = $busName
                start = $origin
                destination = $destination
                startTime = $startTime
                arrivalTime = $arrivalTime
                fare = [double]$fare
                duration = $duration
            }
            
            $buses += $bus
        }
    }
}

# Save to JSON
$jsonOutput = $buses | ConvertTo-Json -Depth 10
$jsonOutput | Out-File -FilePath "data/bus_schedules.json" -Encoding UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Bus Schedules Generated Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total buses: $($buses.Count)" -ForegroundColor Green
Write-Host "Saved to: data/bus_schedules.json" -ForegroundColor Yellow
Write-Host ""
Write-Host "Sample entries:"
$buses | Select-Object -First 3 | Format-Table busName, start, destination, startTime, fare -AutoSize
Write-Host ""
Write-Host "Restart the application to see the changes!" -ForegroundColor Cyan
