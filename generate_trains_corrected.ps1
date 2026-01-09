# Corrected train schedules data
$trainData = @"
701 Subarna Express | Chattogram 07:00 -> Dhaka 11:55 | Stops: Chattogram, Dhaka | Off: Mon | Fare: 495
702 Subarna Express | Dhaka 16:30 -> Chattogram 21:25 | Stops: Dhaka, Chattogram | Off: Mon | Fare: 495

703 Mohonagar Godhuli | Chattogram 15:00 -> Dhaka 20:45 | Stops: Chattogram, Feni, Cumilla, Brahmanbaria, Dhaka | Off: Sun | Fare: 450
704 Mohonagar Provati | Dhaka 07:45 -> Chattogram 13:35 | Stops: Dhaka, Brahmanbaria, Cumilla, Feni, Chattogram | Off: None | Fare: 450

705 Ekota Express | Dhaka 10:15 -> Panchagarh 21:00 | Stops: Dhaka, Tangail, Sirajganj, Natore, Naogaon, Joypurhat, Dinajpur, Thakurgaon, Panchagarh | Off: None | Fare: 740
706 Ekota Express | Panchagarh 21:10 -> Dhaka 07:20 | Stops: Panchagarh, Thakurgaon, Dinajpur, Joypurhat, Naogaon, Natore, Sirajganj, Tangail, Dhaka | Off: None | Fare: 740

707 Tista Express | Dhaka 07:30 -> Jamalpur 11:36 | Stops: Dhaka, Mymensingh(10:32-10:35,145), Jamalpur | Off: Mon | Fare: 210
708 Tista Express | Jamalpur 15:56 -> Dhaka 20:30 | Stops: Jamalpur, Mymensingh(17:03-17:06,65), Dhaka | Off: Mon | Fare: 210

709 Parabat Express | Dhaka 06:30 -> Sylhet 13:00 | Stops: Dhaka, Brahmanbaria, Moulvibazar, Sylhet | Off: Mon | Fare: 410
710 Parabat Express | Sylhet 16:00 -> Dhaka 22:40 | Stops: Sylhet, Moulvibazar, Brahmanbaria, Dhaka | Off: Mon | Fare: 410

711 Upukul Express | Noakhali 06:00 -> Dhaka 11:20 | Stops: Noakhali, Feni, Cumilla, Brahmanbaria, Dhaka | Off: Wed | Fare: 335
712 Upukul Express | Dhaka 15:10 -> Noakhali 20:40 | Stops: Dhaka, Brahmanbaria, Cumilla, Feni, Noakhali | Off: Tue | Fare: 335

713 Karatoa Express | Naogaon 09:25 -> Lalmonirhat 16:00 | Stops: Naogaon, Bogura, Gaibandha, Rangpur, Lalmonirhat | Off: Wed | Fare: 155
714 Karatoa Express | Lalmonirhat 16:20 -> Naogaon 22:40 | Stops: Lalmonirhat, Rangpur, Gaibandha, Bogura, Naogaon | Off: Wed | Fare: 155

715 Kapotaksha Express | Khulna 06:45 -> Rajshahi 12:20 | Stops: Khulna, Jashore, Jhenaidah, Chuadanga, Kushtia, Rajshahi | Off: Fri | Fare: 310
716 Kapotaksha Express | Rajshahi 14:30 -> Khulna 20:25 | Stops: Rajshahi, Kushtia, Chuadanga, Jhenaidah, Jashore, Khulna | Off: Fri | Fare: 310

717 Jayantika Express | Dhaka 11:15 -> Sylhet 19:00 | Stops: Dhaka, Brahmanbaria, Moulvibazar, Sylhet | Off: Tue | Fare: 410
718 Jayantika Express | Sylhet 12:00 -> Dhaka 19:15 | Stops: Sylhet, Moulvibazar, Brahmanbaria, Dhaka | Off: Thu | Fare: 410

