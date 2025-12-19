# Role-Based Application Flow Diagram
## Desktop Application with Controlled API Change Approval

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          APPLICATION START                                   │
│                                  ↓                                           │
│                          ┌─────────────┐                                     │
│                          │   LOGIN     │                                     │
│                          │  INTERFACE  │                                     │
│                          └──────┬──────┘                                     │
│                                 │                                            │
│              ┌──────────────────┼──────────────────┐                        │
│              │                  │                  │                        │
│              ↓                  ↓                  ↓                        │
│      ┌───────────────┐  ┌───────────────┐  ┌───────────────┐              │
│      │  MASTER ROLE  │  │DEVELOPER ROLE │  │   USER ROLE   │              │
│      │   (Admin)     │  │  (API Editor) │  │  (Consumer)   │              │
│      └───────┬───────┘  └───────┬───────┘  └───────┬───────┘              │
│              │                  │                  │                        │
└──────────────┼──────────────────┼──────────────────┼────────────────────────┘
               │                  │                  │
               │                  │                  │
═══════════════╪══════════════════╪══════════════════╪═══════════════════════
    MASTER     │      DEVELOPER   │        USER      │
  WORKFLOWS    │      WORKFLOWS   │      WORKFLOWS   │
═══════════════╪══════════════════╪══════════════════╪═══════════════════════
               │                  │                  │
        ┌──────┴──────┐          │                  │
        │             │          │                  │
        ↓             ↓          ↓                  ↓
┌──────────────┐ ┌──────────┐ ┌─────────────┐ ┌──────────────┐
│   ACCOUNT    │ │   API    │ │ API EDITOR  │ │ APPLICATION  │
│   CONTROL    │ │ APPROVAL │ │  (Routes)   │ │   SERVICES   │
│              │ │ CONTROL  │ │             │ │              │
└──────┬───────┘ └─────┬────┘ └──────┬──────┘ └──────┬───────┘
       │               │             │               │
       │               │             │               │
       ↓               │             ↓               ↓
┌─────────────┐        │      ┌──────────────┐ ┌────────────┐
│ View Pending│        │      │Create/Modify │ │ Create Plan│
│  Accounts   │        │      │  Route Data  │ │ Edit Plan  │
└──────┬──────┘        │      └──────┬───────┘ │ Generate   │
       │               │             │         │   Route    │
       ↓               │             │         └─────┬──────┘
┌─────────────┐        │             ↓               │
│  APPROVE ✓  │        │      ┌──────────────┐       │
│  REJECT  ✗  │        │      │   SUBMIT     │       │
└──────┬──────┘        │      │   CHANGE     │       │
       │               │      │   REQUEST    │       │
       ↓               │      └──────┬───────┘       │
┌─────────────┐        │             │               │
│  User moved │        │             │               │
│  to USERS   │        │             │               │
│   table     │        │             ↓               │
│             │        │      ┌──────────────┐       │
│ ✓ Can Login │        │      │  PENDING     │       │
│ ✗ Blocked   │        │      │   CHANGE     │       │
└─────────────┘        │      │   QUEUE      │       │
                       │      └──────┬───────┘       │
                       │             │               │
                       │             │               │
═══════════════════════╪═════════════╪═══════════════╪══════════════════════
    MASTER APPROVAL    │             │               │       LIVE API
        GATEWAY        │             │               │      CONSUMPTION
═══════════════════════╪═════════════╪═══════════════╪══════════════════════
                       │             │               │
                       │             │               │
                       │      ┌──────┴───────┐       │
                       │      │              │       │
                       │      ↓              ↓       │
                       │  ┌────────────┐ ┌────────────┐
                       │  │   REVIEW   │ │ View Change│
                       └─→│   PENDING  │ │   Details  │
                          │   CHANGES  │ │            │
                          └─────┬──────┘ └────────────┘
                                │
                          ┌─────┴─────┐
                          │           │
                          ↓           ↓
                    ┌──────────┐ ┌──────────┐
                    │ APPROVE  │ │  REJECT  │
                    │    ✓     │ │    ✗     │
                    └────┬─────┘ └────┬─────┘
                         │            │
                         ↓            ↓
                  ┌─────────────┐ ┌──────────────┐
                  │   UPDATE    │ │   DISCARD    │
                  │  LIVE API   │ │   CHANGE     │
                  │             │ │              │
                  │ ✓ Published │ │ ✗ Rejected   │
                  └──────┬──────┘ └──────────────┘
                         │
                         ↓
                  ┌─────────────┐
                  │  LIVE API   │
                  │   (Routes   │
                  │    Table)   │
                  └──────┬──────┘
                         │
                         │
