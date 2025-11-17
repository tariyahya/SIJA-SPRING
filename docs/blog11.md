# Blog 11: Time Tracking & Work Hours - Duration API ⏱️

**Topik**: Time Tracking & Work Hours Calculation  
**Level**: Intermediate  
**Tahap**: 10  
**Estimasi Baca**: 12 menit

---

## 🎯 INTRODUCTION

Pernahkah kamu bertanya:
- Berapa jam kamu belajar di sekolah hari ini?
- Berapa lama kamu bermain game kemarin?
- Kapan deadline tugas capstone-mu?

Semua pertanyaan ini tentang **waktu**. Dan di dunia software engineering, tracking waktu adalah skill penting!

Bayangkan kamu bekerja di perusahaan software:
- **Timesheet**: Catat jam kerja (09:00-17:00 = 8 jam)
- **Overtime**: Kerja lebih dari 8 jam = lembur (dapat uang tambahan!)
- **Break time**: Istirahat makan siang (tidak dihitung jam kerja)
- **Payroll**: Gaji dihitung dari total jam kerja

Di sistem presensi SMK kita:
- **Checkin** (jam masuk): 07:15 → Start tracking
- **Checkout** (jam pulang): 15:00 → Stop tracking
- **Work hours**: 15:00 - 07:15 = **7 jam 45 menit**

Mudah, kan? Tapi bagaimana implementasinya di Java? 🤔

---

## 📚 TIME CONCEPTS

### 1. LocalTime vs LocalDateTime vs Duration

**LocalTime**: Waktu dalam sehari (jam, menit, detik)
```java
LocalTime jamMasuk = LocalTime.of(7, 15, 0);  // 07:15:00
LocalTime jamPulang = LocalTime.of(15, 0, 0); // 15:00:00

// Format: HH:mm:ss (24-hour format)
// 00:00:00 = midnight
// 12:00:00 = noon
// 23:59:59 = last second of day
```

**LocalDate**: Tanggal (tahun, bulan, hari)
```java
LocalDate today = LocalDate.of(2025, 11, 17);  // 2025-11-17
LocalDate tomorrow = today.plusDays(1);        // 2025-11-18

// Format: yyyy-MM-dd
```

**LocalDateTime**: Kombinasi tanggal + waktu
```java
LocalDateTime checkin = LocalDateTime.of(2025, 11, 17, 7, 15, 0);
// 2025-11-17T07:15:00
```

**Duration**: Rentang waktu (time span)
```java
Duration workDuration = Duration.between(jamMasuk, jamPulang);
// Duration: 7 hours 45 minutes
```

### Analogi Sederhana

Bayangkan kamu nonton film:
- **LocalTime**: Jam tayang (14:30 → jam 2 siang 30 menit)
- **LocalDate**: Tanggal tayang (2025-11-17 → 17 November 2025)
- **LocalDateTime**: Tanggal + jam tayang (2025-11-17 14:30)
- **Duration**: Durasi film (120 menit = 2 jam)

---

## ⏱️ DURATION API

### What is Duration?

> Duration = Time span between two time points.

**Contoh real-world**:
```
Start: 07:15 (checkin)
End: 15:00 (checkout)
Duration: 7 hours 45 minutes
```

### Java Implementation

```java
import java.time.LocalTime;
import java.time.Duration;

LocalTime start = LocalTime.of(7, 15, 0);   // 07:15:00
LocalTime end = LocalTime.of(15, 0, 0);     // 15:00:00

// Calculate duration
Duration duration = Duration.between(start, end);

// Get total minutes
long totalMinutes = duration.toMinutes();  // 465 minutes
System.out.println("Total minutes: " + totalMinutes);

// Get hours (integer division)
long hours = totalMinutes / 60;  // 465 / 60 = 7
System.out.println("Hours: " + hours);

// Get remaining minutes (modulo)
long minutes = totalMinutes % 60;  // 465 % 60 = 45
System.out.println("Minutes: " + minutes);

// Result: 7 hours 45 minutes
```

### Why toMinutes()?

Duration API provides multiple methods:
```java
Duration duration = Duration.between(start, end);

// Different units
duration.toSeconds();    // 27900 seconds
duration.toMinutes();    // 465 minutes
duration.toHours();      // 7 hours (truncated!)
duration.toDays();       // 0 days (< 24 hours)
```

**Problem with toHours()**:
```java
Duration duration = Duration.between(
    LocalTime.of(7, 15, 0),   // 07:15
    LocalTime.of(15, 0, 0)    // 15:00
);

long hours = duration.toHours();  // 7 (truncated!)
// Lost information: 45 minutes!
```

**Solution**: Use `toMinutes()` then convert manually:
```java
long totalMinutes = duration.toMinutes();  // 465
long hours = totalMinutes / 60;            // 7
long minutes = totalMinutes % 60;          // 45
// Complete: 7 hours 45 minutes ✅
```

