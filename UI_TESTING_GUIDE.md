# UI Conversion Testing Guide

## Prerequisites
- JavaFX 21.0.1 configured
- Maven dependencies installed
- SQLite database initialized with test data

## Test Execution Plan

### Phase 1: Visual Verification

#### 1.1 ADMIN/MASTER Role Testing
**Login as**: Master user
**Test screens**:
- [ ] Account Approval
  - Verify single-row card layout
  - Check spacing (12px padding, 8px between cards)
  - Confirm buttons are right-aligned
  - Test Approve/Reject functionality
  - Validate role and status badges display correctly
  
- [ ] API Change Approval
  - Verify compact card design
  - Check change type badges (ADD=green, UPDATE=orange, DELETE=red)
  - Test filter dropdown (All/Pending/Approved/Rejected)
  - Confirm Approve/Reject with feedback dialog works
  - Validate route details display inline

**Expected Results**:
- Cards should be ~45px height
- 3-4x more items visible than before
- All text should be readable (no truncation)
- Buttons should respond to clicks

---

#### 1.2 DEVELOPER Role Testing
**Login as**: Developer user
**Test screens**:
- [ ] Manage Routes
  - Confirm TableView with sortable columns
  - Test Edit button (should open edit dialog)
  - Test Delete button (should show confirmation)
  - Verify Actions column displays correctly
  - Sort by each column to verify functionality
  
- [ ] Request History
  - Verify minimal card layout
  - Check change type and status badges
  - Confirm feedback section displays when present
  - Test filter (All/Pending/Approved/Rejected)
  - Validate timeline information is readable

**Expected Results**:
- TableView should show ~35 rows on 1080p screen
- History cards should be ~45-70px (with/without feedback)
- All metadata should be inline and readable
- Sorting should work instantly

---

#### 1.3 USER Role Testing
**Login as**: Regular user
**Test screens**:
- [ ] Create Plan - Schedule Search
  - Verify ListView displays search results
  - Check compact single-row layout per schedule
  - Test type badge (BUS/TRAIN) colors
  - Confirm fare display on right side
  - Test selection (click should add to plan)
  - Verify hover effect (blue background)
  
- [ ] Plan Container (right side)
  - Confirm plan cards display correctly
  - Test Remove button (✕) functionality
  - Verify tight connection warnings appear
  - Check totals update correctly

**Expected Results**:
- ListView should show ~25 schedules on 1080p screen
- Each schedule row should be ~35px
- Hover should change background to #e3f2fd
- Click should add to plan and clear search results

---

### Phase 2: Interaction Testing

#### 2.1 Workflow: Approve New Account (MASTER)
1. Navigate to Account Approval
2. Find a PENDING account
3. Click **Approve** button
4. Verify account disappears from PENDING filter
5. Switch filter to "Approved"
6. Confirm account appears in Approved list

#### 2.2 Workflow: Add New Route (DEVELOPER)
1. Navigate to Manage Routes
2. Click "Add New Route" button
3. Fill in route details
4. Save
5. Verify new route appears in TableView
6. Test Edit on new route
7. Test Delete with confirmation

#### 2.3 Workflow: Create Travel Plan (USER)
1. Navigate to Create Plan
2. Enter: Start="Dhaka", Destination="Sylhet", Date=Tomorrow
3. Click "Search Schedules"
4. Verify ListView populates with results
5. Click a schedule to add to plan
6. Verify schedule appears in right panel
7. Search for next leg
8. Add 2-3 more schedules
9. Click "Summarize Plan"
10. Verify plan summary displays correctly

#### 2.4 Workflow: Review Submission History (DEVELOPER)
1. Submit a route change
2. Navigate to Request History
3. Verify submission appears with PENDING status
4. Login as MASTER
5. Approve the change with feedback
6. Login back as DEVELOPER
7. Check Request History
8. Verify status changed to APPROVED
9. Confirm feedback displays below main row

---

### Phase 3: Responsive Testing

#### 3.1 Window Resize Testing
- [ ] Shrink window width to 800px
  - Verify horizontal scroll appears if needed
  - Check that cards don't break layout
  - Confirm buttons remain visible
  
- [ ] Expand window to 1920px
  - Verify flexible spacers expand properly
  - Check that text doesn't stretch awkwardly
  - Confirm all elements remain aligned

#### 3.2 Content Overflow Testing
- [ ] Test with very long route names
- [ ] Test with very long email addresses
- [ ] Test with multi-line feedback
- [ ] Test with 100+ items in list

**Expected Results**:
- Text wrapping should work for long content
- Ellipsis (...) should appear for truncated text
- ScrollBars should appear automatically
- Layout should not break under stress

---

### Phase 4: Performance Testing

#### 4.1 Large Dataset Testing
- [ ] Load 500+ routes in Manage Routes TableView
  - Measure initial load time (should be <2s)
  - Test scrolling smoothness
  - Verify search/filter responsiveness

- [ ] Load 100+ pending accounts
  - Measure render time
  - Test approval of multiple accounts
  - Check memory usage