═════════════════════════╪═════════════════════════════════════════════════
       ALL USERS         │           API CONSUMPTION
      CONSUME THIS       │
═════════════════════════╪═════════════════════════════════════════════════
                         │
            ┌────────────┴────────────┐
            │                         │
            ↓                         ↓
    ┌──────────────┐         ┌──────────────┐
    │   DEVELOPER  │         │     USER     │
    │   Uses Live  │         │  Uses Live   │
    │  API for own │         │  API for all │
    │   planning   │         │   planning   │
    └──────────────┘         └──────────────┘
```

## 📋 Flow Summary

### 1. LOGIN INTERFACE → ROLE ASSIGNMENT
```
┌────────────┐
│   LOGIN    │ → Credentials Verified
└─────┬──────┘
      │
      ├─→ Username: siyam2207031 → MASTER ROLE
      ├─→ Approved Developer Account → DEVELOPER ROLE  
      └─→ Approved User Account → USER ROLE
```

### 2. MASTER ROLE CONTROL HIERARCHY
```
┌──────────────────────────────────────┐
│         MASTER DASHBOARD             │
│  ┌─────────────┐  ┌─────────────┐   │
│  │   ACCOUNT   │  │    API      │   │
│  │   CONTROL   │  │  APPROVAL   │   │
│  └──────┬──────┘  └──────┬──────┘   │
└─────────┼─────────────────┼──────────┘
          │                 │
          ↓                 ↓
    Accept/Reject      Approve/Reject
    New Accounts       API Changes
          │                 │
          ↓                 ↓
    Users Table       Routes Table
   (Can Login)        (Live API)
```

### 3. DEVELOPER ROLE WORKFLOW
```
┌──────────────────────────────────────┐
│       DEVELOPER DASHBOARD            │
│  ┌─────────────────────────────┐    │
│  │      API EDITOR              │    │
│  │  • Create Route              │    │
│  │  • Modify Route              │    │
│  │  • Delete Route              │    │
│  └─────────────┬────────────────┘    │
└────────────────┼─────────────────────┘
                 │
                 ↓
        ┌────────────────┐
        │   SUBMIT TO    │
        │ PENDING QUEUE  │
        └────────┬───────┘
                 │
                 ↓
        [Awaiting Master Approval]
                 │
     ┌───────────┴───────────┐
     ↓                       ↓
✓ APPROVED              ✗ REJECTED
     │                       │
     ↓                       ↓
Live API Updated      Change Discarded
```

### 4. USER ROLE WORKFLOW
```
┌──────────────────────────────────────┐
│         USER DASHBOARD               │
│  ┌─────────────────────────────┐    │
│  │  • Create Travel Plan        │    │
│  │  • Edit Travel Plan          │    │
│  │  • Generate Automatic Route  │    │
│  └─────────────┬────────────────┘    │
└────────────────┼─────────────────────┘
                 │
                 ↓
        ┌────────────────┐
        │  Reads from    │
        │   LIVE API     │
        │  (Approved     │
        │   Routes)      │
        └────────────────┘
```

## 🔒 Control Hierarchy Diagram

```
                    ┌─────────────────┐
                    │     MASTER      │
                    │   (Ultimate     │
                    │    Control)     │
                    └────────┬────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
         ┌──────────▼──────┐    ┌────▼──────────┐
         │  Account         │    │  API Change   │
         │  Approval        │    │  Approval     │
         └──────────┬───────┘    └────┬──────────┘
                    │                 │
         ┌──────────┴─────────┐       │
         │                    │       │
    ┌────▼────┐        ┌──────▼───────▼───────┐
    │  USER   │        │     DEVELOPER        │
    │  ROLE   │        │       ROLE           │
    └────┬────┘        └──────────┬───────────┘
         │                        │
         │                        │
         └────────────┬───────────┘
                      │
              ┌───────▼────────┐
              │   LIVE API     │
              │  (Read-Only    │
              │   for Users)   │
              └────────────────┘
