# CRUD Testing Guide - Desktop App

**Date**: November 18, 2025  
**Branch**: `tahap-05-desktop-advanced`  
**Backend**: Running on http://localhost:8081  
**Desktop App**: JavaFX Application

---

## 📋 Prerequisites

### 1. Backend Server Running
✅ Backend is running on port 8081  
✅ H2 Database initialized (in-memory)  
✅ Data seeding completed:
- Roles: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA
- Admin user: username=`admin`, password=`admin123`

### 2. Desktop App Running
✅ JavaFX application started  
✅ Connected to backend API  

---

## 🧪 Testing Checklist

### Phase 1: Authentication & Dashboard
- [ ] **Login**
  - Username: `admin`
  - Password: `admin123`
  - Expected: Successful login → Dashboard appears
  - Verify: User label shows "👤 admin (ADMIN)"
  - Verify: Connection status shows "🟢 Connected"

- [ ] **MenuBar Visibility**
  - Verify all menus are visible:
    - ✅ File (Export PDF, Export CSV, Logout)
    - ✅ Manage (Presensi, Siswa, Guru, User Management)
    - ✅ View (Analytics, Dashboard)
    - ✅ Settings (Preferences)
    - ✅ Help (About)

---

### Phase 2: Siswa Management CRUD

#### 📂 Open Siswa Management
1. Click **Manage → Siswa Management**
2. Window should open (1200x700)
3. Verify UI elements:
   - ✅ Search field and filters (Kelas, Jurusan)
   - ✅ Buttons: Add, Refresh, Edit (disabled), Delete (disabled), Close
   - ✅ Mock Data checkbox (should be **unchecked**)
   - ✅ TableView with 8 columns

#### ✅ Test READ (Get All Siswa)
**Steps:**
1. Uncheck "Mock Data" checkbox (if checked)
2. Click "Refresh" button

**Expected Result:**
- Loading indicator appears briefly
- Toast notification: "Data loaded successfully"
- Info label shows: "Total: X siswa"
- If no data: Table is empty
- If has data: Table shows all siswa from database

**API Call:** `GET /api/siswa`

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### ➕ Test CREATE (Add New Siswa)
**Steps:**
1. Click "➕ Tambah Siswa" button
2. Dialog appears: "Tambah Siswa Baru"
3. Fill form:
   - **NIS**: `12399` (unique number)
   - **Nama**: `Testing User`
   - **Kelas**: `XII RPL 1`
   - **Jurusan**: Select `RPL` from dropdown
   - **RFID Card ID**: `RFID12399` (optional)
   - **Barcode ID**: `BC12399` (optional)
4. Click **OK**

**Expected Result:**
- Loading indicator appears
- Toast notification: "Siswa berhasil ditambahkan" (green)
- Table refreshes automatically
- New siswa appears in table
- Info label count increases by 1

**API Call:** `POST /api/siswa`  
**Request Body:**
```json
{
  "nis": "12399",
  "nama": "Testing User",
  "kelas": "XII RPL 1",
  "jurusan": "RPL",
  "rfidCardId": "RFID12399",
  "barcodeId": "BC12399"
}
```

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### 🔍 Test SEARCH & FILTER
**Test 1: Search by NIS**
1. Type `12399` in search field
2. Click "Cari" or press Enter

**Expected:** Only siswa with NIS containing "12399" shown

**Status:** [ ] PASS / [ ] FAIL

**Test 2: Filter by Kelas**
1. Select "XII RPL 1" from Kelas dropdown
2. Click "Cari"

**Expected:** Only siswa from XII RPL 1 shown

**Status:** [ ] PASS / [ ] FAIL

**Test 3: Filter by Jurusan**
1. Select "RPL" from Jurusan dropdown
2. Click "Cari"

**Expected:** Only siswa with jurusan RPL shown

**Status:** [ ] PASS / [ ] FAIL

**Test 4: Combined Filter**
1. Type "Testing" in search field
2. Select "XII RPL 1" from Kelas
3. Select "RPL" from Jurusan
4. Click "Cari"

**Expected:** Siswa matching all criteria shown