- [ ] Search with 50+ schedule results
  - Verify ListView virtualization works
  - Test scroll performance
  - Confirm selection remains responsive

#### 4.2 Memory Profile
- Open all screens in sequence
- Monitor Java heap usage
- Expected: <500MB for typical usage

---

### Phase 5: Cross-Platform Testing

#### 5.1 Windows 10/11
- [ ] Test with Windows default theme
- [ ] Test with high DPI (150%, 200%)
- [ ] Verify font rendering

#### 5.2 macOS (if applicable)
- [ ] Test with macOS UI style
- [ ] Verify CMD shortcuts work
- [ ] Check retina display rendering

#### 5.3 Linux (if applicable)
- [ ] Test with GTK theme
- [ ] Verify keyboard navigation
- [ ] Check X11/Wayland compatibility

---

## Known Issues to Watch For

### Potential Issues
1. **ListView selection**: May need double-click on some platforms
2. **TableView columns**: Might need manual width adjustment
3. **Card shadows**: May not render on all graphics drivers
4. **Hover effects**: Touch screens won't show hover state

### Workarounds
- If ListView doesn't respond to single-click, check `setOnMouseClicked` handler
- If columns overlap, adjust prefWidth values in FXML
- If shadows missing, check JavaFX version and GPU drivers
- Touch screens can use tap instead of hover

---

## Regression Testing Checklist

### Business Logic (Should NOT Change)
- [ ] Database operations work correctly
- [ ] Validation rules still apply
- [ ] Security/authentication unchanged
- [ ] API calls function normally
- [ ] File I/O operations work

### Navigation
- [ ] Back buttons navigate correctly
- [ ] Home navigation works
- [ ] Role-based menu items display correctly
- [ ] Deep linking (if any) still works

### Data Integrity
- [ ] Form submissions save correctly
- [ ] Search results are accurate
- [ ] Filters apply properly
- [ ] Sort maintains data accuracy

---

## Test Data Requirements

### Minimum Test Data
- **Users**: 3 PENDING, 5 APPROVED, 2 REJECTED
- **Routes**: 20 Bus, 20 Train, mix of statuses
- **API Changes**: 10 PENDING, 20 historical (approved/rejected)
- **Schedules**: 50+ for Dhaka-Sylhet route

### Test Accounts
```
Master:
  Username: admin
  Password: admin123

Developer:
  Username: dev1
  Password: dev123

User:
  Username: user1
  Password: user123
```

---

## Bug Reporting Template

```markdown
### Bug Report

**Screen**: [e.g., Account Approval]
**Role**: [Master/Developer/User]
**Priority**: [High/Medium/Low]

**Steps to Reproduce**:
1. 
2. 
3. 

**Expected Behavior**:

**Actual Behavior**:

**Screenshots**: (if applicable)

**Environment**:
- OS: 
- Java Version: 
- JavaFX Version: 
- Screen Resolution: 
```

---

## Success Criteria

### UI Conversion Goals
- ✅ 60%+ reduction in vertical space per item
- ✅ 2-4x more content visible without scrolling
- ✅ Zero business logic changes
- ✅ All existing features work
- ✅ No performance degradation
- ✅ Maintains readability (11px+ font size)

### User Experience Goals
- ✅ Faster task completion (approval workflows)
- ✅ Reduced scrolling required
- ✅ Improved information density
- ✅ Consistent design patterns across roles
- ✅ Responsive and adaptive layout

---

## Post-Testing Actions

### If Tests Pass
1. Create backup of target/classes
2. Update documentation with screenshots
3. Notify users of UI improvements
4. Monitor for user feedback

### If Tests Fail
1. Document specific failures
2. Prioritize fixes (P0, P1, P2)
3. Create bugfix branch
4. Re-test after fixes
5. Repeat until success criteria met

---

## Testing Timeline

- **Day 1**: Phase 1 (Visual Verification) - 2 hours
- **Day 2**: Phase 2 (Interaction Testing) - 3 hours
- **Day 3**: Phase 3-4 (Responsive & Performance) - 2 hours
- **Day 4**: Phase 5 (Cross-Platform) - 2 hours (if applicable)
- **Day 5**: Regression Testing & Documentation - 2 hours

**Total Estimated Time**: 11-13 hours

---

## Quick Smoke Test (15 minutes)

For rapid validation, run this quick test:

1. **Login as Master** → Account Approval → Approve 1 account (2 min)
2. **Login as Developer** → Manage Routes → Edit 1 route (2 min)
3. **Login as Developer** → Request History → View history (1 min)
4. **Login as User** → Create Plan → Search + Add schedules (5 min)
5. **Login as Master** → API Approval → Approve 1 change with feedback (3 min)
6. **Login as Developer** → Request History → Verify feedback displays (2 min)

If all 6 steps pass without errors, proceed to comprehensive testing.

---

## Contact & Support

For testing questions or issues:
- Developer: [Your Name]
- Documentation: `UI_CONVERSION_SUMMARY.md`
- Before/After: `UI_CONVERSION_BEFORE_AFTER.md`
