# Route Management System - User Guide

## Overview
The enhanced **Manage Routes** feature allows developers to efficiently add, edit, and delete routes through the REST API with an approval workflow. All changes require master approval before being written to the JSON files.

## Key Features

### 1. **Add New Routes**
- Click the **"➕ Add Route"** button
- Fill in the required fields:
  - **Route Name** (Bus/Train service name)
  - **Origin** (Starting location)
  - **Destination** (End location)
  - **Transport Type** (BUS or TRAIN)
  - **Duration** (in minutes)
  - **Price** (in BDT)
  - **Schedule Time** (e.g., 08:00, 14:30)
  - **Departure Time** (optional)
  - **Arrival Time** (optional)
  - **Status** (ACTIVE, INACTIVE, MAINTENANCE)
  - **Metadata** (optional additional information)
  - **Notes for Master** (explain why this change is needed)
- Click **"Submit for Approval"**
- Your request will be sent to the master for approval

### 2. **Edit Existing Routes**
- Locate the route in the table
- Click the **"✏ Edit"** button
- Modify the necessary fields
- Add notes explaining the changes
- Click **"Submit Edit for Approval"**
- The master will review and approve/reject your changes

### 3. **Delete Routes**
- Locate the route in the table
- Click the **"🗑 Delete"** button
- Confirm the deletion request
- The master will review and approve/reject the deletion

## Approval Workflow

### Developer Side:
1. Submit a route change (Add/Edit/Delete) through the **Manage Routes** page
2. The request is stored in the `pending_routes` database table
3. Wait for master approval
4. Check your pending requests status in **"My Pending Requests"**

### Master Side:
1. Navigate to **"API Approval"** page
2. Review all pending route changes
3. For each request, you can:
   - **✓ Approve**: The change will be applied to:
     - Database (`routes` table)
     - REST API JSON files (`data/bus_schedules.json` or `data/train_schedules.json`)
     - History table for tracking
   - **✗ Reject**: Provide feedback explaining why the change was rejected

## Technical Details

### Database Tables

#### `pending_routes`
Stores all route change requests awaiting approval.

#### `routes`
Live routes data (after approval).

#### `route_history`
Complete history of all approved/rejected changes.

### JSON Files Integration

When a route change is **approved by master**, the system automatically:

1. **For CREATE operations**:
   - Adds new entry to `data/bus_schedules.json` or `data/train_schedules.json`
   - Creates schedule with calculated departure/arrival times

2. **For UPDATE operations**:
   - Updates existing entry in the JSON file
   - Preserves the schedule structure

3. **For DELETE operations**:
   - Removes entry from the JSON file
   - Archives the deletion in history table

### REST API Endpoints

The JSON files are consumed by these REST API endpoints:
- `GET /api/schedules/bus` - Get all bus schedules
- `GET /api/schedules/train` - Get all train schedules
- `GET /api/routes?start={origin}&destination={destination}` - Search routes

## Field Descriptions

| Field | Description | Required |
|-------|-------------|----------|
| **Route Name** | Bus/Train service name (e.g., "Hanif Express", "Sonar Bangla") | ✓ |
| **Origin** | Starting city/location | ✓ |
| **Destination** | Ending city/location | ✓ |
| **Transport Type** | BUS or TRAIN | ✓ |
| **Duration** | Travel time in minutes | ✓ |
| **Price** | Fare in BDT | ✓ |
| **Schedule Time** | Primary departure time (e.g., 08:00) | Optional |
| **Departure Time** | Specific departure time | Optional |
| **Arrival Time** | Calculated based on duration | Optional |
| **Status** | Route status (ACTIVE/INACTIVE/MAINTENANCE) | Optional |
| **Metadata** | Additional information (amenities, class, etc.) | Optional |
| **Notes** | Explanation for master | Optional |

## Examples

### Example 1: Adding a New Bus Route
```
Route Name: Green Line Express
Origin: Dhaka
Destination: Chittagong
Transport Type: BUS
Duration: 360
Price: 750.00
Schedule Time: 07:00
Status: ACTIVE
Metadata: AC, Wi-Fi, Snacks
Notes: High-demand route during holidays
```

### Example 2: Editing a Train Route
```
Route Name: Sonar Bangla Express
Origin: Dhaka
Destination: Sylhet
Transport Type: TRAIN
Duration: 420
Price: 650.00 (Updated from 600.00)
Notes: Price adjustment due to fuel cost increase
```

### Example 3: Deleting a Route
```
Route Name: Old Express
Reason: Service discontinued due to low demand
```

## Best Practices

1. **Provide Clear Notes**: Always explain why a change is needed
2. **Verify Data**: Double-check all fields before submission
3. **Check Existing Routes**: Avoid duplicate route names
4. **Use Standard Formats**: 
   - Time format: HH:MM (e.g., 08:00, 14:30)
   - Price format: Use decimal numbers (e.g., 450.00)
5. **Monitor Requests**: Check "My Pending Requests" regularly for feedback

## Security & Permissions

- **Developers**: Can submit route changes for approval
- **Master**: Can approve/reject all pending changes
- All changes are tracked in the history table
- Rejected requests include feedback explaining the reason

## Troubleshooting

### "Failed to submit request"
- Check database connection
- Ensure all required fields are filled
- Verify user authentication

### "Route already exists"
- Check for duplicate route names
- Consider editing the existing route instead

### Changes not appearing in REST API
- Ensure the request was approved by master
- Refresh the routes list
- Check JSON files in the `data/` directory

## Summary

The enhanced Route Management system provides:
- ✓ Efficient UI for managing routes
- ✓ Master approval workflow for quality control
- ✓ Automatic synchronization with REST API JSON files
- ✓ Complete audit trail of all changes
- ✓ Direct integration with the REST API backend

For technical support or questions, contact your system administrator.
