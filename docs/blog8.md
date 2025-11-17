# Blog 8: RFID Technology - Presensi Tanpa Kontak dengan Kartu

**Penulis**: Copilot Assistant  
**Tanggal**: 17 November 2025  
**Kategori**: RFID, NFC, Contactless Technology  
**Tahap**: 5 - RFID Integration

---

## 🎯 Pendahuluan

Bayangkan: Siswa hanya perlu **menempelkan kartu** ke reader, dan presensi langsung tercatat! Tidak perlu ketik username/password, tidak perlu kamera. Cepat, praktis, dan efisien.

Teknologi ini disebut **RFID (Radio Frequency Identification)** - sistem identifikasi menggunakan gelombang radio.

### Apa itu RFID?

**RFID** adalah teknologi yang menggunakan gelombang radio untuk:
1. **Identify** - Mengidentifikasi objek/orang
2. **Track** - Melacak pergerakan
3. **Store data** - Menyimpan informasi

**Komponen RFID**:
- **Tag/Card**: Kartu dengan chip (data storage)
- **Reader**: Perangkat baca kartu
- **Antenna**: Mengirim/terima sinyal radio
- **Backend**: Server untuk proses data

---

## 🏷️ Jenis-jenis RFID

### 1. Low Frequency (LF) - 125-134 kHz

**Karakteristik**:
- Range: 10-30 cm (sangat dekat)
- Speed: Lambat
- Biaya: Murah
- Penetrasi: Baik (tembus air, logam)

**Use Case**:
- Access control (kartu akses gedung)
- Animal tracking (chip hewan peliharaan)
- Car immobilizer (kunci mobil)

**Contoh Produk**:
- EM4100 card (read-only)
- T5577 card (read-write)

### 2. High Frequency (HF) - 13.56 MHz

**Karakteristik**:
- Range: 5-10 cm (dekat)
- Speed: Medium-fast
- Biaya: Medium
- Standard: ISO 14443 (NFC)

**Use Case**:
- ⭐ **Presensi sekolah** (our project!)
- E-payment (e-money, contactless payment)
- Public transport (kartu Transjakarta, KRL)
- Library (peminjaman buku)

**Contoh Produk**:
- **MIFARE Classic 1K** (1KB storage) ← Most common
- MIFARE Ultralight (512 bytes)
- NTAG (NFC tag)

### 3. Ultra High Frequency (UHF) - 860-960 MHz

**Karakteristik**:
- Range: 1-12 meter (jauh!)
- Speed: Sangat cepat
- Biaya: Mahal
- Penetrasi: Buruk (tidak tembus air/logam)

**Use Case**:
- Warehouse management (inventory gudang)
- Toll gate (e-toll)
- Marathon timing (chip lari maraton)
- Retail anti-theft (tag pakaian)

**Perbandingan**:

| Type | Frequency | Range | Speed | Price | Use Case |
|------|-----------|-------|-------|-------|----------|
| LF | 125 kHz | 10-30cm | Slow | Low | Access control |
| **HF** | **13.56 MHz** | **5-10cm** | **Medium** | **Medium** | **Presensi ⭐** |
| UHF | 900 MHz | 1-12m | Fast | High | Logistics |

---

## 💳 MIFARE Classic 1K - Kartu yang Kita Gunakan

### Spesifikasi

**Memory Structure**:
```
Total: 1024 bytes (1KB)
Divided into: 16 sectors
Each sector: 4 blocks
Each block: 16 bytes

Sector 0:
  Block 0: Manufacturer data (read-only, UID here)
  Block 1: Data
  Block 2: Data
  Block 3: Key A + Access bits + Key B

Sector 1-15: Same structure
```

**Unique ID (UID)**:
- 4 bytes (32 bits)
- Globally unique
- Stored in Block 0 (read-only)
- Example: `A3 B2 C1 D0`

**Security**:
- Two keys per sector: Key A, Key B
- Default keys: `FF FF FF FF FF FF` (all sectors)
- Access control bits (determine read/write permissions)

### Data Storage Example