---

## 🧮 ARITHMETIC OPERATIONS

### Integer Division vs Modulo

**Integer Division** (`/`):
> Berapa banyak "penuh" yang bisa dibagi?

```java
int total = 465;
int divisor = 60;

int quotient = total / divisor;  // 465 / 60 = 7
// Result: 7 (berapa jam penuh?)
```

**Modulo** (`%`):
> Berapa sisa setelah dibagi?

```java
int total = 465;
int divisor = 60;

int remainder = total % divisor;  // 465 % 60 = 45
// Result: 45 (berapa menit sisa?)
```

**Visual Explanation**:
```
465 minutes ÷ 60 minutes/hour

┌────────┬────────┬────────┬────────┬────────┬────────┬────────┬─────┐
│ Hour 1 │ Hour 2 │ Hour 3 │ Hour 4 │ Hour 5 │ Hour 6 │ Hour 7 │ 45m │
│ 60 min │ 60 min │ 60 min │ 60 min │ 60 min │ 60 min │ 60 min │     │
└────────┴────────┴────────┴────────┴────────┴────────┴────────┴─────┘
   7 full hours (quotient)                                      remainder

hours = 465 / 60 = 7
minutes = 465 % 60 = 45
```

### Real Examples

**Example 1**: 8 jam pas
```java
long totalMinutes = 480;  // 8 hours exactly

long hours = 480 / 60;     // 8
long minutes = 480 % 60;   // 0

Result: "8 hours 0 minutes" atau "8 jam"
```

**Example 2**: 9 jam 30 menit
```java
long totalMinutes = 570;  // 9.5 hours

long hours = 570 / 60;     // 9
long minutes = 570 % 60;   // 30

Result: "9 hours 30 minutes"
```

**Example 3**: 1 jam 5 menit
```java
long totalMinutes = 65;

long hours = 65 / 60;      // 1
long minutes = 65 % 60;    // 5

Result: "1 hour 5 minutes"
```

---

## 💼 OVERTIME DETECTION

### What is Overtime?

> Overtime = Working beyond standard hours (usually > 8 hours)

**Standard Work Hours**:
- Indonesia: 7-8 jam/hari (40 jam/minggu)
- Most countries: 8 jam/hari (40 jam/minggu)
- Overtime = Extra hours beyond standard

**Overtime Pay**:
- Regular: Rp 50,000/jam
- Overtime: Rp 75,000/jam (1.5× regular)
- Example:
  - 8 jam regular: 8 × 50,000 = Rp 400,000
  - 2 jam overtime: 2 × 75,000 = Rp 150,000
  - **Total**: Rp 550,000

### Implementation

```java
// Check if overtime (> 8 hours = 480 minutes)
long totalMinutes = 540;  // 9 hours

boolean isOvertime = totalMinutes > 480;
// true (9 hours > 8 hours)

if (isOvertime) {
    long overtimeMinutes = totalMinutes - 480;  // 60 minutes
    long overtimeHours = overtimeMinutes / 60;  // 1 hour
    System.out.println("Overtime: " + overtimeHours + " hour(s)");
}
```

**Edge Cases**:
```java
// Case 1: Exactly 8 hours (NO overtime)
long minutes = 480;
boolean overtime = minutes > 480;  // false

// Case 2: 8 hours 1 minute (YES overtime, but only 1 minute)
long minutes = 481;
boolean overtime = minutes > 480;  // true

// Case 3: Less than 8 hours (NO overtime)
long minutes = 465;  // 7h 45m
boolean overtime = minutes > 480;  // false
```

---

## 🏫 REAL-WORLD USE CASES

### 1. School Attendance (SMK)

**Scenario**:
- Jam sekolah: 07:00 - 15:00 (8 jam)
- Siswa checkin: 07:15 (terlambat 15 menit)
- Siswa checkout: 15:00
- Work hours: 7 jam 45 menit

**Calculation**:
```java
LocalTime checkin = LocalTime.of(7, 15, 0);   // 07:15
LocalTime checkout = LocalTime.of(15, 0, 0);  // 15:00

Duration duration = Duration.between(checkin, checkout);
long totalMinutes = duration.toMinutes();  // 465 minutes

long hours = totalMinutes / 60;    // 7
long minutes = totalMinutes % 60;  // 45

// Display: "Kamu belajar 7 jam 45 menit hari ini"
```

**Use Cases**:
- Monitor attendance duration
- Identify early leavers (checkout sebelum 15:00)
- Track extracurricular time (after 15:00)

### 2. Office Work (Corporate)

**Scenario**:
- Standard hours: 09:00 - 17:00 (8 jam)
- Break time: 12:00 - 13:00 (1 jam, tidak dihitung)
- Effective work: 7 jam
- Employee checkin: 09:00
- Employee checkout: 18:30 (overtime 1.5 jam)

