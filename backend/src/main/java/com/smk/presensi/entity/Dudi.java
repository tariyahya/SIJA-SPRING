package com.smk.presensi.entity;

import jakarta.persistence.*;

/**
 * Entity Dudi - master data perusahaan/instansi PKL.
 *
 * MVP fields:
 * - nama: nama perusahaan / instansi
 * - bidangUsaha: kategori/bidang (IT, Otomotif, dll)
 * - alamat: alamat singkat
 * - contactPerson & contactPhone: kontak utama
 * - kuotaSiswa: kapasitas maksimal siswa PKL
 * - aktif: status aktif/nonaktif
 */
@Entity
@Table(name = "dudi")
public class Dudi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nama;

    @Column(name = "bidang_usaha", length = 100)
    private String bidangUsaha;

    @Column(length = 255)
    private String alamat;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "kuota_siswa")
    private Integer kuotaSiswa;

    @Column(nullable = false)
    private boolean aktif = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getBidangUsaha() {
        return bidangUsaha;
    }

    public void setBidangUsaha(String bidangUsaha) {
        this.bidangUsaha = bidangUsaha;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getKuotaSiswa() {
        return kuotaSiswa;
    }

    public void setKuotaSiswa(Integer kuotaSiswa) {
        this.kuotaSiswa = kuotaSiswa;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }
}

