package com.smk.presensi.desktop.model;

/**
 * Model desktop untuk master DUDI (perusahaan PKL).
 */
public class Dudi {
    private Long id;
    private String nama;
    private String bidangUsaha;
    private String alamat;
    private String contactPerson;
    private String contactPhone;
    private Integer kuotaSiswa;
    private Boolean aktif;

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

    public Boolean getAktif() {
        return aktif;
    }

    public void setAktif(Boolean aktif) {
        this.aktif = aktif;
    }

    @Override
    public String toString() {
        return nama != null ? nama : ("DUDI #" + id);
    }
}