719 Paharika Express | Chattogram 07:50 -> Sylhet 15:55 | Stops: Chattogram, Feni, Cumilla, Brahmanbaria, Moulvibazar, Sylhet | Off: Mon | Fare: 410
720 Paharika Express | Sylhet 10:30 -> Chattogram 18:55 | Stops: Sylhet, Moulvibazar, Brahmanbaria, Cumilla, Feni, Chattogram | Off: Sat | Fare: 410

721 Mohonagar Provati | Chattogram 12:30 -> Dhaka 18:40 | Stops: Chattogram, Feni, Cumilla, Brahmanbaria, Dhaka | Off: Sun | Fare: 450
722 Mohonagar Godhuli | Dhaka 21:20 -> Chattogram 03:30 | Stops: Dhaka, Brahmanbaria, Cumilla, Feni, Chattogram | Off: Sun | Fare: 450

723 Udayan Express | Chattogram 21:45 -> Sylhet 05:45 | Stops: Chattogram, Feni, Cumilla, Brahmanbaria, Moulvibazar, Sylhet | Off: Sat | Fare: 410
724 Udayan Express | Sylhet 22:00 -> Chattogram 05:35 | Stops: Sylhet, Moulvibazar, Brahmanbaria, Cumilla, Feni, Chattogram | Off: Sun | Fare: 410

725 Sundarban Express | Khulna 21:45 -> Dhaka 05:10 | Stops: Khulna, Jashore, Chuadanga, Kushtia, Tangail, Dhaka | Off: Tue | Fare: 625
726 Sundarban Express | Dhaka 08:00 -> Khulna 15:40 | Stops: Dhaka, Tangail, Kushtia, Chuadanga, Jashore, Khulna | Off: Wed | Fare: 625

727 Rupsha Express | Khulna 07:15 -> Nilphamari 17:00 | Stops: Khulna, Jashore, Chuadanga, Kushtia, Natore, Naogaon, Joypurhat, Dinajpur, Nilphamari | Off: Thu | Fare: 450
728 Rupsha Express | Nilphamari 08:30 -> Khulna 18:25 | Stops: Nilphamari, Dinajpur, Joypurhat, Naogaon, Natore, Kushtia, Chuadanga, Jashore, Khulna | Off: Thu | Fare: 450

729 Meghna Express | Chattogram 17:15 -> Chandpur 21:25 | Stops: Chattogram, Feni, Cumilla, Chandpur | Off: None | Fare: 135
730 Meghna Express | Chandpur 05:00 -> Chattogram 09:00 | Stops: Chandpur, Cumilla, Feni, Chattogram | Off: None | Fare: 135

731 Barendra Express | Rajshahi 15:00 -> Nilphamari 21:30 | Stops: Rajshahi, Natore, Naogaon, Joypurhat, Dinajpur, Nilphamari | Off: Sun | Fare: 215
732 Barendra Express | Nilphamari 05:00 -> Rajshahi 11:10 | Stops: Nilphamari, Dinajpur, Joypurhat, Naogaon, Natore, Rajshahi | Off: Sun | Fare: 215

733 Titumir Express | Rajshahi 06:20 -> Nilphamari 13:00 | Stops: Rajshahi, Natore, Naogaon, Joypurhat, Dinajpur, Nilphamari | Off: Wed | Fare: 215
734 Titumir Express | Nilphamari 15:00 -> Rajshahi 21:30 | Stops: Nilphamari, Dinajpur, Joypurhat, Naogaon, Natore, Rajshahi | Off: Wed | Fare: 215

735 Agnibina | Dhaka 11:30 -> Jamalpur 15:38 | Stops: Dhaka, Mymensingh(14:35-14:38,145), Jamalpur | Off: None | Fare: 210
736 Agnibina | Jamalpur 18:41 -> Dhaka 22:55 | Stops: Jamalpur, Mymensingh(19:45-19:48,65), Dhaka | Off: None | Fare: 210

