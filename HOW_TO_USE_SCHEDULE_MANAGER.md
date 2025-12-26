# How to Use the Schedule Management UI

## 🎯 Accessing the Feature

1. **Run your application**
2. **Login** to your account (must be Developer/Master role)
3. On the **Home** page, click the **"Manage Schedules"** button (green button)

---

## 📋 What You Can Do

### ✅ View All Schedules
- See all bus and train schedules in a table
- **Filter by type**: ALL, BUS, or TRAIN
- **Search**: Search by schedule ID or route name
- **Refresh**: Click 🔄 to reload data

### ➕ Add New Schedules

1. On the right side, fill in the form:
   - **Schedule Type**: Choose BUS or TRAIN
   - **Schedule ID**: Unique ID (e.g., BUS005, TRAIN010)
   - **Origin & Destination**: City names
   - **Departure Date & Time**: When it leaves
   - **Arrival Date & Time**: When it arrives
   - **Fare**: Price in BDT
   - **Available Seats**: Number of seats

2. **For Bus Schedules**:
   - Company Name (e.g., "Green Line")
   - Bus Type (AC, Non-AC, Sleeper, etc.)

3. **For Train Schedules**:
   - Train Name (e.g., "Subarna Express")
   - Train Class (AC, First Class, Snigdha, etc.)

4. Click **"Save Schedule"**

### ✏️ Edit Existing Schedules

1. **Select a schedule** from the table (click on it)
2. Click **"Edit Selected"** button
3. The form will populate with the schedule data
4. **Modify the fields** you want to change
5. Click **"Update Schedule"**

### 🗑️ Delete Schedules

1. **Select a schedule** from the table
2. Click **"Delete Selected"** button
3. **Confirm** the deletion
4. The schedule is permanently removed

---

## 💾 Data Storage

- All changes are saved **immediately** to `schedules-data.json`
- Users will see the updated schedules **instantly** when searching
- The JSON file is in your project root directory

---

## 🔍 Finding Schedules

**Filter Dropdown:**
- Select "BUS" to see only bus schedules
- Select "TRAIN" to see only train schedules
- Select "ALL" to see everything

**Search Box:**
- Type schedule ID (e.g., "BUS001")
- Type route (e.g., "Dhaka")
- Search is case-insensitive

---

## ⚠️ Important Notes

1. **Schedule IDs must be unique** - No duplicates allowed
2. **Can't change Schedule ID** when editing (create new one instead)
3. **Can't change Type** when editing (BUS → TRAIN not allowed)
4. **Time format**: Use 24-hour format (00-23 for hours, 00-59 for minutes)
5. **Arrival must be after departure** (validation coming soon)

---

## 🎨 UI Features

✓ **Real-time count** - See total schedules in top-right corner  
✓ **Color-coded types** - BUS and TRAIN have different colors  
✓ **Form validation** - Required fields are checked  
✓ **Clear form** - Reset button to start fresh  
✓ **Confirmation dialogs** - No accidental deletions  

---

## 🐛 Troubleshooting

**Schedule not appearing?**
- Click the 🔄 refresh button
- Check the filter dropdown (make sure it's not filtering it out)
- Clear the search box

**Can't edit?**
- Make sure you selected a row in the table first
- Only one schedule can be edited at a time

**Save button not working?**
- Check for required fields (marked with *)
- Ensure time fields are valid numbers
- Check error messages in the popup

---

## 🚀 What Users See

When you add/edit schedules here:
1. Users search for routes (e.g., Dhaka → Chittagong)
2. They see YOUR manually added schedules
3. No more random mock data!
4. Real schedules you control

---

## 📂 Files Modified

When you add/edit/delete:
- **schedules-data.json** - Updated automatically
- **No code changes needed** - Just use the UI!

---

**Enjoy managing your schedules! 🎉**
