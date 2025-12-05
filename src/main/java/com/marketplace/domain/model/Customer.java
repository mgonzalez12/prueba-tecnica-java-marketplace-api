package com.marketplace.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Customer {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String status;

    // Modern Date
    private LocalDate birthDate;

    // Legacy Date (for requirement demonstration)
    private Date registrationDate;

    private LocalDate lastActivityDate;
    private LocalDate renewalDate; // For subscription renewals
    private String phone;
    private String address;

    // Activity history tracking
    private Integer totalOrders;
    private BigDecimal totalSpent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDate renewalDate) {
        this.renewalDate = renewalDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public int getAge() {
        if (birthDate == null)
            return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Calculates seniority in days using Legacy Date (java.util.Date)
     * Demonstrating usage of legacy API as requested.
     */
    public long getSeniorityInDaysLegacy() {
        if (registrationDate == null)
            return 0;
        Date now = new Date();
        long diffInMillies = Math.abs(now.getTime() - registrationDate.getTime());
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculates seniority in years using Modern Date (java.time)
     * Converting legacy Date to LocalDate for calculation.
     */
    public int getSeniorityInYearsModern() {
        if (registrationDate == null)
            return 0;
        LocalDate regDate = registrationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return Period.between(regDate, LocalDate.now()).getYears();
    }

    /**
     * Calculates renewal cycle in months using Legacy Calendar API
     * Demonstrating usage of legacy Calendar API as requested.
     */
    public int getRenewalCycleInMonthsLegacy() {
        if (renewalDate == null || registrationDate == null)
            return 0;
        
        Calendar regCal = Calendar.getInstance();
        regCal.setTime(registrationDate);
        
        Calendar renewalCal = Calendar.getInstance();
        renewalCal.setTime(Date.from(renewalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        int yearsDiff = renewalCal.get(Calendar.YEAR) - regCal.get(Calendar.YEAR);
        int monthsDiff = renewalCal.get(Calendar.MONTH) - regCal.get(Calendar.MONTH);
        
        return (yearsDiff * 12) + monthsDiff;
    }

    /**
     * Updates activity date and increments order count
     */
    public void recordActivity() {
        this.lastActivityDate = LocalDate.now();
        if (this.totalOrders == null) {
            this.totalOrders = 0;
        }
        this.totalOrders++;
    }

    /**
     * Adds to total spent amount
     */
    public void addToTotalSpent(BigDecimal amount) {
        if (this.totalSpent == null) {
            this.totalSpent = BigDecimal.ZERO;
        }
        this.totalSpent = this.totalSpent.add(amount);
    }
}