737 Egarosindhur Provati | Dhaka 07:15 -> Kishoreganj 11:10 | Stops: Dhaka, Narsingdi, Kishoreganj | Off: Wed | Fare: 180
738 Egarosindhur Provati | Kishoreganj 06:30 -> Dhaka 10:35 | Stops: Kishoreganj, Narsingdi, Dhaka | Off: None | Fare: 180

739 Upoban Express | Dhaka 22:00 -> Sylhet 05:00 | Stops: Dhaka, Brahmanbaria, Moulvibazar, Sylhet | Off: Wed | Fare: 410
740 Upoban Express | Sylhet 23:30 -> Dhaka 05:40 | Stops: Sylhet, Moulvibazar, Brahmanbaria, Dhaka | Off: None | Fare: 410

741 Turna Express | Chattogram 23:30 -> Dhaka 05:10 | Stops: Chattogram, Feni, Cumilla, Brahmanbaria, Dhaka | Off: None | Fare: 450
742 Turna Express | Dhaka 23:15 -> Chattogram 05:15 | Stops: Dhaka, Brahmanbaria, Cumilla, Feni, Chattogram | Off: None | Fare: 450

743 Brahmaputra Express | Dhaka 18:15 -> Jamalpur 22:39 | Stops: Dhaka, Mymensingh(21:23-21:28,145), Jamalpur | Off: None | Fare: 210
744 Brahmaputra Express | Jamalpur 07:41 -> Dhaka 12:15 | Stops: Jamalpur, Mymensingh(08:50-08:55,65), Dhaka | Off: None | Fare: 210

745 Jamuna Express | Dhaka 16:45 -> Jamalpur 22:05 | Stops: Dhaka, Mymensingh(20:32-20:37,145), Jamalpur | Off: None | Fare: 210
746 Jamuna Express | Jamalpur 03:11 -> Dhaka 08:00 | Stops: Jamalpur, Mymensingh(04:25-04:30,65), Dhaka | Off: None | Fare: 210

747 Simanta Express | Khulna 21:15 -> Nilphamari 06:45 | Stops: Khulna, Jashore, Chuadanga, Kushtia, Natore, Naogaon, Joypurhat, Dinajpur, Nilphamari | Off: Mon | Fare: 450
748 Simanta Express | Nilphamari 18:30 -> Khulna 04:10 | Stops: Nilphamari, Dinajpur, Joypurhat, Naogaon, Natore, Kushtia, Chuadanga, Jashore, Khulna | Off: Mon | Fare: 450

749 Egarosindhur Godhuli | Dhaka 18:45 -> Kishoreganj 22:40 | Stops: Dhaka, Narsingdi, Kishoreganj | Off: None | Fare: 180
750 Egarosindhur Godhuli | Kishoreganj 12:50 -> Dhaka 16:45 | Stops: Kishoreganj, Narsingdi, Dhaka | Off: Wed | Fare: 180

751 Lalmoni Express | Dhaka 21:45 -> Lalmonirhat 07:20 | Stops: Dhaka, Tangail, Natore, Naogaon, Bogura, Gaibandha, Lalmonirhat | Off: Fri | Fare: 505
752 Lalmoni Express | Lalmonirhat 09:50 -> Dhaka 19:45 | Stops: Lalmonirhat, Gaibandha, Bogura, Naogaon, Natore, Tangail, Dhaka | Off: Fri | Fare: 505

753 Silkcity Express | Dhaka 14:30 -> Rajshahi 20:20 | Stops: Dhaka, Tangail, Sirajganj, Natore, Rajshahi | Off: Sun | Fare: 450
754 Silkcity Express | Rajshahi 07:40 -> Dhaka 13:10 | Stops: Rajshahi, Natore, Sirajganj, Tangail, Dhaka | Off: Sun | Fare: 450

755 Madhumati Express | Dhaka 15:00 -> Rajshahi 22:30 | Stops: Dhaka, Faridpur, Rajbari, Kushtia, Rajshahi | Off: Thu | Fare: 175
756 Madhumati Express | Rajshahi 06:40 -> Dhaka 14:00 | Stops: Rajshahi, Kushtia, Rajbari, Faridpur, Dhaka | Off: Thu | Fare: 175