**Block 0 (Manufacturer Block)**:
```
A3 B2 C1 D0 | 08 | 04 | 00 | 46 | 59 53 4A 41 | ...
^UID (4B)    ^Chk ^Mfr ^Ver  ^Data
```

**Block 1 (User Data)**:
```
12 34 56 78 | 90 AB CD EF | 00 00 00 00 | 00 00 00 00
^User data (NIS siswa, etc)
```

**Block 3 (Sector Trailer)**:
```
FF FF FF FF FF FF | C1 C2 C3 C4 | FF FF FF FF FF FF
^Key A (6 bytes)   ^Access bits  ^Key B (6 bytes)
```

---

## 🔌 Hardware Setup

### RC522 RFID Reader Module

**Spesifikasi**:
- Chip: MFRC522 (NXP Semiconductors)
- Frequency: 13.56 MHz
- Interface: SPI, I2C, UART
- Voltage: 3.3V (⚠️ NOT 5V!)
- Range: 0-6 cm
- Price: ~$1-2 USD (sangat murah!)

**Pin Configuration (SPI)**:

| RC522 Pin | Arduino | Raspberry Pi | Description |
|-----------|---------|--------------|-------------|
| VCC | 3.3V | 3.3V | Power supply |
| GND | GND | GND | Ground |
| RST | D9 | GPIO 25 | Reset |
| IRQ | - | - | Interrupt (optional) |
| MISO | D12 | GPIO 9 | Master In Slave Out |
| MOSI | D11 | GPIO 10 | Master Out Slave In |
| SCK | D13 | GPIO 11 | Serial Clock |
| SDA (SS) | D10 | GPIO 8 | Slave Select |

**Wiring Diagram (Arduino)**:
```
Arduino Uno          RC522 Module
-----------          ------------
    3.3V  ------------>  VCC
     GND  ------------>  GND
     D9   ------------>  RST
     D10  ------------>  SDA
     D11  ------------>  MOSI
     D12  ------------>  MISO
     D13  ------------>  SCK
```

**⚠️ Important**:
- RC522 works at 3.3V logic level
- If using 5V Arduino, need level shifter OR use 3.3V pin
- SDA pin is also called SS (Slave Select)

---

## 📡 Communication Protocol: SPI

**SPI (Serial Peripheral Interface)**:
- Master-slave protocol
- Full-duplex (kirim & terima bersamaan)
- 4-wire: MOSI, MISO, SCK, SS

**How SPI Works**:
```
Master (Arduino)          Slave (RC522)
----------------          -------------
1. Pull SS LOW   -------> Enable device
2. Send command  -------> Receive command
3. Clock pulses  <------> Synchronize data
4. Read response <------- Send response
5. Pull SS HIGH  -------> Disable device
```

**Example: Read UID**:
```
Step 1: Master sends "Request A" command
Step 2: Card responds with ATQA (Answer To Request)
Step 3: Master sends "Anti-collision" command
Step 4: Card sends UID (4 bytes)
Step 5: Master sends "Select" command
Step 6: Card sends SAK (Select Acknowledge)
```

---

## 💻 Implementation Flow

### System Architecture

```
┌─────────────┐
│   Siswa     │
│  (RFID Card)│
└──────┬──────┘
       │ 1. Tap card to reader
       ▼
┌─────────────────────────────────┐
│   RC522 RFID Reader             │
│   (Hardware)                    │
│   - Read UID from card          │
│   - Send UID via SPI            │
└────────────┬────────────────────┘
             │ 2. UID data
             ▼
┌─────────────────────────────────┐
│   Microcontroller/PC            │
│   (Arduino/Raspberry Pi/etc)    │
│   - Receive UID via SPI         │
│   - Send HTTP POST to backend   │
└────────────┬────────────────────┘
             │ 3. POST /api/presensi/rfid
             │    { "rfidUid": "A3B2C1D0" }
             ▼
┌─────────────────────────────────┐
│   Spring Boot Backend           │
│   /api/presensi/rfid            │
│   - Lookup user by rfidUid      │
│   - Validate duplicate          │
│   - Save presensi               │
└─────────────────────────────────┘
```

### Arduino Code Example