**Calculation**:
```java
LocalTime checkin = LocalTime.of(9, 0, 0);    // 09:00
LocalTime checkout = LocalTime.of(18, 30, 0); // 18:30

Duration duration = Duration.between(checkin, checkout);
long totalMinutes = duration.toMinutes();  // 570 minutes

// Subtract break time (1 hour)
long breakMinutes = 60;
long workMinutes = totalMinutes - breakMinutes;  // 510 minutes

long hours = workMinutes / 60;     // 8
long minutes = workMinutes % 60;   // 30

// Check overtime
boolean isOvertime = workMinutes > 480;  // true
long overtimeMinutes = workMinutes - 480;  // 30 minutes

// Display: "Work hours: 8h 30m (30 minutes overtime)"
```

### 3. Freelancer Time Tracking

**Scenario**:
- Hourly rate: $50/hour
- Project work: 07:30 - 12:45 (5h 15m)
- Total: 5.25 hours
- Payment: 5.25 × $50 = $262.50

**Calculation**:
```java
LocalTime start = LocalTime.of(7, 30, 0);   // 07:30
LocalTime end = LocalTime.of(12, 45, 0);    // 12:45

Duration duration = Duration.between(start, end);
long totalMinutes = duration.toMinutes();  // 315 minutes

// Convert to decimal hours
double hours = totalMinutes / 60.0;  // 5.25 hours

// Calculate payment
double hourlyRate = 50.0;
double payment = hours * hourlyRate;  // $262.50

// Display: "Time: 5.25 hours, Payment: $262.50"
```

---

## 📊 STATISTICS & ANALYTICS

### 1. Average Work Hours

**Scenario**: Calculate average work hours in a month

```java
// Sample data (in minutes)
List<Long> workHoursList = List.of(
    465L,  // Day 1: 7h 45m
    480L,  // Day 2: 8h 0m
    495L,  // Day 3: 8h 15m
    450L,  // Day 4: 7h 30m
    470L   // Day 5: 7h 50m
);

// Calculate total
long totalMinutes = workHoursList.stream()
        .mapToLong(Long::longValue)
        .sum();
// 465 + 480 + 495 + 450 + 470 = 2360 minutes

// Calculate average
double avgMinutes = (double) totalMinutes / workHoursList.size();
// 2360 / 5 = 472 minutes

// Convert to hours (decimal)
double avgHours = avgMinutes / 60.0;
// 472 / 60 = 7.87 hours

// Display: "Average work hours: 7.87 hours/day"
```

### 2. Overtime Frequency

**Scenario**: How many days had overtime this month?

```java
List<Long> workHoursList = List.of(
    465L,  // 7h 45m - NO overtime
    480L,  // 8h 0m - NO overtime (exactly 8 hours)
    495L,  // 8h 15m - YES overtime (15 minutes)
    450L,  // 7h 30m - NO overtime
    510L   // 8h 30m - YES overtime (30 minutes)
);

// Count overtime days
long overtimeDays = workHoursList.stream()
        .filter(minutes -> minutes > 480)
        .count();
// Result: 2 days

// Percentage
double overtimeRate = (overtimeDays * 100.0) / workHoursList.size();
// (2 * 100.0) / 5 = 40%

// Display: "Overtime frequency: 2 out of 5 days (40%)"
```

### 3. Checkout Completion Rate

**Scenario**: How many people completed checkout today?

```java
// Sample data
int totalCheckin = 100;      // 100 people checked in
int completedCheckout = 95;  // 95 people checked out
int missedCheckout = 5;      // 5 people forgot to checkout

// Calculate completion rate
double completionRate = (completedCheckout * 100.0) / totalCheckin;
// (95 * 100.0) / 100 = 95%

// Display: "Checkout completion rate: 95%"
// Warning: "5 people forgot to checkout!"
```

---

## 🚨 COMMON PITFALLS

### Pitfall 1: Using toHours() for Precision

❌ **Wrong**:
```java
Duration duration = Duration.between(
    LocalTime.of(7, 15, 0),
    LocalTime.of(15, 0, 0)
);

long hours = duration.toHours();  // 7 (lost 45 minutes!)
```

✅ **Correct**:
```java
long totalMinutes = duration.toMinutes();  // 465
long hours = totalMinutes / 60;            // 7
long minutes = totalMinutes % 60;          // 45
```

### Pitfall 2: Integer vs Double Division

❌ **Wrong**:
```java
int totalMinutes = 465;
int hours = totalMinutes / 60;  // 7 (integer division)

// Try to get decimal
double decimalHours = hours;  // 7.0 (no decimals!)
```

✅ **Correct**:
```java
long totalMinutes = 465;
double decimalHours = totalMinutes / 60.0;  // 7.75 hours
```