**Status:** [ ] PASS / [ ] FAIL

**Test 5: Reset Filters**
1. Click "Reset" button

**Expected:** 
- All filters cleared
- Full list restored
- Info label shows total count

**Status:** [ ] PASS / [ ] FAIL

---

#### 👁️ Test VIEW (View Details)
**Steps:**
1. Find "Testing User" in table
2. Click 👁️ button in Actions column

**Expected Result:**
- Alert dialog appears: "Detail Siswa"
- Header shows: "Testing User"
- Content shows:
  ```
  ID: [number]
  NIS: 12399
  Nama: Testing User
  Kelas: XII RPL 1
  Jurusan: RPL
  RFID: RFID12399
  Barcode: BC12399
  ```

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### ✏️ Test UPDATE (Edit Siswa)
**Steps:**
1. Find "Testing User" in table
2. Click ✏️ button in Actions column (or select row → click Edit button)
3. Dialog appears: "Edit Siswa"
4. Modify data:
   - **Nama**: `Testing User Updated`
   - **Kelas**: `XII RPL 2`
   - Keep other fields same
5. Click **OK**

**Expected Result:**
- Loading indicator appears
- Toast notification: "Siswa berhasil diupdate" (green)
- Table refreshes
- Updated data appears in table
- Name shows "Testing User Updated"
- Kelas shows "XII RPL 2"

**API Call:** `PUT /api/siswa/{id}`  
**Request Body:**
```json
{
  "nis": "12399",
  "nama": "Testing User Updated",
  "kelas": "XII RPL 2",
  "jurusan": "RPL",
  "rfidCardId": "RFID12399",
  "barcodeId": "BC12399"
}
```

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### 🗑️ Test DELETE (Delete Siswa)
**Steps:**
1. Find "Testing User Updated" in table
2. Click 🗑️ button in Actions column (or select row → click Delete button)
3. Confirmation dialog appears: "Hapus siswa: Testing User Updated?"
4. Click **OK**

**Expected Result:**
- Loading indicator appears
- Toast notification: "Siswa berhasil dihapus" (green)
- Table refreshes
- Deleted siswa no longer in table
- Info label count decreases by 1

**API Call:** `DELETE /api/siswa/{id}`

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

### Phase 3: Guru Management CRUD

#### 📂 Open Guru Management
1. Click **Manage → Guru Management**
2. Window should open (1100x700)
3. Verify UI elements:
   - ✅ Search field and Mapel filter
   - ✅ Buttons: Add, Refresh, Edit (disabled), Delete (disabled), Close
   - ✅ Mock Data checkbox (should be **unchecked**)
   - ✅ TableView with 7 columns

#### ✅ Test READ (Get All Guru)
**Steps:**
1. Uncheck "Mock Data" checkbox (if checked)
2. Click "Refresh" button

**Expected Result:**
- Loading indicator appears briefly
- Toast notification: "Data loaded"
- Info label shows: "Total: X guru"
- Table shows all guru from database

**API Call:** `GET /api/guru`

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### ➕ Test CREATE (Add New Guru)
**Steps:**
1. Click "➕ Tambah Guru" button
2. Dialog appears: "Tambah Guru Baru"
3. Fill form:
   - **NIP**: `199001012020121001` (unique 18 digits)
   - **Nama**: `Testing Guru`
   - **Mata Pelajaran**: Select or type `Pemrograman Java`
   - **RFID Card ID**: `RFID-G999` (optional)
   - **Barcode ID**: `BC-G999` (optional)
4. Click **OK**

**Expected Result:**
- Loading indicator appears
- Toast notification: "Guru berhasil ditambahkan" (green)
- Table refreshes automatically
- New guru appears in table

**API Call:** `POST /api/guru`  
**Request Body:**
```json
{
  "nip": "199001012020121001",
  "nama": "Testing Guru",
  "mapel": "Pemrograman Java",
  "rfidCardId": "RFID-G999",
  "barcodeId": "BC-G999"
}
```

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### 🔍 Test SEARCH & FILTER (Guru)
**Test 1: Search by NIP**
1. Type `19900101` in search field
2. Click "Cari"

