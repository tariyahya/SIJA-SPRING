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
    private Double latitude;
    private Double longitude;
    private Integer radiusValidasi;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getRadiusValidasi() {
        return radiusValidasi;
    }

    public void setRadiusValidasi(Integer radiusValidasi) {
        this.radiusValidasi = radiusValidasi;
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