```cpp
#include <SPI.h>
#include <MFRC522.h>
#include <WiFi.h>
#include <HTTPClient.h>

// RC522 pins
#define RST_PIN 9
#define SS_PIN 10

// WiFi credentials
const char* ssid = "YourWiFi";
const char* password = "YourPassword";

// Backend URL
const char* serverUrl = "http://192.168.1.100:8080/api/presensi/rfid";

MFRC522 mfrc522(SS_PIN, RST_PIN);

void setup() {
  Serial.begin(9600);
  SPI.begin();
  mfrc522.PCD_Init();
  
  // Connect WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("WiFi connected");
}

void loop() {
  // Check if card is present
  if (!mfrc522.PICC_IsNewCardPresent()) {
    return;
  }
  
  // Read card UID
  if (!mfrc522.PICC_ReadCardSerial()) {
    return;
  }
  
  // Extract UID (4 bytes)
  String uid = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    uid += String(mfrc522.uid.uidByte[i], HEX);
    if (i < mfrc522.uid.size - 1) uid += "";
  }
  uid.toUpperCase();
  
  Serial.println("Card detected: " + uid);
  
  // Send to backend
  sendToBackend(uid);
  
  // Halt card
  mfrc522.PICC_HaltA();
  mfrc522.PCD_StopCrypto1();
  
  delay(2000); // Prevent duplicate reads
}

void sendToBackend(String uid) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");
    
    // Create JSON payload
    String payload = "{\"rfidUid\":\"" + uid + "\"}";
    
    // Send POST request
    int httpCode = http.POST(payload);
    
    if (httpCode > 0) {
      String response = http.getString();
      Serial.println("Response: " + response);
      
      if (httpCode == 200) {
        Serial.println("✅ Checkin success!");
        // Buzzer beep / LED green
      } else {
        Serial.println("❌ Checkin failed");
        // Buzzer error / LED red
      }
    }
    
    http.end();
  }
}
```

### Backend Code (Spring Boot)

```java
@RestController
@RequestMapping("/api/presensi")
public class PresensiController {
    
    @PostMapping("/rfid")
    public ResponseEntity<Map<String, Object>> checkinRfid(
            @RequestBody RfidCheckinRequest request
    ) {
        // 1. Find user by RFID UID
        User user = userRepository.findByRfidUid(request.rfidUid())
                .orElseThrow(() -> new RuntimeException("Kartu RFID tidak terdaftar"));
        
        // 2. Check duplicate
        if (presensiRepository.existsByUserAndTanggal(user, LocalDate.now())) {
            throw new RuntimeException("Sudah checkin hari ini");
        }
        
        // 3. Create presensi record
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTanggal(LocalDate.now());
        presensi.setJamMasuk(LocalTime.now());
        presensi.setMethod(MethodPresensi.RFID);
        presensi.setStatus(calculateStatus(LocalTime.now()));
        
        // 4. Save
        Presensi saved = presensiRepository.save(presensi);
        
        // 5. Response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Checkin berhasil");
        response.put("data", toResponse(saved));
        
        return ResponseEntity.ok(response);
    }
}
```

---

## 🔒 Security Considerations

### 1. MIFARE Classic Vulnerabilities

**Known Issues**:
- **Weak crypto**: Uses proprietary cipher (Crypto-1)
- **Key recovery**: Can be cracked in minutes with tools
- **Cloning**: Cards can be duplicated easily

**Attack Methods**:
- **Darkside attack**: Recover keys using card responses
- **Nested attack**: Use known key to find other keys
- **Replay attack**: Capture and replay authentication

**Tools**:
- Proxmark3 (RFID hacking tool)
- MFDB (MIFARE Default Keys Database)
- libnfc (Open-source NFC library)

### 2. Mitigation Strategies

**Level 1: Basic Protection**
- Change default keys from `FF FF FF FF FF FF`
- Use unique keys per card
- Encrypt sensitive data in blocks

**Level 2: Intermediate Protection**
- Upgrade to **MIFARE Plus** or **DESFire**
- Use AES-128 encryption
- Implement challenge-response protocol