**Expected:** Only guru with NIP containing "19900101" shown

**Status:** [ ] PASS / [ ] FAIL

**Test 2: Filter by Mapel**
1. Select "Pemrograman Java" from Mapel dropdown (or similar)
2. Click "Cari"

**Expected:** Only guru teaching that subject shown

**Status:** [ ] PASS / [ ] FAIL

**Test 3: Reset**
1. Click "Reset" button

**Expected:** All filters cleared, full list restored

**Status:** [ ] PASS / [ ] FAIL

---

#### 👁️ Test VIEW (Guru Details)
**Steps:**
1. Find "Testing Guru" in table
2. Click 👁️ button

**Expected:**
- Detail dialog shows all guru information
- NIP, Nama, Mapel, RFID, Barcode displayed correctly

**Status:** [ ] PASS / [ ] FAIL

---

#### ✏️ Test UPDATE (Edit Guru)
**Steps:**
1. Find "Testing Guru" in table
2. Click ✏️ button

**Expected Result:**
- Currently shows: "Edit Guru - Coming Soon!" toast
- **Note**: Full edit implementation pending

**Status:** [ ] PLACEHOLDER  
**Notes:** _Edit functionality not yet fully implemented_

---

#### 🗑️ Test DELETE (Delete Guru)
**Steps:**
1. Find "Testing Guru" in table
2. Click 🗑️ button
3. Confirm deletion

**Expected Result:**
- Confirmation dialog
- After OK: Guru deleted from table
- Toast notification: "Guru berhasil dihapus"

**API Call:** `DELETE /api/guru/{id}`

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

### Phase 4: Presensi Management

#### 📂 Open Presensi Management
1. Click **Manage → Presensi Management**
2. Window should open (1400x700)
3. Verify UI elements:
   - ✅ Date pickers (start/end)
   - ✅ Filters: Tipe (ALL/SISWA/GURU), Status (ALL/HADIR/TERLAMBAT/ALPHA)
   - ✅ Search field
   - ✅ Buttons: Add (placeholder), Refresh, Edit (placeholder), Delete (placeholder)
   - ✅ TableView with 10 columns

#### ✅ Test READ (Get Presensi)
**Steps:**
1. Uncheck "Mock Data" checkbox
2. Click "Refresh" button

**Expected Result:**
- Loads presensi data for today
- Table populated with attendance records
- Shows: ID, Username, Tipe, Tanggal, Jam Masuk, Jam Pulang, Status, Method, Keterangan

**API Call:** `GET /api/laporan/harian?tanggal=[today]`

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

#### 🔍 Test FILTER (Presensi)
**Test 1: Filter by Tipe**
1. Select "SISWA" from Tipe dropdown
2. Click "Cari"

**Expected:** Only student attendance shown

**Status:** [ ] PASS / [ ] FAIL

**Test 2: Filter by Status**
1. Select "HADIR" from Status dropdown
2. Click "Cari"

**Expected:** Only present records shown

**Status:** [ ] PASS / [ ] FAIL

**Test 3: Filter by Username**
1. Type username in search field
2. Click "Cari"

**Expected:** Only matching username shown

**Status:** [ ] PASS / [ ] FAIL

**Test 4: Date Range Filter**
1. Select start date
2. Select end date
3. Click "Cari"

**Expected:** Presensi within date range shown

**Status:** [ ] PASS / [ ] FAIL

---

#### ➕ Test CREATE/EDIT/DELETE (Placeholders)
**Note:** These are currently placeholders for future implementation

**Add Presensi:**
- Click "➕ Tambah Presensi"
- Expected: Toast "Add Presensi - Coming Soon!"
- Status: [ ] PLACEHOLDER

**Edit Presensi:**
- Select row → Click Edit
- Expected: Toast "Edit Presensi - Coming Soon!"
- Status: [ ] PLACEHOLDER

**Delete Presensi:**
- Select row → Click Delete
- Expected: Toast "Delete Presensi - Coming Soon!"
- Status: [ ] PLACEHOLDER

---

### Phase 5: Analytics View