```

## 🎯 Key Control Points

### ✓ MASTER CONTROLS:
1. **Account Creation Gate**
   - New registrations → Pending state
   - Master approves → Active user
   - Master rejects → Blocked

2. **API Modification Gate**
   - Developer edits → Pending state
   - Master approves → Live API updated
   - Master rejects → Changes discarded

### ⚠️ DEVELOPER RESTRICTIONS:
- ❌ Cannot directly modify Live API
- ❌ Cannot approve own changes
- ✓ Can create/edit routes (pending state)
- ✓ Can submit change requests
- ✓ Can view pending changes

### 🔐 USER RESTRICTIONS:
- ❌ Cannot access API editor
- ❌ Cannot modify routes
- ❌ Cannot approve accounts
- ✓ Can use application services
- ✓ Can read Live API (approved data only)

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        DATABASE LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────┐        │
│  │    USERS    │   │   PENDING    │   │    ROUTES    │        │
│  │    TABLE    │   │    USERS     │   │   (LIVE API) │        │
│  │             │   │    TABLE     │   │              │        │
│  │ Approved    │   │  Awaiting    │   │  Master-     │        │
│  │ Accounts    │   │  Approval    │   │  Approved    │        │
│  └──────┬──────┘   └──────┬───────┘   └──────┬───────┘        │
│         │                 │                  │                 │
└─────────┼─────────────────┼──────────────────┼─────────────────┘
          │                 │                  │
          ↓                 ↓                  ↓
    ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
    │   Master    │   │   Master    │   │ Master      │
    │  Approves   │   │  Approves   │   │ Publishes   │
    │  Accounts   │   │  Changes    │   │  to Live    │
    └─────────────┘   └─────────────┘   └─────────────┘
          │                 │                  │
          │                 │                  │
          └─────────────────┼──────────────────┘
                            │
                     ┌──────┴───────┐
                     │              │
            ┌────────▼─────┐  ┌─────▼────────┐
            │  Developer   │  │     User     │
            │  Consumes    │  │   Consumes   │
            └──────────────┘  └──────────────┘
```

## 🚦 Approval Flow with Status States

```
DEVELOPER SUBMITS CHANGE
         ↓
    ┌──────────────┐
    │   PENDING    │  ← Status: "AWAITING_APPROVAL"
    │    STATE     │
    └──────┬───────┘
           │
           ↓
    Master Reviews
           │
     ┌─────┴─────┐
     ↓           ↓
┌─────────┐  ┌─────────┐
│APPROVED │  │REJECTED │
└────┬────┘  └────┬────┘
     │            │
     ↓            ↓
Update Live   Discard
   API        Changes
     │            │
     ↓            ↓
Status:      Status:
"ACTIVE"    "REJECTED"
```

## 🎨 User Interface Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│                    LOGIN SCREEN                          │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ↓               ↓               ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   MASTER     │ │  DEVELOPER   │ │     USER     │
│  DASHBOARD   │ │  DASHBOARD   │ │  DASHBOARD   │
├──────────────┤ ├──────────────┤ ├──────────────┤
│              │ │              │ │              │
│ • Edit API   │ │ • API Editor │ │ • Create Plan│
│              │ │              │ │ • Edit Plan  │
│ • Pending    │ │ • Submit     │ │ • Generate   │
│   Accounts   │ │   Changes    │ │   Route      │
│              │ │              │ │              │
│              │ │ • View       │ │ • View Plans │
│              │ │   Pending    │ │              │
└──────────────┘ └──────────────┘ └──────────────┘
```

## 📌 Summary: Master-Controlled Application

**All real-time application changes are controlled and approved by the Master account:**

1. ✅ **New users** → Master approval required
2. ✅ **API changes** → Master approval required
3. ✅ **Live data** → Only Master-approved content
4. ✅ **Access control** → Role-based enforcement
5. ✅ **Change tracking** → All modifications logged

**No developer or user can bypass Master approval for critical changes.**

---

*This ensures complete administrative control over the application's data and user access.*
