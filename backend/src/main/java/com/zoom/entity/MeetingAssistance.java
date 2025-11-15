package com.zoom.entity;

import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "assistance_values",
        joinColumns = @JoinColumn(name = "meeting_assistance_id")
    )
    @Column(name = "assistance_value")
    @OrderColumn(name = "position")
    private List<Integer> values = new ArrayList<>();

    // Constructeurs
    public MeetingAssistance() {
    }

    public MeetingAssistance(Meeting meeting, Integer total, Integer inPersonTotal, List<Integer> values) {
        this.meeting = meeting;
        this.total = total;
        this.inPersonTotal = inPersonTotal;
        this.values = values != null ? new ArrayList<>(values) : new ArrayList<>();
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

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values != null ? new ArrayList<>(values) : new ArrayList<>();
    }
}