#### 📊 Open Analytics
1. Click **View → Analytics & Reports**
2. Window should open (1200x800)

**Verify:**
- [ ] PieChart shows status distribution
- [ ] BarChart shows daily attendance
- [ ] LineChart shows weekly trends
- [ ] Date range filters work
- [ ] Refresh button updates charts
- [ ] Clear cache button works

**Status:** [ ] PASS / [ ] FAIL  
**Notes:** _________________________________________

---

## 🐛 Error Scenarios to Test

### 1. Network Errors
**Test:** Stop backend server while desktop app is running

**Steps:**
1. Stop backend (Ctrl+C in backend terminal)
2. In desktop app, click "Refresh" in any management screen

**Expected:**
- Error toast notification
- Status label shows error message
- App doesn't crash

**Status:** [ ] PASS / [ ] FAIL

---

### 2. Invalid Data
**Test:** Submit form with empty required fields

**Steps:**
1. Click "Add Siswa"
2. Leave NIS and Nama empty
3. Click OK

**Expected:**
- Form validation error (if implemented)
- Or backend returns 400 error
- User-friendly error message shown

**Status:** [ ] PASS / [ ] FAIL

---

### 3. Duplicate Data
**Test:** Add siswa with existing NIS

**Steps:**
1. Add siswa with NIS "12345"
2. Try to add another siswa with same NIS "12345"

**Expected:**
- Backend returns error (409 Conflict or 400 Bad Request)
- Error toast shown to user

**Status:** [ ] PASS / [ ] FAIL

---

### 4. Delete Non-Existent
**Test:** Delete record that doesn't exist

**Steps:**
1. Manually modify ID in code or use deleted record
2. Try to delete

**Expected:**
- Backend returns 404
- Error message shown

**Status:** [ ] PASS / [ ] FAIL

---

## 📊 Test Summary

### Siswa Management
- [ ] READ: ___/___
- [ ] CREATE: ___/___
- [ ] UPDATE: ___/___
- [ ] DELETE: ___/___
- [ ] SEARCH: ___/___
- [ ] FILTERS: ___/___

### Guru Management
- [ ] READ: ___/___
- [ ] CREATE: ___/___
- [ ] UPDATE: ___/___ (Placeholder)
- [ ] DELETE: ___/___
- [ ] SEARCH: ___/___
- [ ] FILTERS: ___/___

### Presensi Management
- [ ] READ: ___/___
- [ ] FILTERS: ___/___
- [ ] CREATE: Placeholder
- [ ] UPDATE: Placeholder
- [ ] DELETE: Placeholder

### Overall Status
- **Total Tests**: ___
- **Passed**: ___
- **Failed**: ___
- **Placeholders**: 3

---

## 🔍 Additional Testing Notes

### Performance
- [ ] Large dataset handling (>100 records)
- [ ] Concurrent operations
- [ ] Memory usage during operations

### UI/UX
- [ ] Loading indicators appear/disappear correctly
- [ ] Toast notifications timing (3-5 seconds)
- [ ] Modal dialogs center on screen
- [ ] Table columns resize properly
- [ ] Action buttons enabled/disabled correctly

### Data Integrity
- [ ] Data persists after operations
- [ ] Refresh shows latest data
- [ ] No duplicate records created
- [ ] Foreign key relationships maintained

---

## 🚀 Next Steps After Testing

1. **Document Issues**
   - Create GitHub issues for bugs found
   - Screenshot error scenarios
   - Note API response codes

2. **Implement Placeholders**
   - Complete Guru Edit functionality
   - Implement Presensi full CRUD
   - Add form validation

3. **Enhance Features**
   - Add pagination for large datasets
   - Implement batch operations
   - Add CSV/PDF export per entity
   - Add data validation rules

4. **Performance Optimization**
   - Implement caching strategies
   - Optimize search algorithms
   - Add debouncing to search input

---

**Testing Started**: ___________  
**Testing Completed**: ___________  
**Tested By**: ___________  
**Backend Version**: 0.0.1-SNAPSHOT  
**Desktop Version**: 1.0.0  
**Notes**: ___________________________________________
