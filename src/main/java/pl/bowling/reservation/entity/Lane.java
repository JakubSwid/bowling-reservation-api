package pl.bowling.reservation.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "lane")
public class Lane {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer laneNumber;

    @Column(nullable = false)
    private String status;

    @Version
    private Integer version;

    public Long getId() {
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public Integer getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(Integer laneNumber) {
        this.laneNumber = laneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
