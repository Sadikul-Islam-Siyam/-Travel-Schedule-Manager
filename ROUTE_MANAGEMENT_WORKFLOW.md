# Route Management Workflow Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Travel Schedule Manager                          │
│                                                                       │
│  ┌──────────────┐         ┌──────────────┐         ┌──────────────┐ │
│  │   Developer  │         │    Master    │         │   REST API   │ │
│  │     User     │         │     User     │         │    Server    │ │
│  └──────┬───────┘         └──────┬───────┘         └──────┬───────┘ │
│         │                        │                        │         │
└─────────┼────────────────────────┼────────────────────────┼─────────┘
          │                        │                        │
          │                        │                        │
          │  1. Submit Route       │                        │
          │     Change Request     │                        │
          │                        │                        │
          ▼                        │                        │
┌─────────────────────┐            │                        │
│  Manage Routes UI   │            │                        │
│  (JavaFX)          │            │                        │
│                     │            │                        │
│  - Add Route Form   │            │                        │
│  - Edit Route Form  │            │                        │
│  - Delete Button    │            │                        │
└──────────┬──────────┘            │                        │
           │                       │                        │
           │ 2. Validate &         │                        │
           │    Submit             │                        │
           │                       │                        │
           ▼                       │                        │
┌─────────────────────┐            │                        │
│ ManageRoutes        │            │                        │
│ Controller          │            │                        │
└──────────┬──────────┘            │                        │
           │                       │                        │
           │ 3. Insert into        │                        │
           │    pending_routes     │                        │
           │                       │                        │
           ▼                       │                        │
┌─────────────────────┐            │                        │
│  Database Manager   │            │                        │
│                     │            │                        │
│  ┌───────────────┐  │            │                        │
│  │pending_routes │  │            │                        │
│  │    Table      │  │            │                        │
│  └───────────────┘  │            │                        │
└─────────────────────┘            │                        │
           │                       │                        │
           │ 4. Notify Master      │                        │
           │                       │                        │
           │                       ▼                        │
           │             ┌─────────────────────┐            │
           │             │  API Approval UI    │            │
           │             │  (JavaFX)          │            │
           │             │                     │            │
           │             │  - View Pending     │            │
           │             │  - Approve Button   │            │
           │             │  - Reject Button    │            │
           │             └──────────┬──────────┘            │
           │                        │                       │
           │                        │ 5. Approve/Reject     │
           │                        │                       │
           │                        ▼                       │
           │             ┌─────────────────────┐            │
           │             │  ApiApproval        │            │
           │             │  Controller         │            │
           │             └──────────┬──────────┘            │
           │                        │                       │
           │                        │ 6. Process Approval   │
           │                        │                       │
           ▼                        ▼                       │
┌─────────────────────────────────────────┐                │
│         Database Manager                │                │
│  (approvePendingRoute method)          │                │
│                                         │                │
│  If APPROVED:                           │                │
│  ┌────────────────────────────────┐    │                │
│  │ 1. Insert/Update routes table  │    │                │
│  └────────────────────────────────┘    │                │
│           │                             │                │
│           ▼                             │                │
│  ┌────────────────────────────────┐    │                │
│  │ 2. Write to JSON files         │◄───┼────────────────┤
│  │    - bus_schedules.json        │    │                │
│  │    - train_schedules.json      │    │                │
│  └────────────────────────────────┘    │                │
│           │                             │                │
│           ▼                             │                │
│  ┌────────────────────────────────┐    │                │
│  │ 3. Archive to route_history    │    │                │
│  └────────────────────────────────┘    │                │
│           │                             │                │
│           ▼                             │                │
│  ┌────────────────────────────────┐    │                │
│  │ 4. Delete from pending_routes  │    │                │
│  └────────────────────────────────┘    │                │
└─────────────────┬───────────────────────┘                │
                  │                                        │
                  │ 7. JSON Files Updated                  │
                  │                                        │
                  ▼                                        │
         ┌─────────────────┐                               │
         │ Storage Layer   │                               │
         │                 │                               │
         │ - BusSchedule   │                               │
         │   Storage       │                               │
         │ - TrainSchedule │                               │
         │   Storage       │                               │
         └────────┬────────┘                               │
                  │                                        │
                  │ 8. REST API Reads                      │
                  │    Updated JSON                        │
                  │                                        │
                  └────────────────────────────────────────▶
                                                           │
                                                           ▼
                                              ┌─────────────────────┐
                                              │   REST Endpoints    │
                                              │                     │
                                              │ GET /api/schedules  │
                                              │     /bus            │
                                              │                     │
                                              │ GET /api/schedules  │
                                              │     /train          │
                                              │                     │
                                              │ GET /api/routes     │
                                              └─────────────────────┘