### Pitfall 3: Negative Duration

❌ **Problem**:
```java
LocalTime start = LocalTime.of(15, 0, 0);  // 15:00
LocalTime end = LocalTime.of(7, 15, 0);    // 07:15 (next day?)

Duration duration = Duration.between(start, end);
// Negative duration! (crossing midnight)
```

✅ **Solution**: Use LocalDateTime for multi-day tracking
```java
LocalDateTime start = LocalDateTime.of(2025, 11, 17, 15, 0, 0);
LocalDateTime end = LocalDateTime.of(2025, 11, 18, 7, 15, 0);  // Next day

Duration duration = Duration.between(start, end);
// Positive duration: 16h 15m
```

### Pitfall 4: Forgetting Break Time

❌ **Wrong**:
```java
// 09:00 - 17:00 = 8 hours
// But user had 1 hour lunch break!
// Actual work: 7 hours
```

✅ **Correct**:
```java
long totalMinutes = 480;  // 8 hours
long breakMinutes = 60;   // 1 hour lunch
long workMinutes = totalMinutes - breakMinutes;  // 7 hours
```

---

## 💡 BEST PRACTICES

### 1. Always Use LocalTime for Time of Day

```java
// ✅ Good: LocalTime for time of day
LocalTime jamMasuk = LocalTime.of(7, 15, 0);

// ❌ Bad: String parsing
String jamMasuk = "07:15:00";  // Hard to calculate!
```

### 2. Store Times as LocalTime in Database

```sql
-- Database schema
CREATE TABLE presensi (
    id BIGINT PRIMARY KEY,
    jam_masuk TIME,      -- LocalTime
    jam_pulang TIME,     -- LocalTime
    tanggal DATE         -- LocalDate
);
```

```java
// Java entity
@Column(name = "jam_masuk")
private LocalTime jamMasuk;

@Column(name = "jam_pulang")
private LocalTime jamPulang;
```

### 3. Use Duration for Calculations

```java
// ✅ Good: Duration API
Duration duration = Duration.between(start, end);
long minutes = duration.toMinutes();

// ❌ Bad: Manual calculation
long startMinutes = start.getHour() * 60 + start.getMinute();
long endMinutes = end.getHour() * 60 + end.getMinute();
long diff = endMinutes - startMinutes;  // Error-prone!
```

### 4. Handle Null Values

```java
// Check before calculating
if (presensi.getJamMasuk() == null || presensi.getJamPulang() == null) {
    throw new RuntimeException("Data jam belum lengkap");
}

// Safe to calculate
Duration duration = Duration.between(
    presensi.getJamMasuk(),
    presensi.getJamPulang()
);
```

### 5. Round for Display

```java
// Calculate average
double avgMinutes = 472.5;
double avgHours = avgMinutes / 60.0;  // 7.875 hours

// Round to 2 decimals for display
double rounded = Math.round(avgHours * 100.0) / 100.0;
// 7.88 hours (user-friendly!)
```

---

## 🎯 QUIZ

Test your understanding:

**Q1**: What's the result?
```java
long totalMinutes = 500;
long hours = totalMinutes / 60;
long minutes = totalMinutes % 60;
```
<details>
<summary>Show Answer</summary>
hours = 8 (500 / 60 = 8)
minutes = 20 (500 % 60 = 20)
Result: 8 hours 20 minutes
</details>

**Q2**: Is this overtime?
```java
long totalMinutes = 480;  // 8 hours
boolean isOvertime = totalMinutes > 480;
```
<details>
<summary>Show Answer</summary>
false (8 hours is NOT overtime, overtime starts at > 8 hours)
</details>

**Q3**: What's wrong with this code?
```java
Duration duration = Duration.between(start, end);
long hours = duration.toHours();  // 7
```
<details>
<summary>Show Answer</summary>
toHours() truncates! If duration is 7h 45m, you lose 45 minutes.
Use toMinutes() then divide manually.
</details>

**Q4**: Calculate payment
```java
long totalMinutes = 315;  // 5h 15m
double hourlyRate = 50.0;
double payment = ?
```
<details>
<summary>Show Answer</summary>
double hours = totalMinutes / 60.0;  // 5.25 hours
double payment = hours * hourlyRate;  // $262.50
</details>

---

## 📚 REFERENCES

- **Java Duration API**: [Oracle Docs](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html)
- **LocalTime API**: [Oracle Docs](https://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html)
- **Time Tracking Best Practices**: [Martin Fowler](https://martinfowler.com/)
- **Overtime Regulations (Indonesia)**: [UU Ketenagakerjaan No. 13/2003](https://peraturan.bpk.go.id/Home/Details/43013)

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025  
**Next Blog**: Export to CSV/PDF (Tahap 11)

**🎉 Happy Learning!**