757 Drutojan Express | Dhaka 20:45 -> Panchagarh 07:10 | Stops: Dhaka, Tangail, Sirajganj, Natore, Naogaon, Joypurhat, Dinajpur, Thakurgaon, Panchagarh | Off: Wed | Fare: 740
758 Drutojan Express | Panchagarh 07:20 -> Dhaka 18:55 | Stops: Panchagarh, Thakurgaon, Dinajpur, Joypurhat, Naogaon, Natore, Sirajganj, Tangail, Dhaka | Off: Wed | Fare: 740

759 Padma Express | Dhaka 22:45 -> Rajshahi 04:00 | Stops: Dhaka, Tangail, Sirajganj, Natore, Rajshahi | Off: Tue | Fare: 450
760 Padma Express | Rajshahi 16:00 -> Dhaka 21:15 | Stops: Rajshahi, Natore, Sirajganj, Tangail, Dhaka | Off: Tue | Fare: 450

761 Sagardari Express | Khulna 16:00 -> Rajshahi 22:00 | Stops: Khulna, Jashore, Jhenaidah, Chuadanga, Kushtia, Rajshahi | Off: Mon | Fare: 310
762 Sagardari Express | Rajshahi 06:00 -> Khulna 12:10 | Stops: Rajshahi, Kushtia, Chuadanga, Jhenaidah, Jashore, Khulna | Off: Mon | Fare: 310

763 Chitra Express | Khulna 09:00 -> Dhaka 18:05 | Stops: Khulna, Jashore, Chuadanga, Kushtia, Tangail, Dhaka | Off: Mon | Fare: 505
764 Chitra Express | Dhaka 19:30 -> Khulna 04:40 | Stops: Dhaka, Tangail, Kushtia, Chuadanga, Jashore, Khulna | Off: Mon | Fare: 505

765 Nilsagor Express | Dhaka 06:45 -> Nilphamari 16:00 | Stops: Dhaka, Tangail, Sirajganj, Naogaon, Joypurhat, Dinajpur, Nilphamari | Off: Mon | Fare: 465
766 Nilsagor Express | Nilphamari 20:00 -> Dhaka 05:25 | Stops: Nilphamari, Dinajpur, Joypurhat, Naogaon, Sirajganj, Tangail, Dhaka | Off: Sun | Fare: 465

767 Sirajganj Express | Dhaka 06:00 -> Sirajganj 10:15 | Stops: Dhaka, Tangail, Sirajganj | Off: Sat | Fare: 240
768 Sirajganj Express | Sirajganj 16:15 -> Dhaka 20:10 | Stops: Sirajganj, Tangail, Dhaka | Off: Sat | Fare: 240

769 Dhumketu Express | Dhaka 06:00 -> Rajshahi 11:40 | Stops: Dhaka, Tangail, Sirajganj, Natore, Rajshahi | Off: Sat | Fare: 450
770 Dhumketu Express | Rajshahi 23:20 -> Dhaka 04:40 | Stops: Rajshahi, Natore, Sirajganj, Tangail, Dhaka | Off: Thu | Fare: 450

771 Rangpur Express | Dhaka 09:10 -> Rangpur 19:00 | Stops: Dhaka, Tangail, Sirajganj, Natore, Naogaon, Bogura, Gaibandha, Rangpur | Off: Mon | Fare: 505
772 Rangpur Express | Rangpur 20:00 -> Dhaka 06:00 | Stops: Rangpur, Gaibandha, Bogura, Naogaon, Natore, Sirajganj, Tangail, Dhaka | Off: Sun | Fare: 505

773 Kalni Express | Dhaka 14:55 -> Sylhet 21:30 | Stops: Dhaka, Brahmanbaria, Moulvibazar, Sylhet | Off: Fri | Fare: 410
774 Kalni Express | Sylhet 06:15 -> Dhaka 12:55 | Stops: Sylhet, Moulvibazar, Brahmanbaria, Dhaka | Off: Fri | Fare: 410

