package com.zoom.entity;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_assistance")
public class MeetingAssistance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "meeting_id", unique = true, nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private Integer total;

    @Column(name = "in_person_total", nullable = false)
    private Integer inPersonTotal;

    // Stocke la valeur d'assistance pour chaque participant par leur ID
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "assistance_values",
        joinColumns = @JoinColumn(name = "meeting_assistance_id")
    )
    @MapKeyColumn(name = "participant_id")
    @Column(name = "assistance_value")
    private Map<Long, Integer> values = new HashMap<>();

    // Constructeurs
    public MeetingAssistance() {
    }

    public MeetingAssistance(Meeting meeting, Integer total, Integer inPersonTotal, Map<Long, Integer> values) {
        this.meeting = meeting;
        this.total = total;
        this.inPersonTotal = inPersonTotal;
        this.values = values != null ? new HashMap<>(values) : new HashMap<>();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getInPersonTotal() {
        return inPersonTotal;
    }

    public void setInPersonTotal(Integer inPersonTotal) {
        this.inPersonTotal = inPersonTotal;
    }

    public Map<Long, Integer> getValues() {
        return values;
    }

    public void setValues(Map<Long, Integer> values) {
        this.values = values != null ? new HashMap<>(values) : new HashMap<>();
    }
}