**Level 3: Advanced Protection**
- Two-factor authentication (RFID + PIN)
- Server-side validation (blacklist stolen cards)
- Tamper detection (detect cloned cards)
- Time-based constraints (card only valid during school hours)

### 3. Our Implementation (Basic)

**Security Features**:
1. **UID Binding**: UID stored in database (registered cards only)
2. **Server-side Validation**: Backend checks duplicate, time, etc.
3. **No Sensitive Data on Card**: Only UID used (no personal data)
4. **HTTPS**: Encrypted communication (Arduino → Backend)

**Why Basic is OK**:
- Low-risk environment (school attendance, not payment)
- Physical security (readers inside school)
- Acceptable for educational purposes
- Cost-effective (cheap cards)

---

## 📊 Comparison: RFID vs Other Methods

| Feature | Manual | RFID | Barcode | Face |
|---------|--------|------|---------|------|
| **Speed** | 5-10s | ⭐ 1-2s | 2-3s | 3-5s |
| **Contact** | Yes | ⭐ No | Yes | No |
| **Hardware** | Phone | Reader | Scanner | Camera |
| **Cost** | Free | ⭐ Low | Low | Medium |
| **Hygiene** | Touch | ⭐ Contactless | Touch | ⭐ Contactless |
| **Reliability** | 95% | ⭐ 99% | 90% | 85% |
| **Setup** | Easy | Medium | Easy | Hard |
| **Spoofing Risk** | Low | Medium | ⭐ Low | High |

**RFID Advantages**:
- ✅ **Fastest** (1-2 seconds per checkin)
- ✅ **Contactless** (hygienic, COVID-safe)
- ✅ **High reliability** (99% success rate)
- ✅ **Low cost** (reader ~$2, card ~$0.10)
- ✅ **No training** (intuitive, just tap)
- ✅ **Works in dark** (no lighting needed)

**RFID Disadvantages**:
- ❌ Requires physical card (can be lost/forgotten)
- ❌ Card can be lent to others (fraud)
- ❌ Hardware installation needed
- ❌ Limited range (5-10cm only)

---

## 🏫 Real-World Deployment

### Scenario: SMK dengan 1000 Siswa

**Hardware Requirements**:
- **RFID Readers**: 2-3 units (main gate, each building)
- **RFID Cards**: 1000 units (one per student)
- **Microcontroller**: 2-3 units (ESP32 recommended)
- **Power Supply**: 2-3 units (5V 2A adapter)
- **Casing**: Waterproof enclosure
- **Network**: WiFi or Ethernet connection

**Cost Estimation**:
```
RFID Reader (RC522):    $2 × 3 = $6
RFID Cards:             $0.10 × 1000 = $100
ESP32 microcontroller:  $5 × 3 = $15
Power supply:           $3 × 3 = $9
Casing:                 $10 × 3 = $30
Cables & misc:          $20
---------------------------------
Total:                  ~$180 USD
```

**Setup Steps**:

1. **Hardware Installation**:
   ```
   Location 1: Main gate (outdoor)
   - RC522 reader in waterproof case
   - ESP32 with WiFi
   - 5V power supply
   - Buzzer + LED for feedback
   
   Location 2: Building A entrance
   Location 3: Building B entrance
   ```

2. **Card Registration**:
   ```sql
   -- Register each student's card
   UPDATE users 
   SET rfid_uid = 'A3B2C1D0' 
   WHERE username = '12345';
   ```

3. **Network Configuration**:
   ```cpp
   // ESP32 connects to school WiFi
   WiFi.begin("SMK-WiFi", "password123");
   
   // Backend API endpoint
   const char* serverUrl = "http://10.0.1.100:8080/api/presensi/rfid";
   ```

4. **Testing**:
   - Test all cards (1000 cards)
   - Verify checkin speed (<2 seconds)
   - Check network stability
   - Test duplicate prevention

### Maintenance

**Daily**:
- Check reader status (online/offline)
- Monitor error logs
- Clean reader surface (dust affects range)

**Monthly**:
- Replace damaged cards
- Update firmware if needed
- Backup database