775 Haor Express | Dhaka 22:15 -> Netrokona 04:10 | Stops: Dhaka, Kishoreganj, Netrokona | Off: Wed | Fare: 295
776 Haor Express | Netrokona 08:00 -> Dhaka 13:55 | Stops: Netrokona, Kishoreganj, Dhaka | Off: Thu | Fare: 295

777 Kishorgonj Express | Dhaka 10:30 -> Kishoreganj 14:10 | Stops: Dhaka, Narsingdi, Kishoreganj | Off: Fri | Fare: 180
778 Kishorgonj Express | Kishoreganj 16:00 -> Dhaka 20:00 | Stops: Kishoreganj, Narsingdi, Dhaka | Off: Fri | Fare: 180

779 Tungipara Express | Gopalganj 06:30 -> Rajshahi 13:15 | Stops: Gopalganj, Rajbari, Kushtia, Rajshahi | Off: Mon | Fare: 285
780 Tungipara Express | Rajshahi 15:30 -> Gopalganj 22:10 | Stops: Rajshahi, Kushtia, Rajbari, Gopalganj | Off: Tue | Fare: 285

781 Bijoy Express | Chattogram 09:15 -> Jamalpur 18:00 | Stops: Chattogram, Feni, Cumilla, Kishoreganj, Mymensingh, Jamalpur | Off: Wed | Fare: 375
782 Bijoy Express | Jamalpur 20:00 -> Chattogram 05:00 | Stops: Jamalpur, Mymensingh, Kishoreganj, Cumilla, Feni, Chattogram | Off: Tue | Fare: 375

787 Sonar Bangla Express | Chattogram 17:00 -> Dhaka 21:55 | Stops: Chattogram, Dhaka | Off: Wed | Fare: 495
788 Sonar Bangla Express | Dhaka 07:00 -> Chattogram 11:55 | Stops: Dhaka, Chattogram | Off: Tue | Fare: 495

789 Mohanganj Express | Dhaka 13:15 -> Mymensingh 16:10 | Stops: Dhaka, Mymensingh | Off: Mon | Fare: 145
790 Mohanganj Express | Mymensingh 01:30 -> Dhaka 04:55 | Stops: Mymensingh, Dhaka | Off: Mon | Fare: 145

791 Banalata Express | Dhaka 13:30 -> Chapainawabganj 19:15 | Stops: Dhaka, Chapainawabganj | Off: Fri | Fare: 565
792 Banalata Express | Chapainawabganj 06:00 -> Dhaka 11:35 | Stops: Chapainawabganj, Dhaka | Off: Fri | Fare: 565

793 Panchagarh Express | Dhaka 23:30 -> Panchagarh 09:50 | Stops: Dhaka, Tangail, Naogaon, Dinajpur, Thakurgaon, Panchagarh | Off: None | Fare: 740
794 Panchagarh Express | Panchagarh 12:10 -> Dhaka 22:10 | Stops: Panchagarh, Thakurgaon, Dinajpur, Naogaon, Tangail, Dhaka | Off: None | Fare: 740

795 Benapole Express | Jashore 12:25 -> Dhaka 20:30 | Stops: Jashore, Kushtia, Dhaka | Off: Wed | Fare: 600
796 Benapole Express | Dhaka 23:30 -> Jashore 07:00 | Stops: Dhaka, Kushtia, Jashore | Off: Wed | Fare: 600

797 Kurigram Express | Dhaka 20:00 -> Kurigram 05:10 | Stops: Dhaka, Tangail, Natore, Naogaon, Bogura, Rangpur, Kurigram | Off: Wed | Fare: 510
798 Kurigram Express | Kurigram 07:10 -> Dhaka 17:05 | Stops: Kurigram, Rangpur, Bogura, Naogaon, Natore, Tangail, Dhaka | Off: Wed | Fare: 510

