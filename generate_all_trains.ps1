# Complete train schedules data - all 104 trains
$trainData = @"
701. Subarna Express | Chattogram, 07:00 -> Dhaka, 11:55 | Stops: Chattogram, Feni, Cumilla, Dhaka | Off: Monday | Fare: 495
702. Subarna Express | Dhaka, 14:30 -> Chattogram, 19:25 | Stops: Dhaka, Cumilla, Feni, Chattogram | Off: Monday | Fare: 495
703. Turna Nishitha | Chattogram, 22:00 -> Dhaka, 05:30 | Stops: Chattogram, Feni, Cumilla, Dhaka | Off: Monday | Fare: 510
704. Turna Nishitha | Dhaka, 23:15 -> Chattogram, 06:45 | Stops: Dhaka, Cumilla, Feni, Chattogram | Off: Monday | Fare: 510
705. Mohanagar Provati | Chattogram, 07:20 -> Dhaka, 12:35 | Stops: Chattogram, Feni, Cumilla, Dhaka | Off: Friday | Fare: 505
706. Mohanagar Provati | Dhaka, 15:00 -> Chattogram, 20:15 | Stops: Dhaka, Cumilla, Feni, Chattogram | Off: Friday | Fare: 505
707. Mohanagar Godhuli | Chattogram, 16:00 -> Dhaka, 21:15 | Stops: Chattogram, Feni, Cumilla, Dhaka | Off: No off day | Fare: 505
708. Mohanagar Godhuli | Dhaka, 08:00 -> Chattogram, 13:15 | Stops: Dhaka, Cumilla, Feni, Chattogram | Off: No off day | Fare: 505
709. Sonar Bangla Express | Dhaka, 06:50 -> Chattogram, 12:10 | Stops: Dhaka, Cumilla, Feni, Chattogram | Off: Monday | Fare: 495
710. Sonar Bangla Express | Chattogram, 15:00 -> Dhaka, 20:20 | Stops: Chattogram, Feni, Cumilla, Dhaka | Off: Monday | Fare: 495
711. Paharika Express | Dhaka, 06:40 -> Chattogram, 13:00 | Stops: Dhaka, Cumilla, Feni, Laksham, Chattogram | Off: No off day | Fare: 485
712. Paharika Express | Chattogram, 14:15 -> Dhaka, 20:30 | Stops: Chattogram, Laksham, Feni, Cumilla, Dhaka | Off: No off day | Fare: 485
713. Chattala Express | Dhaka, 21:30 -> Chattogram, 05:05 | Stops: Dhaka, Cumilla, Feni, Chattogram | Off: No off day | Fare: 495
714. Chattala Express | Chattogram, 23:15 -> Dhaka, 06:45 | Stops: Chattogram, Feni, Cumilla, Dhaka | Off: No off day | Fare: 495
715. Mahanagar Provati | Sylhet, 06:30 -> Dhaka, 12:15 | Stops: Sylhet, Moulvibazar, Akhaura, Dhaka | Off: No off day | Fare: 450
716. Mahanagar Provati | Dhaka, 14:45 -> Sylhet, 20:30 | Stops: Dhaka, Akhaura, Moulvibazar, Sylhet | Off: No off day | Fare: 450
717. Parabat Express | Sylhet, 15:30 -> Dhaka, 22:05 | Stops: Sylhet, Moulvibazar, Akhaura, Dhaka | Off: Monday | Fare: 440
718. Parabat Express | Dhaka, 06:45 -> Sylhet, 13:20 | Stops: Dhaka, Akhaura, Moulvibazar, Sylhet | Off: Monday | Fare: 440
719. Kalni Express | Sylhet, 07:45 -> Dhaka, 14:30 | Stops: Sylhet, Moulvibazar, Akhaura, Dhaka | Off: No off day | Fare: 435
720. Kalni Express | Dhaka, 16:30 -> Sylhet, 23:15 | Stops: Dhaka, Akhaura, Moulvibazar, Sylhet | Off: No off day | Fare: 435
721. Upaban Express | Sylhet, 21:50 -> Dhaka, 06:15 | Stops: Sylhet, Moulvibazar, Akhaura, Dhaka | Off: Tuesday | Fare: 445
722. Upaban Express | Dhaka, 22:20 -> Sylhet, 06:45 | Stops: Dhaka, Akhaura, Moulvibazar, Sylhet | Off: Tuesday | Fare: 445
723. Jayantika Express | Sylhet, 14:00 -> Dhaka, 20:30 | Stops: Sylhet, Moulvibazar, Akhaura, Dhaka | Off: No off day | Fare: 440
724. Jayantika Express | Dhaka, 07:00 -> Sylhet, 13:30 | Stops: Dhaka, Akhaura, Moulvibazar, Sylhet | Off: No off day | Fare: 440
725. Sundarban Express | Khulna, 06:20 -> Dhaka, 13:50 | Stops: Khulna, Jessore, Faridpur, Dhaka | Off: No off day | Fare: 420
726. Sundarban Express | Dhaka, 16:00 -> Khulna, 23:30 | Stops: Dhaka, Faridpur, Jessore, Khulna | Off: No off day | Fare: 420
727. Chitra Express | Khulna, 07:15 -> Dhaka, 15:00 | Stops: Khulna, Jessore, Faridpur, Dhaka | Off: No off day | Fare: 415
728. Chitra Express | Dhaka, 17:00 -> Khulna, 00:45 | Stops: Dhaka, Faridpur, Jessore, Khulna | Off: No off day | Fare: 415
729. Sagardari Express | Khulna, 22:00 -> Dhaka, 06:15 | Stops: Khulna, Jessore, Faridpur, Dhaka | Off: Wednesday | Fare: 425
730. Sagardari Express | Dhaka, 23:00 -> Khulna, 07:15 | Stops: Dhaka, Faridpur, Jessore, Khulna | Off: Wednesday | Fare: 425
731. Simanta Express | Khulna, 11:45 -> Dhaka, 19:15 | Stops: Khulna, Jessore, Faridpur, Dhaka | Off: No off day | Fare: 420
732. Simanta Express | Dhaka, 06:30 -> Khulna, 14:00 | Stops: Dhaka, Faridpur, Jessore, Khulna | Off: No off day | Fare: 420
733. Rajshahi Express | Rajshahi, 06:00 -> Dhaka, 13:15 | Stops: Rajshahi, Ishwardi, Pabna, Dhaka | Off: No off day | Fare: 380
734. Rajshahi Express | Dhaka, 16:00 -> Rajshahi, 23:15 | Stops: Dhaka, Pabna, Ishwardi, Rajshahi | Off: No off day | Fare: 380
735. Silk City Express | Rajshahi, 15:30 -> Dhaka, 22:45 | Stops: Rajshahi, Ishwardi, Pabna, Dhaka | Off: Monday | Fare: 375
736. Silk City Express | Dhaka, 07:00 -> Rajshahi, 14:15 | Stops: Dhaka, Pabna, Ishwardi, Rajshahi | Off: Monday | Fare: 375
737. Dhumketu Express | Rajshahi, 22:00 -> Dhaka, 05:45 | Stops: Rajshahi, Ishwardi, Pabna, Dhaka | Off: No off day | Fare: 385
738. Dhumketu Express | Dhaka, 22:30 -> Rajshahi, 06:15 | Stops: Dhaka, Pabna, Ishwardi, Rajshahi | Off: No off day | Fare: 385
739. Padma Express | Rajshahi, 14:15 -> Dhaka, 21:30 | Stops: Rajshahi, Ishwardi, Pabna, Dhaka | Off: Tuesday | Fare: 380
740. Padma Express | Dhaka, 06:45 -> Rajshahi, 14:00 | Stops: Dhaka, Pabna, Ishwardi, Rajshahi | Off: Tuesday | Fare: 380
741. Rangpur Express | Rangpur, 09:00 -> Dhaka, 18:30 | Stops: Rangpur, Bogura, Santahar, Dhaka | Off: No off day | Fare: 465
742. Rangpur Express | Dhaka, 21:00 -> Rangpur, 06:30 | Stops: Dhaka, Santahar, Bogura, Rangpur | Off: No off day | Fare: 465
743. Ekota Express | Dhaka, 11:20 -> Dinajpur, 21:45 | Stops: Dhaka, Santahar, Bogura, Dinajpur | Off: No off day | Fare: 490
744. Ekota Express | Dinajpur, 10:30 -> Dhaka, 21:00 | Stops: Dinajpur, Bogura, Santahar, Dhaka | Off: No off day | Fare: 490
745. Lalmoni Express | Dhaka, 14:00 -> Lalmonirhat, 01:15 | Stops: Dhaka, Santahar, Bogura, Lalmonirhat | Off: Friday | Fare: 505
746. Lalmoni Express | Lalmonirhat, 13:20 -> Dhaka, 00:35 | Stops: Lalmonirhat, Bogura, Santahar, Dhaka | Off: Friday | Fare: 505
747. Nilsagar Express | Dhaka, 09:30 -> Nilphamari, 20:00 | Stops: Dhaka, Santahar, Bogura, Nilphamari | Off: No off day | Fare: 495
748. Nilsagar Express | Nilphamari, 08:45 -> Dhaka, 19:15 | Stops: Nilphamari, Bogura, Santahar, Dhaka | Off: No off day | Fare: 495
749. Drutojan Express | Dhaka, 23:00 -> Panchagarh, 09:50 | Stops: Dhaka, Santahar, Bogura, Panchagarh | Off: No off day | Fare: 510
750. Drutojan Express | Panchagarh, 17:00 -> Dhaka, 03:50 | Stops: Panchagarh, Bogura, Santahar, Dhaka | Off: No off day | Fare: 510
751. Cox's Bazar Express | Dhaka, 06:30 -> Cox's Bazar, 20:00 | Stops: Dhaka, Cumilla, Feni, Chattogram, Cox's Bazar | Off: No off day | Fare: 650
752. Cox's Bazar Express | Cox's Bazar, 07:00 -> Dhaka, 20:30 | Stops: Cox's Bazar, Chattogram, Feni, Cumilla, Dhaka | Off: No off day | Fare: 650
753. Maitree Express | Dhaka, 08:15 -> Kolkata, 18:30 | Stops: Dhaka, Jessore, Kolkata | Off: No off day | Fare: 1200
754. Maitree Express | Kolkata, 08:10 -> Dhaka, 18:30 | Stops: Kolkata, Jessore, Dhaka | Off: No off day | Fare: 1200
755. Bandhan Express | Khulna, 08:10 -> Kolkata, 16:30 | Stops: Khulna, Jessore, Kolkata | Off: No off day | Fare: 1100
756. Bandhan Express | Kolkata, 07:20 -> Khulna, 15:40 | Stops: Kolkata, Jessore, Khulna | Off: No off day | Fare: 1100
757. Kurigram Express | Dhaka, 19:30 -> Kurigram, 06:00 | Stops: Dhaka, Santahar, Bogura, Kurigram | Off: Wednesday | Fare: 500
758. Kurigram Express | Kurigram, 18:00 -> Dhaka, 04:30 | Stops: Kurigram, Bogura, Santahar, Dhaka | Off: Wednesday | Fare: 500
759. Jamalpur Express | Dhaka, 06:50 -> Jamalpur, 12:30 | Stops: Dhaka, Tangail, Mymensingh, Jamalpur | Off: No off day | Fare: 320
760. Jamalpur Express | Jamalpur, 14:00 -> Dhaka, 19:40 | Stops: Jamalpur, Mymensingh, Tangail, Dhaka | Off: No off day | Fare: 320
761. Brahmaputra Express | Dhaka, 15:00 -> Jamalpur, 20:45 | Stops: Dhaka, Tangail, Mymensingh, Jamalpur | Off: Monday | Fare: 315
762. Brahmaputra Express | Jamalpur, 07:30 -> Dhaka, 13:15 | Stops: Jamalpur, Mymensingh, Tangail, Dhaka | Off: Monday | Fare: 315
763. Agnibina Express | Dhaka, 07:20 -> Tarakandi, 14:00 | Stops: Dhaka, Tangail, Mymensingh, Tarakandi | Off: No off day | Fare: 350
764. Agnibina Express | Tarakandi, 14:45 -> Dhaka, 21:25 | Stops: Tarakandi, Mymensingh, Tangail, Dhaka | Off: No off day | Fare: 350
765. Tista Express | Dhaka, 09:00 -> Dewanganj, 14:45 | Stops: Dhaka, Tangail, Mymensingh, Dewanganj | Off: Tuesday | Fare: 340
766. Tista Express | Dewanganj, 15:30 -> Dhaka, 21:15 | Stops: Dewanganj, Mymensingh, Tangail, Dhaka | Off: Tuesday | Fare: 340
767. Egarosindhur Provati | Dhaka, 06:40 -> Kishoreganj, 09:50 | Stops: Dhaka, Narsingdi, Kishoreganj | Off: No off day | Fare: 180
768. Egarosindhur Provati | Kishoreganj, 16:30 -> Dhaka, 19:40 | Stops: Kishoreganj, Narsingdi, Dhaka | Off: No off day | Fare: 180
769. Egarosindhur Godhuli | Dhaka, 15:00 -> Kishoreganj, 18:10 | Stops: Dhaka, Narsingdi, Kishoreganj | Off: No off day | Fare: 180
770. Egarosindhur Godhuli | Kishoreganj, 07:00 -> Dhaka, 10:10 | Stops: Kishoreganj, Narsingdi, Dhaka | Off: No off day | Fare: 180
771. Bhairab Express | Dhaka, 08:30 -> Bhairab Bazar, 10:45 | Stops: Dhaka, Narsingdi, Bhairab Bazar | Off: No off day | Fare: 140
772. Bhairab Express | Bhairab Bazar, 18:00 -> Dhaka, 20:15 | Stops: Bhairab Bazar, Narsingdi, Dhaka | Off: No off day | Fare: 140
773. Madhumati Express | Dhaka, 07:30 -> Goalanda, 11:15 | Stops: Dhaka, Faridpur, Goalanda | Off: No off day | Fare: 220
774. Madhumati Express | Goalanda, 16:00 -> Dhaka, 19:45 | Stops: Goalanda, Faridpur, Dhaka | Off: No off day | Fare: 220
775. Padma Express | Dhaka, 06:00 -> Rajbari, 10:30 | Stops: Dhaka, Faridpur, Rajbari | Off: No off day | Fare: 240
776. Padma Express | Rajbari, 16:45 -> Dhaka, 21:15 | Stops: Rajbari, Faridpur, Dhaka | Off: No off day | Fare: 240
777. Tungipara Express | Dhaka, 14:30 -> Tungipara, 19:00 | Stops: Dhaka, Faridpur, Tungipara | Off: Friday | Fare: 250
778. Tungipara Express | Tungipara, 06:00 -> Dhaka, 10:30 | Stops: Tungipara, Faridpur, Dhaka | Off: Friday | Fare: 250
779. Barendra Express | Dhaka, 08:00 -> Parbatipur, 17:30 | Stops: Dhaka, Santahar, Bogura, Parbatipur | Off: No off day | Fare: 475
780. Barendra Express | Parbatipur, 09:30 -> Dhaka, 19:00 | Stops: Parbatipur, Bogura, Santahar, Dhaka | Off: No off day | Fare: 475
781. Chilahati Express | Dhaka, 20:15 -> Chilahati, 07:00 | Stops: Dhaka, Santahar, Bogura, Chilahati | Off: Wednesday | Fare: 520
782. Chilahati Express | Chilahati, 15:00 -> Dhaka, 01:45 | Stops: Chilahati, Bogura, Santahar, Dhaka | Off: Wednesday | Fare: 520
783. Sirajganj Express | Dhaka, 07:00 -> Sirajganj, 12:15 | Stops: Dhaka, Tangail, Sirajganj | Off: No off day | Fare: 280
784. Sirajganj Express | Sirajganj, 14:30 -> Dhaka, 19:45 | Stops: Sirajganj, Tangail, Dhaka | Off: No off day | Fare: 280
785. Jamuna Express | Dhaka, 15:30 -> Sirajganj, 20:45 | Stops: Dhaka, Tangail, Sirajganj | Off: Monday | Fare: 280
786. Jamuna Express | Sirajganj, 06:30 -> Dhaka, 11:45 | Stops: Sirajganj, Tangail, Dhaka | Off: Monday | Fare: 280
787. Netrokona Express | Dhaka, 06:30 -> Netrokona, 13:30 | Stops: Dhaka, Tangail, Mymensingh, Netrokona | Off: No off day | Fare: 360
788. Netrokona Express | Netrokona, 14:15 -> Dhaka, 21:15 | Stops: Netrokona, Mymensingh, Tangail, Dhaka | Off: No off day | Fare: 360
789. Mohanganj Express | Dhaka, 16:00 -> Mohanganj, 22:00 | Stops: Dhaka, Narsingdi, Bhairab Bazar, Mohanganj | Off: Tuesday | Fare: 290
790. Mohanganj Express | Mohanganj, 06:00 -> Dhaka, 12:00 | Stops: Mohanganj, Bhairab Bazar, Narsingdi, Dhaka | Off: Tuesday | Fare: 290
791. Commuter Train | Dhaka, 06:00 -> Narayanganj, 07:00 | Stops: Dhaka, Narayanganj | Off: No off day | Fare: 40
792. Commuter Train | Narayanganj, 17:00 -> Dhaka, 18:00 | Stops: Narayanganj, Dhaka | Off: No off day | Fare: 40
793. Airport Express | Dhaka, 08:00 -> Airport, 08:30 | Stops: Dhaka, Airport | Off: No off day | Fare: 80
794. Airport Express | Airport, 18:30 -> Dhaka, 19:00 | Stops: Airport, Dhaka | Off: No off day | Fare: 80
795. Gazipur Express | Dhaka, 07:30 -> Gazipur, 08:45 | Stops: Dhaka, Tongi, Gazipur | Off: No off day | Fare: 70
796. Gazipur Express | Gazipur, 17:15 -> Dhaka, 18:30 | Stops: Gazipur, Tongi, Dhaka | Off: No off day | Fare: 70
797. Tangail Commuter | Dhaka, 06:15 -> Tangail, 09:30 | Stops: Dhaka, Gazipur, Tangail | Off: No off day | Fare: 150
798. Tangail Commuter | Tangail, 16:00 -> Dhaka, 19:15 | Stops: Tangail, Gazipur, Dhaka | Off: No off day | Fare: 150
799. Mymensingh Express | Dhaka, 06:00 -> Mymensingh, 10:30 | Stops: Dhaka, Gazipur, Tangail, Mymensingh | Off: No off day | Fare: 200
800. Mymensingh Express | Mymensingh, 15:30 -> Dhaka, 20:00 | Stops: Mymensingh, Tangail, Gazipur, Dhaka | Off: No off day | Fare: 200
801. Nalitabari Express | Dhaka, 14:00 -> Nalitabari, 21:30 | Stops: Dhaka, Tangail, Mymensingh, Nalitabari | Off: Wednesday | Fare: 375
802. Nalitabari Express | Nalitabari, 06:00 -> Dhaka, 13:30 | Stops: Nalitabari, Mymensingh, Tangail, Dhaka | Off: Wednesday | Fare: 375
803. Intercity Express | Dhaka, 09:00 -> Cumilla, 12:30 | Stops: Dhaka, Cumilla | Off: No off day | Fare: 250
804. Intercity Express | Cumilla, 15:00 -> Dhaka, 18:30 | Stops: Cumilla, Dhaka | Off: No off day | Fare: 250
"@