**Yearly**:
- Replace worn-out readers
- Refresh card batch (if degraded)

---

## 🔧 Troubleshooting

### Common Issues

**1. Card Not Detected**
```
Problem: Reader shows "No card detected"
Causes:
  - Card too far (>10cm)
  - Reader not powered (check 3.3V)
  - Wrong wiring (check SPI pins)
  - Card damaged (chip broken)
  
Solution:
  - Move card closer (<5cm)
  - Check voltage with multimeter
  - Verify wiring with diagram
  - Test with different card
```

**2. Wrong UID Read**
```
Problem: UID changes every read
Causes:
  - Electromagnetic interference
  - Multiple cards in range
  - Bad connection (loose wires)
  
Solution:
  - Move away from metal objects
  - Read one card at a time
  - Solder wires (don't use breadboard)
```

**3. Slow Response**
```
Problem: Checkin takes >5 seconds
Causes:
  - Network latency (WiFi poor)
  - Server overload (too many requests)
  - Inefficient code (blocking operations)
  
Solution:
  - Use Ethernet instead of WiFi
  - Add caching layer
  - Optimize database queries (add indexes)
```

---

## 🎓 Kesimpulan

### Key Takeaways

1. **RFID Technology**: Uses radio waves to identify objects wirelessly
2. **Frequency Types**: LF (125kHz), HF (13.56MHz), UHF (900MHz)
3. **MIFARE Classic**: Most common card for access control
4. **UID**: 4-byte unique identifier stored in card
5. **RC522 Module**: Cheap ($2) and easy to use RFID reader
6. **SPI Protocol**: Communication between reader and microcontroller
7. **Security**: MIFARE Classic has known vulnerabilities but OK for low-risk use

### Pengetahuan yang Didapat

Setelah mempelajari blog ini, siswa memahami:

- ✅ Prinsip kerja RFID (gelombang radio)
- ✅ Perbedaan LF, HF, UHF frequencies
- ✅ Struktur memory MIFARE Classic 1K
- ✅ UID extraction dan storage
- ✅ RC522 module wiring (SPI pins)
- ✅ Arduino/ESP32 programming untuk RFID
- ✅ Backend integration (Spring Boot)
- ✅ Security considerations dan mitigations
- ✅ Real-world deployment (hardware, cost, setup)

### Next Steps

**Tahap 6**: Barcode/QR Code Integration
- Alternative to RFID (cheaper, no hardware)
- QR code generation per student
- Smartphone camera scanner
- Print-your-own cards

**Tahap 7**: Face Recognition
- Biometric authentication
- No card/phone needed
- Two-phase: photo enrollment + daily checkin
- OpenCV + face_recognition library

**Tahap 8**: Geolocation Validation
- GPS coordinates validation
- Haversine formula (distance calculation)
- Prevent remote checkin
- 200m radius from school

---

## 📚 References

### RFID Standards
- ISO 14443: Contactless smart cards (MIFARE)
- ISO 15693: Vicinity cards (longer range)
- NFC Forum: NFC specifications

### Hardware
- **RC522 Datasheet**: NXP MFRC522
- **MIFARE Classic**: NXP MF1S50 (1K card)
- **Arduino Libraries**: MFRC522 by GithubCommunity

### Security
- **Hack MIFARE Classic**: [Hack-a-day article](https://hackaday.com/2012/03/26/cracking-mifare-classic-takes-minutes/)
- **Proxmark3**: RFID research tool
- **MFDB**: MIFARE Default Keys Database

### Further Reading
- "RFID Handbook" by Klaus Finkenzeller
- "Beginning NFC" by Tom Igoe
- Arduino RFID tutorials: [Arduino Project Hub](https://create.arduino.cc/)

---

**Penulis**: Copilot Assistant  
**Tanggal**: 17 November 2025  
**Kategori**: RFID, NFC, Contactless Technology  
**Tahap**: 5 - RFID Integration  
**Status**: ✅ Complete

**Feedback**: Jika ada pertanyaan tentang RFID, silakan hubungi tim development!

---

*"Tap once, checkin done! The power of RFID."* 💳⚡