799 Jamalpur Express | Dhaka 10:00 -> Jamalpur 14:25 | Stops: Dhaka, Mymensingh(13:05-13:10,145), Jamalpur | Off: Sunday | Fare: 210
800 Jamalpur Express | Jamalpur 19:25 -> Dhaka 23:55 | Stops: Jamalpur, Mymensingh(20:35-20:40,65), Dhaka | Off: Sunday | Fare: 210

801 Chattala Express | Chattogram 06:00 -> Dhaka 12:40 | Stops: Chattogram, Feni, Cumilla, Brahmanbaria, Narsingdi, Dhaka | Off: Tue | Fare: 450
802 Chattala Express | Dhaka 14:15 -> Chattogram 20:30 | Stops: Dhaka, Narsingdi, Brahmanbaria, Cumilla, Feni, Chattogram | Off: Tue | Fare: 450

803 Bangla Bandha Express | Rajshahi 21:00 -> Panchagarh 04:40 | Stops: Rajshahi, Natore, Naogaon, Joypurhat, Dinajpur, Thakurgaon, Panchagarh | Off: Fri | Fare: 275
804 Bangla Bandha Express | Panchagarh 09:00 -> Rajshahi 17:15 | Stops: Panchagarh, Thakurgaon, Dinajpur, Joypurhat, Naogaon, Natore, Rajshahi | Off: Fri | Fare: 275

813 Cox's Bazar Express | Cox's Bazar 12:30 -> Dhaka 21:00 | Stops: Cox's Bazar, Chattogram, Dhaka | Off: Tue | Fare: 695
814 Cox's Bazar Express | Dhaka 23:00 -> Cox's Bazar 07:20 | Stops: Dhaka, Chattogram, Cox's Bazar | Off: Mon | Fare: 695

815 Parjotak Express | Cox's Bazar 19:45 -> Dhaka 04:20 | Stops: Cox's Bazar, Chattogram, Dhaka | Off: Wed | Fare: 695
816 Parjotak Express | Dhaka 06:15 -> Cox's Bazar 14:40 | Stops: Dhaka, Chattogram, Cox's Bazar | Off: Wed | Fare: 695
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

function Normalize-OffDay {
    param([string]$offDay)
    
    $day = $offDay.Trim()
    switch ($day) {
        "Mon" { return "Monday" }
        "Tue" { return "Tuesday" }
        "Wed" { return "Wednesday" }
        "Thu" { return "Thursday" }
        "Fri" { return "Friday" }
        "Sat" { return "Saturday" }
        "Sun" { return "Sunday" }
        "Sunday" { return "Sunday" }
        "None" { return "None" }
        default { return "None" }
    }
}

function Parse-Stop {
    param([string]$stopText)
    
    $stopText = $stopText.Trim()
    
    # Check if stop has timing info like "Mymensingh(10:32-10:35,145)"
    if ($stopText -match '^(.+?)\((\d+:\d+)-(\d+:\d+),(\d+(?:\.\d+)?)\)$') {
        return @{
            Station = $matches[1].Trim()
            ArrivalTime = $matches[2].Trim()
            DepartureTime = $matches[3].Trim()
            Fare = [double]$matches[4].Trim()
        }
    } else {
        return @{
            Station = $stopText
            ArrivalTime = $null
            DepartureTime = $null
            Fare = $null
        }
    }
}

# Parse all trains
$trains = @()
$lines = $trainData -split "`n" | Where-Object { $_.Trim() -ne "" }