```

## Detailed Workflow Steps

### Create Route Workflow
```
Developer                  System                     Master
    │                         │                         │
    ├──[1. Open Add Form]────▶│                         │
    │                         │                         │
    ├──[2. Fill Details]─────▶│                         │
    │   - Route Name           │                         │
    │   - Origin/Destination   │                         │
    │   - Type, Price, etc.    │                         │
    │                         │                         │
    ├──[3. Submit]───────────▶│                         │
    │                         │                         │
    │                         ├─[4. Validate]           │
    │                         │                         │
    │                         ├─[5. Save to             │
    │                         │    pending_routes]       │
    │                         │                         │
    │◀──[6. Confirmation]─────┤                         │
    │   "Request Submitted"   │                         │
    │                         │                         │
    │                         ├─[7. Notify]────────────▶│
    │                         │                         │
    │                         │                         ├─[8. Review]
    │                         │                         │
    │                         │◀─[9. Approve]───────────┤
    │                         │                         │
    │                         ├─[10. Update DB]         │
    │                         ├─[11. Write JSON]        │
    │                         ├─[12. Archive History]   │
    │                         │                         │
    │                         │──[13. Success]─────────▶│
    │                         │                         │
    │◀──[14. REST API         │                         │
    │    Now Has New Route]   │                         │
```

## Data Flow

### From Form Submission to REST API
```
┌────────────────┐
│   User Input   │
│   (Form Data)  │
└───────┬────────┘
        │
        ▼
┌────────────────────────────┐
│   Controller Validation    │
│   - Check required fields  │
│   - Validate formats       │
│   - Parse numeric values   │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│   Database Manager         │
│   submitPendingRoute()     │
│   - Insert to              │
│     pending_routes table   │
└───────┬────────────────────┘
        │
        │ [Awaiting Master Approval]
        │
        ▼
┌────────────────────────────┐
│   Master Approves          │
│   approvePendingRoute()    │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│   writeToJsonFile()        │
│   - Create DTO object      │
│   - Calculate times        │
│   - Call storage layer     │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│   Storage Layer            │
│   - Load existing JSON     │
│   - Add/Update/Delete      │
│   - Save to file           │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│   JSON File                │
│   data/bus_schedules.json  │
│   data/train_schedules.json│
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│   REST API Endpoints       │
│   - Serve schedule data    │
│   - Handle queries         │
└────────────────────────────┘
```

## Database Schema Relationships

```
┌──────────────────┐         ┌──────────────────┐
│  pending_routes  │         │     routes       │
├──────────────────┤         ├──────────────────┤
│ id               │         │ id               │
│ route_name       │         │ route_name       │
│ origin           │         │ origin           │
│ destination      │         │ destination      │
│ transport_type   │         │ transport_type   │
│ duration_minutes │         │ duration_minutes │
│ price            │         │ price            │
│ schedule_time    │         │ schedule_time    │
│ metadata         │         │ metadata         │
│ status [PENDING] │         │ status [ACTIVE]  │
│ change_type      │         │ created_date     │
│ original_route_id├────────▶│ modified_date    │
│ submitted_by     │         └──────────────────┘
│ submitted_date   │                 │
│ notes            │                 │ (On Approve)
│ feedback         │                 │
│ reviewed_by      │                 ▼
│ reviewed_date    │         ┌──────────────────┐
└──────────────────┘         │  route_history   │
         │                   ├──────────────────┤
         │ (On Approve       │ id               │
         │  or Reject)       │ route_name       │
         └──────────────────▶│ ...all fields... │
                             │ change_type      │
                             │ status [APPROVED]│
                             │ feedback         │
                             └──────────────────┘
```

## Component Interaction

```
┌─────────────────────────────────────────────────┐
│              JavaFX Application                 │
│                                                 │
│  ┌──────────────┐        ┌──────────────┐      │
│  │   Manage     │        │     API      │      │
│  │   Routes     │        │   Approval   │      │
│  │  Controller  │        │  Controller  │      │
│  └──────┬───────┘        └──────┬───────┘      │
│         │                       │               │
│         └───────────┬───────────┘               │
│                     │                           │
│                     ▼                           │
│          ┌──────────────────┐                   │
│          │  Database        │                   │
│          │  Manager         │                   │
│          └────────┬─────────┘                   │
└───────────────────┼─────────────────────────────┘
                    │
                    │ writes to
                    │
                    ▼
        ┌───────────────────────┐
        │   Storage Layer       │
        ├───────────────────────┤
        │ BusScheduleStorage    │
        │ TrainScheduleStorage  │
        └───────────┬───────────┘
                    │
                    │ persists to
                    │
                    ▼
        ┌───────────────────────┐
        │   JSON Files          │
        ├───────────────────────┤
        │ bus_schedules.json    │
        │ train_schedules.json  │
        └───────────┬───────────┘
                    │
                    │ consumed by
                    │
                    ▼
        ┌───────────────────────┐
        │   REST API Server     │
        ├───────────────────────┤
        │ ScheduleController    │
        │ - GET endpoints       │
        │ - Search endpoints    │
        └───────────────────────┘
```

## Key Takeaways

1. **Separation of Concerns**: 
   - UI handles presentation and validation
   - Controllers manage business logic
   - Database layer handles persistence
   - Storage layer manages JSON files

2. **Approval Workflow**: 
   - Changes go through pending state
   - Master review required
   - Complete audit trail

3. **Data Synchronization**: 
   - Database and JSON files stay in sync
   - Automatic updates on approval
   - No manual intervention needed

4. **Security**: 
   - User authentication required
   - Role-based access control
   - All actions logged