function Parse-Time {
    param([string]$time)
    $parts = $time.Trim() -split ':'
    return @{
        Hour = [int]$parts[0]
        Minute = [int]$parts[1]
    }
}

function Calculate-Duration {
    param([string]$start, [string]$arrival)
    
    $startTime = Parse-Time $start
    $arrivalTime = Parse-Time $arrival
    
    $startMinutes = $startTime.Hour * 60 + $startTime.Minute
    $arrivalMinutes = $arrivalTime.Hour * 60 + $arrivalTime.Minute
    
    $diff = $arrivalMinutes - $startMinutes
    if ($diff -lt 0) { $diff += 1440 }
    
    $hours = [Math]::Floor($diff / 60)
    $minutes = $diff % 60
    
    return "$($hours):$($minutes.ToString('00'))h"
}

function Calculate-Fare-Per-Km {
    param([double]$totalFare, [int]$numStops)
    # Simple fare calculation: divide total by number of segments
    if ($numStops -le 1) { return $totalFare }
    return [Math]::Round($totalFare / ($numStops - 1), 2)
}

# Parse all trains
$trains = @()
$lines = $trainData -split "`n" | Where-Object { $_.Trim() -ne "" }

foreach ($line in $lines) {
    try {
        # Parse format: "701. Subarna Express | Chattogram, 07:00 -> Dhaka, 11:55 | Stops: ... | Off: ... | Fare: 495"
        if ($line -match '(\d+)\.\s+(.+?)\s+\|\s+(.+?),\s+(\d+:\d+)\s+->\s+(.+?),\s+(\d+:\d+)\s+\|\s+Stops:\s+(.+?)\s+\|\s+Off:\s+(.+?)\s+\|\s+Fare:\s+(\d+)') {
            $trainNo = $matches[1]
            $trainName = $matches[2].Trim()
            $startStation = $matches[3].Trim()
            $startTime = $matches[4].Trim()
            $destStation = $matches[5].Trim()
            $arrivalTime = $matches[6].Trim()
            $stopsText = $matches[7].Trim()
            $offDay = $matches[8].Trim()
            $fare = [double]$matches[9]
            
            # Parse stops
            $stopsList = $stopsText -split ',' | ForEach-Object { $_.Trim() }
            
            # Calculate duration
            $duration = Calculate-Duration $startTime $arrivalTime
            
            # Build stops array with cumulative fares
            $stops = @()
            $farePerSegment = Calculate-Fare-Per-Km $fare $stopsList.Count
            
            for ($i = 0; $i -lt $stopsList.Count; $i++) {
                $cumulativeFare = [Math]::Round($farePerSegment * $i, 2)
                if ($i -eq 0) {
                    $stopTime = $startTime
                } elseif ($i -eq $stopsList.Count - 1) {
                    $stopTime = $arrivalTime
                } else {
                    # Interpolate time for intermediate stops
                    $startParsed = Parse-Time $startTime
                    $arrivalParsed = Parse-Time $arrivalTime
                    $startMinutes = $startParsed.Hour * 60 + $startParsed.Minute
                    $arrivalMinutes = $arrivalParsed.Hour * 60 + $arrivalParsed.Minute
                    if ($arrivalMinutes -lt $startMinutes) { $arrivalMinutes += 1440 }
                    
                    $totalMinutes = $arrivalMinutes - $startMinutes
                    $segmentMinutes = [Math]::Floor($totalMinutes * $i / ($stopsList.Count - 1))
                    $stopMinutes = $startMinutes + $segmentMinutes
                    
                    $hour = [Math]::Floor($stopMinutes / 60) % 24
                    $min = $stopMinutes % 60
                    $stopTime = "$($hour.ToString('00')):$($min.ToString('00'))"
                }
                
                $stops += @{
                    station = $stopsList[$i]
                    arrivalTime = $stopTime
                    departureTime = $stopTime
                    cumulativeFare = $cumulativeFare
                }
            }
            
            # Normalize off day
            if ($offDay -eq "No off day") {
                $offDay = "None"
            }
            
            $train = @{
                trainName = "$trainName($trainNo)"
                start = $startStation
                destination = $destStation
                startTime = $startTime
                arrivalTime = $arrivalTime
                fare = $fare
                duration = $duration
                offDay = $offDay
                stops = $stops
            }
            
            $trains += $train
            Write-Host "Processed: $trainName($trainNo) - $startStation -> $destStation"
        }
    } catch {
        Write-Host "Error processing line: $line" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

# Convert to JSON and save
$json = $trains | ConvertTo-Json -Depth 10
$outputFile = "e:\GitHub\temp\-Travel-Schedule-Manager\data\train_schedules.json"
$json | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "`nSuccessfully generated $($trains.Count) train schedules" -ForegroundColor Green
Write-Host "Output file: $outputFile" -ForegroundColor Green