foreach ($line in $lines) {
    try {
        # Parse format: "701 Subarna Express | Chattogram 07:00 -> Dhaka 11:55 | Stops: ... | Off: ... | Fare: 495"
        if ($line -match '^(\d+)\s+(.+?)\s+\|\s+(.+?)\s+(\d+:\d+)\s+->\s+(.+?)\s+(\d+:\d+)\s+\|\s+Stops:\s+(.+?)\s+\|\s+Off:\s+(.+?)\s+\|\s+Fare:\s+(\d+)') {
            $trainNo = $matches[1]
            $trainName = $matches[2].Trim()
            $startStation = $matches[3].Trim()
            $startTime = $matches[4].Trim()
            $destStation = $matches[5].Trim()
            $arrivalTime = $matches[6].Trim()
            $stopsText = $matches[7].Trim()
            $offDay = $matches[8].Trim()
            $fare = [double]$matches[9]
            
            # Parse stops - need to handle stops with parentheses containing commas
            $parsedStops = @()
            $currentStop = ""
            $inParentheses = $false
            
            for ($i = 0; $i -lt $stopsText.Length; $i++) {
                $char = $stopsText[$i]
                
                if ($char -eq '(') {
                    $inParentheses = $true
                    $currentStop += $char
                } elseif ($char -eq ')') {
                    $inParentheses = $false
                    $currentStop += $char
                } elseif ($char -eq ',' -and -not $inParentheses) {
                    # End of stop, parse it
                    if ($currentStop.Trim() -ne "") {
                        $parsedStops += Parse-Stop $currentStop.Trim()
                    }
                    $currentStop = ""
                } else {
                    $currentStop += $char
                }
            }
            
            # Don't forget the last stop
            if ($currentStop.Trim() -ne "") {
                $parsedStops += Parse-Stop $currentStop.Trim()
            }
            
            # Calculate duration
            $duration = Calculate-Duration $startTime $arrivalTime
            
            # Normalize off day
            $normalizedOffDay = Normalize-OffDay $offDay
            
            # Build stops array with cumulative fares
            $stops = @()
            $farePerSegment = $fare / ($parsedStops.Count - 1)
            
            for ($i = 0; $i -lt $parsedStops.Count; $i++) {
                $parsedStop = $parsedStops[$i]
                
                # Determine arrival/departure times
                if ($parsedStop.ArrivalTime -ne $null) {
                    # Stop has explicit timing
                    $stopArrival = $parsedStop.ArrivalTime
                    $stopDeparture = $parsedStop.DepartureTime
                    $cumulativeFare = $parsedStop.Fare
                } elseif ($i -eq 0) {
                    # First stop
                    $stopArrival = $startTime
                    $stopDeparture = $startTime
                    $cumulativeFare = 0.0
                } elseif ($i -eq $parsedStops.Count - 1) {
                    # Last stop
                    $stopArrival = $arrivalTime
                    $stopDeparture = $arrivalTime
                    $cumulativeFare = $fare
                } else {
                    # Interpolate time for intermediate stops
                    $startParsed = Parse-Time $startTime
                    $arrivalParsed = Parse-Time $arrivalTime
                    $startMinutes = $startParsed.Hour * 60 + $startParsed.Minute
                    $arrivalMinutes = $arrivalParsed.Hour * 60 + $arrivalParsed.Minute
                    if ($arrivalMinutes -lt $startMinutes) { $arrivalMinutes += 1440 }
                    
                    $totalMinutes = $arrivalMinutes - $startMinutes
                    $segmentMinutes = [Math]::Floor($totalMinutes * $i / ($parsedStops.Count - 1))
                    $stopMinutes = $startMinutes + $segmentMinutes
                    
                    $hour = [Math]::Floor($stopMinutes / 60) % 24
                    $min = $stopMinutes % 60
                    $stopArrival = "$($hour.ToString('00')):$($min.ToString('00'))"
                    $stopDeparture = $stopArrival
                    $cumulativeFare = [Math]::Round($farePerSegment * $i, 2)
                }
                
                $stops += @{
                    station = $parsedStop.Station
                    arrivalTime = $stopArrival
                    departureTime = $stopDeparture
                    cumulativeFare = $cumulativeFare
                }
            }
            
            $train = @{
                trainName = "$trainName($trainNo)"
                start = $startStation
                destination = $destStation
                startTime = $startTime
                arrivalTime = $arrivalTime
                fare = $fare
                duration = $duration
                offDay = $normalizedOffDay
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
